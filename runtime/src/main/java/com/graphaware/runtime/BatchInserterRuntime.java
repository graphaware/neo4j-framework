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

import com.graphaware.common.util.FakeTransaction;
import com.graphaware.runtime.manager.TxDrivenModuleManager;
import com.graphaware.runtime.module.BatchSupportingTxDrivenModule;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import org.neo4j.graphdb.Transaction;


/**
 * {@link GraphAwareRuntime} that operates on a {@link org.neo4j.unsafe.batchinsert.BatchInserter}
 * (or more precisely {@link TransactionSimulatingBatchInserter}) rather than {@link org.neo4j.graphdb.GraphDatabaseService}.
 * <p/>
 * Only supports {@link com.graphaware.runtime.module.TxDrivenModule}, <b>does not support</b> {@link com.graphaware.runtime.module.TimerDrivenModule}s.
 *
 * @see BaseGraphAwareRuntime
 * @see org.neo4j.unsafe.batchinsert.BatchInserter - same limitations apply.
 */
public class BatchInserterRuntime extends TxDrivenRuntime<BatchSupportingTxDrivenModule> {

    private final TxDrivenModuleManager<BatchSupportingTxDrivenModule> txDrivenModuleManager;

    /**
     * Construct a new runtime. This method is protected, please use {@link GraphAwareRuntimeFactory}.
     *
     * @param batchInserter         against which this runtime runs.
     * @param txDrivenModuleManager module manager.
     */
    protected BatchInserterRuntime(TransactionSimulatingBatchInserter batchInserter, TxDrivenModuleManager<BatchSupportingTxDrivenModule> txDrivenModuleManager) {
        this.txDrivenModuleManager = txDrivenModuleManager;
        batchInserter.registerTransactionEventHandler(this);
        batchInserter.registerKernelEventHandler(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TxDrivenModuleManager<BatchSupportingTxDrivenModule> getTxDrivenModuleManager() {
        return txDrivenModuleManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doRegisterModule(RuntimeModule module) {
        if (module instanceof BatchSupportingTxDrivenModule) {
            txDrivenModuleManager.registerModule((BatchSupportingTxDrivenModule) module);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean databaseAvailable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Transaction startTransaction() {
        return new FakeTransaction();
    }
}
