package org.util.text.regex;

/**
 * @author ahmad
 */
final class EscapeCharacter extends AbstractToken {

    static final char SYMBOL = '\\';

    @Override
    public char value() {
        return SYMBOL;
    }

}
