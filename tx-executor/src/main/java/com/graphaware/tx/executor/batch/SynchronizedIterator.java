/*
 * Copyright (c) 2015 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

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
    public synchronized boolean hasNext() {
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
