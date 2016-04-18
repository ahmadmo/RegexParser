package org.util.text.regex;

/**
 * @author ahmad
 */
enum Operator {

    KLEENE_STAR('*', 1),
    KLEENE_PLUS('+', 1),
    CONCATENATION('\u0000', 2),
    ALTERNATION('|', 3);

    private final char value;
    private final int precedence;

    Operator(char value, int precedence) {
        this.value = value;
        this.precedence = precedence;
    }

    char value() {
        return value;
    }

    int precedence() {
        return precedence;
    }

    static Operator find(char ch) {
        for (Operator operator : values()) {
            if (operator != CONCATENATION && operator.value == ch) {
                return operator;
            }
        }
        return null;
    }

    static boolean isOperator(char ch) {
        return find(ch) != null;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
