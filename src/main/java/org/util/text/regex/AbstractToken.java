package org.util.text.regex;

/**
 * @author ahmad
 */
abstract class AbstractToken implements Token {

    @Override
    public String toString() {
        return String.valueOf(value());
    }

}
