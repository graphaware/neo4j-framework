package com.graphaware.tx.executor.batch;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Wrapper of {@link Iterator} with synchronized access to the {@link #next()} method.
 */
public class SynchronizedIterator<T> implements Iterator<T> {

    private final Iterator<T> wrapped;

    /**
     * Construct a new iterator.
     *
     * @param wrapped iterator that will be delegated to.
     */
    public SynchronizedIterator(Iterator<T> wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return wrapped.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        return wrapped.next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Synchronized Iterator does not support removals");
    }
}
