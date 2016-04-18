package org.util.text.regex;

/**
 * @author ahmad
 */
final class Range {

    private final int startInclusive;
    private final int endExclusive;

    Range(int index) {
        this(index, index + 1);
    }

    Range(int startInclusive, int endExclusive) {
        if (startInclusive < 0) {
            throw new IndexOutOfBoundsException("startInclusive = " + startInclusive);
        }
        if (startInclusive > endExclusive) {
            throw new IllegalArgumentException("startInclusive(" + startInclusive + ") > endExclusive(" + endExclusive + ")");
        }
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
    }

    int getStartInclusive() {
        return startInclusive;
    }

    int getEndExclusive() {
        return endExclusive;
    }

    boolean isInRange(int index) {
        return index >= startInclusive && index < endExclusive;
    }

    boolean isOnBound(int index) {
        return index == startInclusive || index + 1 == endExclusive;
    }

    @Override
    public int hashCode() {
        return startInclusive * 31 + endExclusive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        Range that = (Range) o;
        return startInclusive == that.startInclusive && endExclusive == that.endExclusive;
    }

    @Override
    public String toString() {
        return "[" + startInclusive + ", " + endExclusive + ')';
    }

    static Range join(Range left, Range right) {
        return new Range(left.getStartInclusive(), right.getEndExclusive());
    }

}
