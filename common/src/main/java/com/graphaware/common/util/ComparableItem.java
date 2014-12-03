package com.graphaware.common.util;

import static org.springframework.util.Assert.notNull;

/**
 * A comparable item.
 * <p/>
 * Note that equality and hash code depend purely in the item, not on the quantity associated with it.
 *
 * @param <T> type of the item.
 * @param <C> type of the quantity associated with the item.
 */
public class ComparableItem<T, C extends Comparable<C>> implements Comparable<ComparableItem<T, C>> {

    private final T item;
    private final C quantity;

    /**
     * Construct a new comparable item.
     *
     * @param item     item, must not be null.
     * @param quantity quantity, must not be null.
     */
    public ComparableItem(T item, C quantity) {
        notNull(item);
        notNull(quantity);
        this.item = item;
        this.quantity = quantity;
    }

    public T getItem() {
        return item;
    }

    public C getQuantity() {
        return quantity;
    }

    @Override
    public int compareTo(ComparableItem<T, C> o) {
        return getQuantity().compareTo(o.getQuantity());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComparableItem that = (ComparableItem) o;

        if (!item.equals(that.item)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return item.hashCode();
    }
}