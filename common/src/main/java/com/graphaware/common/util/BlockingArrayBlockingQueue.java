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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * {@link ArrayBlockingQueue} that blocks for a specified amount of time on {@link java.util.Queue#offer(Object)}, throwing
 * a {@link RuntimeException} upon timeout. The timeout is 30 minutes by default and can be changed by overriding
 * {@link #timeoutMinutes()}.
 *
 * @param <E>
 */
public class BlockingArrayBlockingQueue<E> extends ArrayBlockingQueue<E> {

    public BlockingArrayBlockingQueue(int capacity) {
        super(capacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean offer(E e) {
        try {
            return offer(e, timeoutMinutes(), TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @return number of minutes to block on offer, before throwing a {@link RuntimeException}.
     */
    protected int timeoutMinutes() {
        return 30;
    }
}

