package org.util.text.regex;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ahmad
 */
final class Slice implements Comparable<Slice> {

    private final Range range;
    private final List<Slice> children = new ArrayList<>();

    Slice(Range range) {
        this.range = range;
    }

    Range getRange() {
        return range;
    }

    List<Slice> getChildren() {
        return children;
    }

    private int weight() {
        int w = 1;
        for (Slice slice : children) {
            w += slice.weight();
        }
        return w;
    }

    @Override
    public int compareTo(Slice that) {
        return Integer.compare(weight(), that.weight());
    }

}
