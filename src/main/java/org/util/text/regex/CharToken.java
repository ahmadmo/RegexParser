package org.util.text.regex;

/**
 * @author ahmad
 */
final class CharToken extends AbstractToken {

    private final char value;

    CharToken(char value) {
        this.value = value;
    }

    @Override
    public Character value() {
        return value;
    }

}
