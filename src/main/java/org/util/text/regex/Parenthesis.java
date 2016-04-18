package org.util.text.regex;

/**
 * @author ahmad
 */
enum Parenthesis {

    LEFT('('), RIGHT(')');

    private final char value;

    Parenthesis(char value) {
        this.value = value;
    }

    char value() {
        return value;
    }

    static Parenthesis find(char ch) {
        return ch == LEFT.value ? LEFT : ch == RIGHT.value ? RIGHT : null;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
