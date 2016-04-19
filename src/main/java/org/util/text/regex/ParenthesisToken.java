package org.util.text.regex;

/**
 * @author ahmad
 */
class ParenthesisToken extends AbstractToken {

    private final Parenthesis parenthesis;

    ParenthesisToken(Parenthesis parenthesis) {
        this.parenthesis = parenthesis;
    }

    Parenthesis getParenthesis() {
        return parenthesis;
    }

    boolean isLeft() {
        return parenthesis == Parenthesis.LEFT;
    }

    boolean isRight() {
        return parenthesis == Parenthesis.RIGHT;
    }

    @Override
    public char value() {
        return parenthesis.value();
    }

}
