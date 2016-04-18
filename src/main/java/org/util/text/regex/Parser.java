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
            Slice next = slices.pop();
            Range range = next.getRange();
            OpIndex opIndex;
            do {
                opIndex = null;
                for (int i = range.getStartInclusive(); i < range.getEndExclusive(); i++) {
                    if (visited.contains(i)) {
                        continue;
                    }
                    Token token = tokens.get(i);
                    if (token instanceof OperatorToken) {
                        Operator op = ((OperatorToken) token).getOperator();
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
            TreeNode leftNode = leftNode(tokens, nodes, opIndex, c);
            switch (opIndex.op) {
                case KLEENE_STAR:
                case KLEENE_PLUS: {
                    TreeNode node = TreeNode.join(leftNode, TreeNode.nodeFor(opIndex.op, new Range(opIndex.index)), c.incrementAndGet());
                    nodes.put(node.getRange(), node);
                    break;
                }
                case CONCATENATION: {
                    TreeNode node = TreeNode.join(leftNode, rightNode(tokens, nodes, opIndex, c), c.incrementAndGet());
                    nodes.put(node.getRange(), node);
                    break;
                }
                case ALTERNATION: {
                    TreeNode node = TreeNode.or(leftNode, rightNode(tokens, nodes, opIndex, c), c.incrementAndGet());
                    nodes.put(node.getRange(), node);
                    break;
                }
            }
        }
        return new ParseTree(
                nodes.isEmpty()
                        ? TreeNode.nodeFor(new Epsilon(), 0, new Range(0))
                        : nodes.values().iterator().next()
        );
    }

    private static TreeNode leftNode(List<Token> tokens, Map<Range, TreeNode> nodes, OpIndex opIndex, AtomicInteger c) {
        int index = opIndex.index;
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

    private static TreeNode rightNode(List<Token> tokens, Map<Range, TreeNode> nodes, OpIndex opIndex, AtomicInteger c) {
        int index = opIndex.index;
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
        char current;
        Character prev = null;
        for (int i = 0, n = buf.length(); i < n; i++) {
            current = buf.charAt(i);
            if (i == 0) {
                if (current == Operator.ALTERNATION.value()) {
                    tokens.add(new Epsilon());
                }
            } else if (prev == Parenthesis.LEFT.value() || prev == Operator.ALTERNATION.value()) {
                if (current == Operator.ALTERNATION.value() || current == Parenthesis.RIGHT.value()) {
                    tokens.add(new Epsilon());
                }
            } else if (!Operator.isOperator(current) && current != Parenthesis.RIGHT.value()) {
                tokens.add(new OperatorToken(Operator.CONCATENATION));
            }
            Operator op;
            Parenthesis p;
            if (current == EscapeCharacter.SYMBOL) {
                tokens.add(new EscapeCharacter());
            } else if ((op = Operator.find(current)) != null) {
                tokens.add(new OperatorToken(op));
                if (i == n - 1 && op == Operator.ALTERNATION) {
                    tokens.add(new Epsilon());
                }
            } else if ((p = Parenthesis.find(current)) != null) {
                tokens.add(p == Parenthesis.LEFT ? new LeftParenthesis() : new RightParenthesis());
            } else {
                tokens.add(new CharToken(current));
            }
            prev = current;
        }
        return tokens;
    }

    private static void normalize(List<Token> tokens) {
        { /* apply escape characters */
            int index;
            do {
                index = -1;
                for (int i = 0, tokensSize = tokens.size(); i < tokensSize; i++) {
                    Token token = tokens.get(i);
                    if (token instanceof EscapeCharacter) {
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
            for (int i = 0, tokensSize = tokens.size(); i < tokensSize; i++) {
                Token token = tokens.get(i);
                if (token instanceof OperatorToken) {
                    Operator op = ((OperatorToken) token).getOperator();
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
        for (int i = 0; i < tokens.size(); i++) {
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
                    if (higherLevelSlices != null) {
                        while (!higherLevelSlices.isEmpty()) {
                            nextSlice.getChildren().add(higherLevelSlices.pop());
                        }
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
