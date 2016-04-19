package org.util.text.regex;

/**
 * @author ahmad
 */
final class OperatorToken extends AbstractToken {

    private final Operator operator;

    OperatorToken(Operator operator) {
        this.operator = operator;
    }

    Operator getOperator() {
        return operator;
    }

    @Override
    public char value() {
        return operator.value();
    }

    static boolean test(Token token, Operator op) {
        return token instanceof OperatorToken && ((OperatorToken) token).getOperator() == op;
    }

}
