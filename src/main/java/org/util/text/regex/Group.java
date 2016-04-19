package org.util.text.regex;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ahmad
 */
final class Group implements Comparable<Group> {

    private final Range range;
    private final List<Group> children = new ArrayList<>();

    Group(Range range) {
        this.range = range;
    }

    Range getRange() {
        return range;
    }

    List<Group> getChildren() {
        return children;
    }

    private int weight() {
        int w = 1;
        for (Group group : children) {
            w += group.weight();
        }
        return w;
    }

    @Override
    public int compareTo(Group that) {
        return Integer.compare(weight(), that.weight());
    }

}
