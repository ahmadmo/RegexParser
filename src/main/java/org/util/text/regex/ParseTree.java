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
            sb.append(indent).append("|\n");
        }
        sb.append(indent).append("+-: ").append(node.getLabel()).append('\n');
        indent += lastChild ? "    " : "|   ";
        List<TreeNode> children = node.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            prettyPrint(sb, children.get(i), indent, false, i == 0);
        }
    }

}
