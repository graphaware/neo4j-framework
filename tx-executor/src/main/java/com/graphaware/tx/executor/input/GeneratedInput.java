/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.tx.executor.input;

import com.graphaware.tx.executor.batch.BatchTransactionExecutor;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A base class for {@link Iterable}s, items of which are generated on-demand. Intended to be used as input to
 * implementations of {@link BatchTransactionExecutor}.
 *
 * @param <T> type of the generated input.
 */
public abstract class GeneratedInput<T> implements Iterable<T>, Iterator<T> {

    private final int numberOfItems;
    private AtomicInteger generated = new AtomicInteger(0);

    /**
     * Construct a new generated input.
     *
     * @param numberOfItems the total number of input items that will ever be produced.
     */
    protected GeneratedInput(int numberOfItems) {
        this.numberOfItems = numberOfItems;
    }

    /**
     * Generate a new input item.
     *
     * @return the new item.
     */
    protected abstract T generate();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return generated.get() < numberOfItems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T next() {
        if (generated.incrementAndGet() > numberOfItems) {
            throw new NoSuchElementException("More items requested than the generator was set up to generate.");
        }

        return generate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("GeneratedInput does not support removal. It has nothing to remove!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator() {
        return this;
    }
}
