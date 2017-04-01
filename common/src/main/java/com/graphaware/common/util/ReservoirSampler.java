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

package com.graphaware.common.util;

import java.util.Random;
import java.util.Vector;

/**
 * Sampler that uses the reservoir sampling algorithm, which is useful when the total number of items from which the sample
 * is being taken is unknown in advance.
 *
 * @param <T> type of sampled item.
 */
public class ReservoirSampler<T> {

    private final Vector<T> reservoir = new Vector<>();
    private final int numberOfSamples;
    private final Random random = new Random();
    private int numberOfItemsSeen = 0;

    /**
     * Create a new sampler with a certain reservoir size.
     *
     * @param numberOfSamples Maximum number of samples to retain in the reservoir. Must be positive.
     */
    public ReservoirSampler(int numberOfSamples) {
        if (numberOfSamples <= 0) {
            throw new IllegalArgumentException("Reservoir must be bigger than 0");
        }
        this.numberOfSamples = numberOfSamples;
    }

    /**
     * Sample an item and store in the reservoir if needed.
     *
     * @param item The item to sample. Must not be null.
     */
    public void sample(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Item to random must not be null");
        }

        if (reservoir.size() < numberOfSamples) {
            reservoir.add(item);
        } else {
            int index = random.nextInt(numberOfItemsSeen + 1);
            if (index < numberOfSamples) {
                reservoir.set(index, item);
            }
        }
        numberOfItemsSeen++;
    }

    /**
     * Get samples collected in the reservoir.
     *
     * @return A list of the samples.
     */
    public Iterable<T> getSamples() {
        return reservoir;
    }

    /**
     * Is the reservoir empty?
     *
     * @return true iff empty.
     */
    public boolean isEmpty() {
        return reservoir.isEmpty();
    }
}
