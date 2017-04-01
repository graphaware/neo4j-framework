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

package com.graphaware.writer.thirdparty;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.writer.service.QueueBackedScheduledService;
import org.neo4j.logging.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract base-class for {@link ThirdPartyWriter} implementations, backed by a {@link QueueBackedScheduledService}.
 */
public abstract class BaseThirdPartyWriter extends QueueBackedScheduledService<Collection<WriteOperation<?>>> implements ThirdPartyWriter {

    private static final Log LOG = LoggerFactory.getLogger(BaseThirdPartyWriter.class);

    /**
     * Construct a new writer with a default queue capacity of {@link #DEFAULT_QUEUE_CAPACITY}.
     */
    protected BaseThirdPartyWriter() {
        super();
    }

    /**
     * Construct a new writer.
     *
     * @param queueCapacity capacity of the queue.
     */
    protected BaseThirdPartyWriter(int queueCapacity) {
        super(queueCapacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runOneIteration() throws Exception {
        if (queue.isEmpty()) {
            return;
        }

        List<Collection<WriteOperation<?>>> tasks = new LinkedList<>();
        queue.drainTo(tasks);

        processOperations(tasks);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final WriteOperation<?> operation, String id) {
        write(Collections.<WriteOperation<?>>singleton(operation), id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final Collection<WriteOperation<?>> operations, String id) {
        offer(operations, id);
    }

    /**
     * Offer a collection of {@link WriteOperation}s, typically all the operations performed within the context of a single
     * transaction, to the queue for processing. If the queue is full, the operations will be dropped and a warning logged.
     *
     * @param operations to process.
     * @param id         of the operations for logging purposes.
     */
    protected final void offer(final Collection<WriteOperation<?>> operations, String id) {
        if (!state().equals(State.NEW) && !state().equals(State.STARTING) && !state().equals(State.RUNNING)) {
            throw new IllegalStateException("Service is not running!");
        }

        if (!offer(operations)) {
            LOG.warn("Could not write task " + id + " to queue as it is too full. We're losing tasks now.");
        }
    }

    /**
     * Process the given operations that have been pulled from the queue, i.e. shoot them at the third-party system.
     * Implementations must handle failures scenarios, but the {@link #retry(List)} method is intended to help there.
     *
     * @param operations to be processed.
     */
    protected abstract void processOperations(List<Collection<WriteOperation<?>>> operations);

    /**
     * A convenience method for failure scenarios, which will insert the provided operations into the front of the queue.
     *
     * @param operations to retry.
     */
    protected final void retry(List<Collection<WriteOperation<?>>> operations) {
        Collections.reverse(operations);

        for (Collection<WriteOperation<?>> collection : operations) {
            if (!queue.offerFirst(collection)) {
                LOG.warn("Could not retry failed tasks as the queue is too full. We're losing tasks now.");
            }
        }
    }
}
