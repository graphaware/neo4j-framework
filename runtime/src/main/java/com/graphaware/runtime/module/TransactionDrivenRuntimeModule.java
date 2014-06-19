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

package com.graphaware.runtime.module;

import com.graphaware.tx.event.improved.api.ImprovedTransactionData;

/**
 * A {@link com.graphaware.runtime.GraphAwareRuntime} module performing some useful work based on
 * about-to-be-committed transaction data.
 */
public interface TransactionDrivenRuntimeModule extends RuntimeModule {

    /**
     * Perform the core business logic of this module before a transaction commits.
     *
     * @param transactionData data about the soon-to-be-committed transaction. It is already filtered based on {@link #getConfiguration()}.
     * @throws com.graphaware.runtime.NeedsInitializationException
     *          if it detects data is out of sync. {@link #initialize(org.neo4j.graphdb.GraphDatabaseService)}  will be called next
     *          time the {@link com.graphaware.runtime.GraphAwareRuntime} is started. Until then, the module
     *          should perform on best-effort basis.
     */
    void beforeCommit(ImprovedTransactionData transactionData);
}
