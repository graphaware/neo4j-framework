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

package com.graphaware.runtime.module;

import com.graphaware.runtime.config.TxDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.TxDrivenModuleMetadata;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * A {@link RuntimeModule} module performing some useful work based on about-to-be-committed transaction data.
 *
 * @param <T> The type of a state object that the module can use to
 *            pass information from the {@link #beforeCommit(com.graphaware.tx.event.improved.api.ImprovedTransactionData)}
 *            method to the {@link #afterCommit(Object)} method.
 */
public interface TxDrivenModule<T> extends RuntimeModule {

    /**
     * Perform the core business logic of this module before a transaction commits. If the framework determines by
     * looking at {@link #getConfiguration()} that the module isn't interested in this transaction, this method will
     * not be called.
     * <p/>
     * Note that in case this method throws {@link RuntimeException} (including {@link NeedsInitializationException}),
     * {@link #afterCommit(Object)} will be called with a <code>null</code> argument.
     * <p/>
     * Note that in case this method throws {@link DeliberateTransactionRollbackException}, {@link #afterRollback(Object)}
     * will be called with a <code>null</code> argument.
     *
     * @param transactionData data about the soon-to-be-committed transaction. It is already filtered based on {@link #getConfiguration()}.
     * @return a state object (or <code>null</code>) that will be passed on to {@link #afterCommit(Object)} of this object. Only return <code>null</code> if you do nothing in {@link #afterCommit(Object)}.
     * @throws NeedsInitializationException           if it detects data is out of sync. {@link #initialize(org.neo4j.graphdb.GraphDatabaseService)}  will be called next
     *                                                time the {@link com.graphaware.runtime.GraphAwareRuntime} is started. Until then, the module
     *                                                should perform on best-effort basis.
     * @throws DeliberateTransactionRollbackException if the module wants to prevent the transaction from committing.
     */
    T beforeCommit(ImprovedTransactionData transactionData) throws DeliberateTransactionRollbackException;

    /**
     * Perform the core business logic of this module after a transaction commits.
     *
     * @param state returned by {@link #beforeCommit(com.graphaware.tx.event.improved.api.ImprovedTransactionData)}. Will
     *              be <code>null</code> if {@link #beforeCommit(com.graphaware.tx.event.improved.api.ImprovedTransactionData)}
     *              threw an exception but the transaction still committed. This is the case for all exceptions except {@link DeliberateTransactionRollbackException}.
     */
    void afterCommit(T state);

    /**
     * Cleanup if needed after a transaction rolled back. Will only be called if this module's {@link #beforeCommit(com.graphaware.tx.event.improved.api.ImprovedTransactionData)}
     * method was called.
     *
     * @param state returned by {@link #beforeCommit(com.graphaware.tx.event.improved.api.ImprovedTransactionData)}. Will
     *              be null if it is this module that caused the rollback by throwing {@link DeliberateTransactionRollbackException}.
     *              Will also be <code>null</code> if {@link #beforeCommit(com.graphaware.tx.event.improved.api.ImprovedTransactionData)} threw
     *              a non-rollback-causing exception, but some other module caused rollback after that.
     */
    void afterRollback(T state);

    /**
     * Return the configuration of this module. Each module must encapsulate its entire configuration in an instance of
     * a {@link com.graphaware.runtime.config.TxDrivenModuleConfiguration} implementation. Use {@link com.graphaware.runtime.config.NullTxDrivenModuleConfiguration}
     * if this module needs no configuration.
     *
     * @return module configuration.
     */
    TxDrivenModuleConfiguration getConfiguration();

    /**
     * Start the module. This method must bring the module to a usable state, i.e. after it returns, the module must be
     * able to handle multi-threaded requests. This method is guaranteed to be called exactly once every time the runtime/database
     * starts.
     *
     * @param database to start this module against.
     */
    void start(GraphDatabaseService database);

    /**
     * Initialize this module. This method must bring the module to a state equivalent to a state of the same module that
     * has been registered at all times since the database was empty. It can perform global-graph operations to achieve
     * this. It must manage its own transactions.
     * <p/>
     * For example, a module that performs some in-graph caching needs to write information into the graph so that when
     * the method returns, the graph is in the same state as it would be if the module has been running all the time
     * since the graph was empty.
     * <p/>
     *
     * @param database to initialize this module for.
     */
    void initialize(GraphDatabaseService database);

    /**
     * Re-initialize this module. This method must remove all metadata written to the graph by this module and bring the
     * module to a state equivalent to a state of the same module that has been registered at all times since the
     * database was empty. It can perform global-graph operations to achieve this. It must manage its own transactions.
     *
     * @param database    to re-initialize this module for.
     * @param oldMetadata metadata stored for this module from its previous run. Can be <code>null</code> in case metadata
     *                    was corrupt or there was no metadata.
     */
    void reinitialize(GraphDatabaseService database, TxDrivenModuleMetadata oldMetadata);

}
