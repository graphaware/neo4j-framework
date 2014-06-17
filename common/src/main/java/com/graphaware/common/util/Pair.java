package com.graphaware.common.util;

import java.util.Objects;

/**
 * An immutable pair of objects.
 *
 * @param <FIRST>  type of the first object in the pair.
 * @param <SECOND> type of the second object in the pair.
 */
public class Pair<FIRST, SECOND> {

    private final FIRST first;
    private final SECOND second;

    /**
     * Construct a new pair.
     *
     * @param first first element, can be null.
     * @param second second element, can be null.
     */
    public Pair(FIRST first, SECOND second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Return the first element of the pair.
     *
     * @return first element, can be null.
     */
    public FIRST first() {
        return first;
    }

    /**
     * Return the second element of the pair.
     *
     * @return second element, can be null.
     */
    public SECOND second() {
        return second;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (first != null ? !first.equals(pair.first) : pair.first != null) return false;
        if (second != null ? !second.equals(pair.second) : pair.second != null) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "(" + Objects.toString(first) + ", " + Objects.toString(second) + ")";
    }
}
