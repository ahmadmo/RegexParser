package org.util.text.regex;

import java.util.List;

/**
 * @author ahmad
 */
public final class ParseTree {

    private final TreeNode root;

    ParseTree(TreeNode root) {
        this.root = root;
    }

    public TreeNode getRoot() {
        return root;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        prettyPrint(sb, root, "", true, true);
        return sb.toString();
    }

    private static void prettyPrint(StringBuilder sb, TreeNode node, String indent, boolean root, boolean lastChild) {
        if (!root) {
            sb.append(indent).append('|').append(System.lineSeparator());
        }
        sb.append(indent).append("+-: ").append(node.getLabel()).append(System.lineSeparator());
        indent += lastChild ? "    " : "|   ";
        List<TreeNode> children = node.getChildren();
        for (int i = 0, childrenSize = children.size(); i < childrenSize; i++) {
            TreeNode t = children.get(i);
            prettyPrint(sb, t, indent, false, i == childrenSize - 1);
        }
    }

}
