package org.util.text.regex;

import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * @author ahmad
 */
final class SyntaxValidator {

    private SyntaxValidator() {
    }

    static void validate(List<Token> tokens) {
        { /* apply escape characters */
            int index;
            do {
                index = -1;
                for (int i = 0, n = tokens.size(); i < n && index == -1; i++) {
                    if (tokens.get(i) instanceof EscapeCharacter) {
                        index = i;
                    }
                }
                if (index != -1) {
                    if (index == tokens.size() - 1) {
                        syntaxException("Illegal/Unsupported escape sequence", tokens, index + 1);
                    }
                    tokens.remove(index);
                    tokens.set(index, new CharToken(tokens.get(index).value()));
                }
            } while (index != -1);
        }
        { /* check parentheses */
            int level = 0;
            for (int i = 0, n = tokens.size(); i < n; i++) {
                Token token = tokens.get(i);
                if (token instanceof LeftParenthesis) {
                    ++level;
                } else if (token instanceof RightParenthesis) {
                    --level;
                    if (level < 0) {
                        syntaxException("Unmatched closing \')\'", tokens, i - 1);
                    }
                }
            }
            if (level != 0) {
                syntaxException("Unclosed group", tokens, tokens.size());
            }
        }
        { /* detect dangling meta-characters */
            Token next, prev;
            for (int i = 0, n = tokens.size(); i < n; i++) {
                next = tokens.get(i);
                if (next instanceof OperatorToken) {
                    Operator op = ((OperatorToken) next).getOperator();
                    switch (op) {
                        case KLEENE_STAR:
                            if (i == 0 || !((prev = tokens.get(i - 1)) instanceof CharToken) && !(prev instanceof RightParenthesis)) {
                                syntaxException("Dangling meta-character \'*\'", tokens, i);
                            }
                            break;
                        case KLEENE_PLUS:
                            if (i == 0 || !((prev = tokens.get(i - 1)) instanceof CharToken)
                                    && !(prev instanceof RightParenthesis)
                                    && !(OperatorToken.test(prev, Operator.KLEENE_STAR))
                                    && !(OperatorToken.test(prev, Operator.KLEENE_PLUS))) {
                                syntaxException("Dangling meta-character \'+\'", tokens, i);
                            }
                            break;
                    }
                }
            }
        }
        { /* add Epsilon & Concatenation tokens */
            int index;
            Token toBeAdded;
            Token next, prev;
            do {
                index = -1;
                toBeAdded = null;
                prev = null;
                for (int i = 0, n = tokens.size(); i < n && index == -1; i++) {
                    next = tokens.get(i);
                    if (i == 0) {
                        if (OperatorToken.test(next, Operator.ALTERNATION)) {
                            index = i;
                            toBeAdded = new Epsilon();
                        }
                    } else if (i == n - 1) {
                        if (OperatorToken.test(next, Operator.ALTERNATION)) {
                            index = i + 1;
                            toBeAdded = new Epsilon();
                        } else if (next instanceof RightParenthesis
                                && (prev instanceof LeftParenthesis || OperatorToken.test(prev, Operator.ALTERNATION))) {
                            index = i;
                            toBeAdded = new Epsilon();
                        }
                    } else if (prev instanceof LeftParenthesis || OperatorToken.test(prev, Operator.ALTERNATION)) {
                        if (OperatorToken.test(next, Operator.ALTERNATION) || next instanceof RightParenthesis) {
                            index = i;
                            toBeAdded = new Epsilon();
                        }
                    } else if (!(next instanceof OperatorToken) && !(next instanceof RightParenthesis)
                            && (prev instanceof CharToken || prev instanceof RightParenthesis
                            || OperatorToken.test(prev, Operator.KLEENE_STAR) || OperatorToken.test(prev, Operator.KLEENE_PLUS))) {
                        index = i;
                        toBeAdded = new OperatorToken(Operator.CONCATENATION);
                    }
                    prev = next;
                }
                if (index != -1) {
                    tokens.add(index, toBeAdded);
                }
            } while (index != -1);
        }
        tokens.add(0, new LeftParenthesis());
        tokens.add(new RightParenthesis());
    }

    private static void syntaxException(String message, List<Token> tokens, int index) {
        StringBuilder sb = new StringBuilder();
        for (Token token : tokens) {
            sb.append(token.value());
        }
        throw new PatternSyntaxException(message, sb.toString(), index);
    }

}
