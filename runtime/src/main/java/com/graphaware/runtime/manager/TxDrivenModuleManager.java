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

package com.graphaware.runtime.manager;

import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;

import java.util.Map;

/**
 * {@link ModuleManager} for {@link TxDrivenModule}s.
 */
public interface TxDrivenModuleManager<T extends TxDrivenModule> extends ModuleManager<T> {

    /**
     * Delegate work to modules before a transaction is committed.
     *
     * @param transactionData about-to-be-committed transaction data.
     * @return map of objects (states) returned by the modules, keyed by {@link com.graphaware.runtime.module.TxDrivenModule#getId()}.
     */
    Map<String, Object> beforeCommit(TransactionDataContainer transactionData);

    /**
     * Delegate work to modules after a transaction is committed.
     *
     * @param states returned by {@link #beforeCommit(com.graphaware.tx.event.improved.data.TransactionDataContainer)}.
     */
    void afterCommit(Map<String, Object> states);

    /**
     * Delegate work to modules after a transaction is rolled back.
     *
     * @param states returned by {@link #beforeCommit(com.graphaware.tx.event.improved.data.TransactionDataContainer)}.
     */
    void afterRollback(Map<String, Object> states);
}
