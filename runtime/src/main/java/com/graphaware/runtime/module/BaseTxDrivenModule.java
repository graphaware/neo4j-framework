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

import com.graphaware.runtime.config.NullTxDrivenModuleConfiguration;
import com.graphaware.runtime.config.TxDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.TxDrivenModuleMetadata;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Base class for {@link TxDrivenModule} implementations.
 *
 * @param <T> The type of a state object that the module can use to
 *            pass information from the {@link #beforeCommit(com.graphaware.tx.event.improved.api.ImprovedTransactionData)}
 *            method to the {@link #afterCommit(Object)} method.
 */
public abstract class BaseTxDrivenModule<T> extends BaseRuntimeModule implements TxDrivenModule<T> {

    /**
     * Construct a new module.
     *
     * @param moduleId ID of this module. Must not be <code>null</code>.
     */
    protected BaseTxDrivenModule(String moduleId) {
        super(moduleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxDrivenModuleConfiguration getConfiguration() {
        return NullTxDrivenModuleConfiguration.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(GraphDatabaseService database) {
        //to be overridden
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that the implementation in this base class doesn't do anything and can be safely overridden without calling super.
     * </p>
     */
    @Override
    public void initialize(GraphDatabaseService database) {
        //to be overridden
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reinitialize(GraphDatabaseService database, TxDrivenModuleMetadata oldMetadata) {
        //to be overridden if re-initialisation differs from initialisation
        initialize(database);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that the implementation in this base class doesn't do anything and can be safely overridden without calling super.
     * </p>
     */
    @Override
    public void shutdown() {
        //to be overridden
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterCommit(T state) {
        //allow subclasses to override
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterRollback(T state) {
        //allow subclasses to override
    }
}
