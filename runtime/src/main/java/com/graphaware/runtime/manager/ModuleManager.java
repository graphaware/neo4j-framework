/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime.manager;

import com.graphaware.runtime.module.Module;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;

import java.util.Map;

/**
 * A manager of {@link Module}s, which takes care of their lifecycle.
 */
public interface ModuleManager {

    /**
     * Check that the given module isn't already registered with the manager.
     *
     * @param module to check.
     * @throws IllegalStateException in case the module is already registered.
     */
    void checkNotAlreadyRegistered(Module<?> module);

    /**
     * Register a module with this manager.
     *
     * @param module to register.
     * @throws IllegalStateException in case the module is already registered.
     */
    void registerModule(Module<?> module);

    /**
     * Get a module registered with the manager.
     *
     * @param moduleId module ID.
     * @param clazz    expected class of the module.
     * @param <M>      type of the class above.
     * @return module, <code>null</code> if no such module exists.
     */
    <M extends Module<?>> M getModule(String moduleId, Class<M> clazz);

    /**
     * Get a module registered with the manager.
     *
     * @param clazz class of the module.
     * @param <M>   type of the class above.
     * @return module. <code>null</code> if no such module exists.
     * @throws IllegalStateException if more than one module of the same type has been registered.
     */
    <M extends Module<?>> M getModule(Class<M> clazz);

    /**
     * Perform work needed to make modules start doing their job. Called exactly once each time the database is started.
     */
    void startModules();

    /**
     * Bring all modules to an orderly shutdown, when the database is stopped.
     */
    void stopModules();

    /**
     * Delegate work to modules before a transaction is committed.
     *
     * @param transactionData about-to-be-committed transaction data.
     * @return map of objects (states) returned by the modules, keyed by {@link Module#getId()}.
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
