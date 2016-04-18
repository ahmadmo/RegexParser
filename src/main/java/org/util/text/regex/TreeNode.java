package org.util.text.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author ahmad
 */
public final class TreeNode {

    private final String label;
    private final Range range;
    private final List<TreeNode> children = new ArrayList<>();

    private TreeNode(String label, Range range) {
        this.label = label;
        this.range = range;
    }

    public String getLabel() {
        return label;
    }

    Range getRange() {
        return range;
    }

    @Override
    public String toString() {
        return label;
    }

    public List<TreeNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    static TreeNode nodeFor(Token token, int n, Range range) {
        TreeNode node = new TreeNode(token.toString(), range);
        if (token instanceof CharToken) {
            TreeNode parent = new TreeNode("r" + n, range);
            parent.children.add(node);
            return parent;
        }
        return node;
    }

    static TreeNode nodeFor(Operator op, Range range) {
        return new TreeNode(op.toString(), range);
    }

    static TreeNode join(TreeNode left, TreeNode right, int n) {
        TreeNode parent = new TreeNode("r" + n, Range.join(left.getRange(), right.getRange()));
        parent.children.add(left);
        parent.children.add(right);
        return parent;
    }

    static TreeNode or(TreeNode left, TreeNode right, int n) {
        TreeNode parent = new TreeNode("r" + n, Range.join(left.getRange(), right.getRange()));
        parent.children.add(left);
        parent.children.add(new TreeNode(Operator.ALTERNATION.toString(), new Range(left.getRange().getEndExclusive())));
        parent.children.add(right);
        return parent;
    }

}