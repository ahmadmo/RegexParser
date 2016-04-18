package org.util.text.regex;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ahmad
 */
public final class Parser {

    private Parser() {
    }

    public static ParseTree parse(String regex) {
        checkParentheses(regex);
        List<Token> tokens = tokenize(regex);
        normalize(tokens);
        Slice rootSlice = makeSlice(tokens);
        Deque<Slice> slices = new ArrayDeque<>();
        slices.push(rootSlice);
        while (true) {
            List<Slice> children = slices.peek().getChildren();
            if (children.isEmpty()) {
                break;
            }
            Collections.sort(children);
            children.forEach(slices::push);
        }
        List<OpIndex> opIndices = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        while (!slices.isEmpty()) {
            Range range = slices.pop().getRange();
            OpIndex opIndex;
            Token next;
            do {
                opIndex = null;
                for (int i = range.getStartInclusive(); i < range.getEndExclusive(); i++) {
                    if (visited.contains(i)) {
                        continue;
                    }
                    next = tokens.get(i);
                    if (next instanceof OperatorToken) {
                        Operator op = ((OperatorToken) next).getOperator();
                        if (opIndex == null || op.precedence() < opIndex.op.precedence()) {
                            opIndex = new OpIndex(i, op);
                        }
                    }
                }
                if (opIndex != null) {
                    opIndices.add(opIndex);
                    visited.add(opIndex.index);
                }
            } while (opIndex != null);
        }
        Map<Range, TreeNode> nodes = new HashMap<>();
        AtomicInteger c = new AtomicInteger();
        for (OpIndex opIndex : opIndices) {
            TreeNode next = null;
            TreeNode left = leftNode(tokens, nodes, opIndex.index, c);
            switch (opIndex.op) {
                case KLEENE_STAR:
                case KLEENE_PLUS:
                    next = TreeNode.join(left, TreeNode.nodeFor(opIndex.op, new Range(opIndex.index)), c.incrementAndGet());
                    break;
                case CONCATENATION:
                    next = TreeNode.join(left, rightNode(tokens, nodes, opIndex.index, c), c.incrementAndGet());
                    break;
                case ALTERNATION:
                    next = TreeNode.or(left, rightNode(tokens, nodes, opIndex.index, c), c.incrementAndGet());
                    break;
            }
            nodes.put(next.getRange(), next);
        }
        return new ParseTree(
                nodes.isEmpty()
                        ? TreeNode.nodeFor(new Epsilon(), 0, new Range(0))
                        : nodes.values().iterator().next()
        );
    }

    private static TreeNode leftNode(List<Token> tokens, Map<Range, TreeNode> nodes, int index, AtomicInteger c) {
        do {
            --index;
            for (Map.Entry<Range, TreeNode> e : nodes.entrySet()) {
                if (e.getKey().isOnBound(index)) {
                    return nodes.remove(e.getKey());
                }
            }
        } while (index >= 0 && tokens.get(index) instanceof RightParenthesis);
//        index = opIndex.index - 1;
        return TreeNode.nodeFor(tokens.get(index), c.incrementAndGet(), new Range(index));
    }

    private static TreeNode rightNode(List<Token> tokens, Map<Range, TreeNode> nodes, int index, AtomicInteger c) {
        do {
            ++index;
            for (Map.Entry<Range, TreeNode> e : nodes.entrySet()) {
                if (e.getKey().isOnBound(index)) {
                    return nodes.remove(e.getKey());
                }
            }
        } while (index < tokens.size() && tokens.get(index) instanceof LeftParenthesis);
//        index = opIndex.index + 1;
        return TreeNode.nodeFor(tokens.get(index), c.incrementAndGet(), new Range(index));
    }

    private static void checkParentheses(String regex) {
        int lp = 0, rp = 0;
        for (int i = 0, n = regex.length(); i < n; i++) {
            switch (regex.charAt(i)) {
                case '(':
                    ++lp;
                    break;
                case ')':
                    ++rp;
                    break;
            }
        }
        if (lp != rp) {
            throw new IllegalStateException("Unbalanced Parenthesis. ( = " + lp + ", ) = " + rp);
        }
    }

    private static List<Token> tokenize(String regex) {
        StringBuilder buf = new StringBuilder(regex);
        List<Token> tokens = new ArrayList<>();
        char next;
        Character prev = null;
        for (int i = 0, n = buf.length(); i < n; i++) {
            next = buf.charAt(i);
            if (i == 0) {
                if (next == Operator.ALTERNATION.value()) {
                    tokens.add(new Epsilon());
                }
            } else if (prev == Parenthesis.LEFT.value() || prev == Operator.ALTERNATION.value()) {
                if (next == Operator.ALTERNATION.value() || next == Parenthesis.RIGHT.value()) {
                    tokens.add(new Epsilon());
                }
            } else if (!Operator.isOperator(next) && next != Parenthesis.RIGHT.value()) {
                tokens.add(new OperatorToken(Operator.CONCATENATION));
            }
            Operator op;
            Parenthesis p;
            if (next == EscapeCharacter.SYMBOL) {
                tokens.add(new EscapeCharacter());
            } else if ((op = Operator.find(next)) != null) {
                tokens.add(new OperatorToken(op));
                if (i == n - 1 && op == Operator.ALTERNATION) {
                    tokens.add(new Epsilon());
                }
            } else if ((p = Parenthesis.find(next)) != null) {
                tokens.add(p == Parenthesis.LEFT ? new LeftParenthesis() : new RightParenthesis());
            } else {
                tokens.add(new CharToken(next));
            }
            prev = next;
        }
        return tokens;
    }

    private static void normalize(List<Token> tokens) {
        { /* apply escape characters */
            int index;
            do {
                index = -1;
                for (int i = 0, n = tokens.size(); i < n; i++) {
                    if (tokens.get(i) instanceof EscapeCharacter) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    if (index == tokens.size() - 1) {
                        throw new IllegalStateException("Illegal/Unsupported escape sequence. (token index = " + index + ")");
                    }
                    tokens.remove(index);
                    tokens.set(index, new CharToken(tokens.get(index).value()));
                }
            } while (index != -1);
        }
        { /* detect dangling meta-characters */
            Token next;
            for (int i = 0, n = tokens.size(); i < n; i++) {
                next = tokens.get(i);
                if (next instanceof OperatorToken) {
                    Operator op = ((OperatorToken) next).getOperator();
                    Token prev;
                    switch (op) {
                        case KLEENE_STAR: {
                            if (i == 0 || !((prev = tokens.get(i - 1)) instanceof CharToken) && !(prev instanceof RightParenthesis)) {
                                throw new IllegalStateException("Dangling meta-character. (token index = " + i + ")");
                            }
                            break;
                        }
                        case KLEENE_PLUS: {
                            if (i == 0 || !((prev = tokens.get(i - 1)) instanceof CharToken) && !(prev instanceof RightParenthesis)
                                    && !(prev instanceof OperatorToken && ((OperatorToken) prev).getOperator() == Operator.KLEENE_STAR)) {
                                throw new IllegalStateException("Dangling meta-character. (token index = " + i + ")");
                            }
                            break;
                        }
                    }
                }
            }
        }
        tokens.add(0, new LeftParenthesis());
        tokens.add(new RightParenthesis());
    }

    private static Slice makeSlice(List<Token> tokens) {
        Deque<Cursor> cursors = new ArrayDeque<>();
        Map<Integer, Deque<Slice>> slices = new HashMap<>();
        int level = 0;
        Token next;
        for (int i = 0, n = tokens.size(); i < n; i++) {
            next = tokens.get(i);
            if (next instanceof ParenthesisToken) {
                if (((ParenthesisToken) next).isRight()) {
                    Cursor cursor;
                    while (true) {
                        cursor = cursors.pop();
                        if (cursor.token instanceof LeftParenthesis) {
                            break;
                        }
                    }
                    Slice nextSlice = new Slice(new Range(cursor.index + 1, i));
                    Deque<Slice> higherLevelSlices = slices.get(level + 1);
                    if (higherLevelSlices != null) while (!higherLevelSlices.isEmpty()) {
                        nextSlice.getChildren().add(higherLevelSlices.pop());
                    }
                    slices.computeIfAbsent(level, l -> new ArrayDeque<>()).push(nextSlice);
                    --level;
                } else {
                    ++level;
                    cursors.push(new Cursor(i, next));
                }
            } else {
                cursors.push(new Cursor(i, next));
            }
        }
        return slices.get(1).pop();
    }

    private static final class Cursor {

        private int index;
        private Token token;

        private Cursor(int index, Token token) {
            this.index = index;
            this.token = token;
        }

    }

    private static final class OpIndex {

        private final int index;
        private final Operator op;

        private OpIndex(int index, Operator op) {
            this.index = index;
            this.op = op;
        }

    }

}
