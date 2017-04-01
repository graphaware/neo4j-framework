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

package org.neo4j.server.rest.transactional;

import com.graphaware.server.tx.LongRunningTransaction;

import java.lang.reflect.Field;

/**
 * {@link com.graphaware.server.tx.LongRunningTransaction} constructed from a {@link org.neo4j.server.rest.transactional.TransactionHandle}.
 * <p/>
 * It must be in the same package as {@link TransitionalTxManagementKernelTransaction} is package protected, and even then,
 * a lot of ugly stuff is going on in the constructor.
 */
public class GraphAwareLongRunningTransaction implements LongRunningTransaction {

    private final TransactionHandle transactionHandle;
    private final TransitionalTxManagementKernelTransaction transaction;
    private final TransactionRegistry registry;
    private final long id;

    //This is very very dirty, but do we have a choice?
    public GraphAwareLongRunningTransaction(TransactionHandle transactionHandle) {
        this.transactionHandle = transactionHandle;
        try {
            Field contextField = transactionHandle.getClass().getDeclaredField("context");
            contextField.setAccessible(true);
            transaction = (TransitionalTxManagementKernelTransaction) contextField.get(transactionHandle);

            Field registryField = transactionHandle.getClass().getDeclaredField("registry");
            registryField.setAccessible(true);
            registry = (TransactionRegistry) registryField.get(transactionHandle);

            Field idField = transactionHandle.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            id = (long) idField.get(transactionHandle);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void resume() {
        transaction.resumeSinceTransactionsAreStillThreadBound();
    }

    @Override
    public void suspend() {
        transaction.suspendSinceTransactionsAreStillThreadBound();
        registry.release(id, transactionHandle);
    }
}
