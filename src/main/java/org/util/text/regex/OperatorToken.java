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
    public Character value() {
        return operator.value();
    }

}
