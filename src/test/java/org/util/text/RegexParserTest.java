package org.util.text;

import org.util.text.regex.ParseTree;
import org.util.text.regex.Parser;

/**
 * @author ahmad
 */
public class RegexParserTest {

    public static void main(String[] args) {
        String regex = "£*(ab+|ca*(€$+|a)+c)";
        ParseTree tree = Parser.parse(regex);
        System.out.println(tree);
    }

}
