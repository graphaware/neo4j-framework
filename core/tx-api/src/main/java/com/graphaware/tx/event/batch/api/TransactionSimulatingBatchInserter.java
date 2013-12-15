/*
 * Copyright (c) 2013 GraphAware
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

package com.graphaware.tx.event.batch.api;

import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.unsafe.batchinsert.BatchInserter;

/**
 * A {@link org.neo4j.unsafe.batchinsert.BatchInserter} that can produce {@link org.neo4j.graphdb.event.TransactionData}
 * despite the fact that there are no transactions involved. Therefore, {@link org.neo4j.graphdb.event.TransactionEventHandler}
 * can be registered on it. It is up to the implementations to decide when / how often to simulate a "transaction commit"
 * and call {@link org.neo4j.graphdb.event.TransactionEventHandler} methods.
 */
public interface TransactionSimulatingBatchInserter extends BatchInserter {

    /**
     * Registers {@code handler} as a handler for simulated transaction events. There is no guarantee when these simulated
     * transactions "commit", but they must do so at least once at the point where {@link #shutdown()} is called.
     *
     * @param handler the handler to receive events about simulated transactions.
     */
    void registerTransactionEventHandler(TransactionEventHandler handler);

    /**
     * Registers {@code handler} as a handler for simulated kernel events, e.g. shutdown.
     *
     * @param handler the handler to receive events about different states in the kernel lifecycle.
     */
    void registerKernelEventHandler(KernelEventHandler handler);

    /**
     * Returns IDs of all nodes in the graph.
     *
     * @return all IDs of nodes in the graph.
     */
    Iterable<Long> getAllNodes();
}
