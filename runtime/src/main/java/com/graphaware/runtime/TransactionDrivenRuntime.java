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

package com.graphaware.runtime;

import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.manager.TransactionDrivenModuleManager;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.module.TransactionDrivenRuntimeModule;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.ErrorState;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

import java.util.Set;

/**
 * {@link BaseGraphAwareRuntime} that registers itself as a Neo4j {@link org.neo4j.graphdb.event.TransactionEventHandler},
 * translates {@link org.neo4j.graphdb.event.TransactionData} into {@link com.graphaware.tx.event.improved.api.ImprovedTransactionData}
 * and lets registered {@link com.graphaware.runtime.module.TransactionDrivenRuntimeModule}s deal with the data before each transaction
 * commits, in the order the modules were registered.
 *
 * @param <T> implementation of {@link TransactionDrivenRuntimeModule} that this runtime supports.
 */
public abstract class TransactionDrivenRuntime<T extends TransactionDrivenRuntimeModule> extends BaseGraphAwareRuntime implements TransactionEventHandler<Void> {

    private final TransactionDrivenModuleManager<T> transactionDrivenModuleManager;

    /**
     * Create a new instance of the runtime with {@link com.graphaware.runtime.config.DefaultRuntimeConfiguration}.
     */
    protected TransactionDrivenRuntime(TransactionDrivenModuleManager<T> transactionDrivenModuleManager) {
        this(DefaultRuntimeConfiguration.getInstance(), transactionDrivenModuleManager);
    }

    protected TransactionDrivenRuntime(RuntimeConfiguration configuration, TransactionDrivenModuleManager<T> transactionDrivenModuleManager) {
        super(configuration);
        this.transactionDrivenModuleManager = transactionDrivenModuleManager;
    }


    protected abstract Class<T> supportedModule();

    protected void doRegisterModule(RuntimeModule module) {
        if (supportedModule().isAssignableFrom(module.getClass())) {
            transactionDrivenModuleManager.registerModule((T) module);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Void beforeCommit(TransactionData data) throws Exception {
        if (!makeSureIsStarted()) {
            return null;
        }

        transactionDrivenModuleManager.throwExceptionIfIllegal(data);

        transactionDrivenModuleManager.beforeCommit(new LazyTransactionData(data));

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void afterCommit(TransactionData data, Void state) {
        //do nothing for now
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void afterRollback(TransactionData data, Void state) {
        //do nothing for now
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<String> initializeModules() {
        return transactionDrivenModuleManager.initializeModules();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeUnusedModules(Set<String> usedModules) {
        transactionDrivenModuleManager.performCleanup(usedModules);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void shutdownModules() {
        transactionDrivenModuleManager.stopModules();
    }
}
