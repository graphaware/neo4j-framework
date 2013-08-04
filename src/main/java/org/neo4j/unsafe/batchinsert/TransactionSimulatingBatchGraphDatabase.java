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

package org.neo4j.unsafe.batchinsert;

import com.graphaware.tx.event.batch.data.BatchTransactionData;
import com.graphaware.tx.event.batch.propertycontainer.database.BatchDatabaseNode;
import com.graphaware.tx.event.batch.propertycontainer.database.BatchDatabaseRelationship;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.helpers.collection.PrefetchingIterator;
import org.neo4j.kernel.impl.nioneo.store.FileSystemAbstraction;
import org.neo4j.kernel.impl.nioneo.store.NeoStore;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A {@link org.neo4j.unsafe.batchinsert.BatchGraphDatabaseImpl} that can produce {@link org.neo4j.graphdb.event.TransactionData} despite the fact
 * that there are no transactions involved. Therefore, {@link org.neo4j.graphdb.event.TransactionEventHandler} can be registered on it, using
 * {@link #registerTransactionEventHandler(org.neo4j.graphdb.event.TransactionEventHandler)}.
 * <p/>
 * By default, a transaction commit is simulated every {@link com.graphaware.tx.event.batch.data.BatchTransactionData#COMMIT_TX_AFTER_MUTATIONS} mutations,
 * but this can be changed by constructing this object using one of the appropriate constructors.
 * <p/>
 * This is a hacky extension of {@link org.neo4j.unsafe.batchinsert.BatchGraphDatabaseImpl}, which isn't really maintained by Neo4j as it is "fake"
 * and doesn't, for example, take indices into account. Thus, it is preferable to use {@link com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter}.
 */
public class TransactionSimulatingBatchGraphDatabase extends BatchGraphDatabaseImpl {

    private final BatchTransactionData batchTransactionData;
    private final List<KernelEventHandler> kernelEventHandlers = new LinkedList<>();

    public TransactionSimulatingBatchGraphDatabase(String storeDir) {
        super(storeDir);
        batchTransactionData = new BatchTransactionData();
    }

    public TransactionSimulatingBatchGraphDatabase(String storeDir, Map<String, String> stringParams) {
        super(storeDir, stringParams);
        batchTransactionData = new BatchTransactionData();
    }

    public TransactionSimulatingBatchGraphDatabase(String storeDir, FileSystemAbstraction fileSystem, Map<String, String> stringParams) {
        super(storeDir, fileSystem, stringParams);
        batchTransactionData = new BatchTransactionData();
    }

    public TransactionSimulatingBatchGraphDatabase(BatchInserter batchInserter) {
        super(batchInserter);
        batchTransactionData = new BatchTransactionData();
    }

    public TransactionSimulatingBatchGraphDatabase(String storeDir, int commitTxAfterMutations) {
        super(storeDir);
        batchTransactionData = new BatchTransactionData(commitTxAfterMutations);
    }

    public TransactionSimulatingBatchGraphDatabase(String storeDir, Map<String, String> stringParams, int commitTxAfterMutations) {
        super(storeDir, stringParams);
        batchTransactionData = new BatchTransactionData(commitTxAfterMutations);
    }

    public TransactionSimulatingBatchGraphDatabase(String storeDir, FileSystemAbstraction fileSystem, Map<String, String> stringParams, int commitTxAfterMutations) {
        super(storeDir, fileSystem, stringParams);
        batchTransactionData = new BatchTransactionData(commitTxAfterMutations);
    }

    public TransactionSimulatingBatchGraphDatabase(BatchInserter batchInserter, int commitTxAfterMutations) {
        super(batchInserter);
        batchTransactionData = new BatchTransactionData(commitTxAfterMutations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> TransactionEventHandler<T> registerTransactionEventHandler(TransactionEventHandler<T> handler) {
        batchTransactionData.registerTransactionEventHandler(handler);
        return handler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KernelEventHandler registerKernelEventHandler(KernelEventHandler handler) {
        if (!kernelEventHandlers.contains(handler)) {
            kernelEventHandlers.add(handler);
        }
        return handler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node createNode() {
        BatchDatabaseNode result = new BatchDatabaseNode(super.createNode().getId(), this, batchTransactionData);
        batchTransactionData.nodeCreated(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getNodeById(long id) {
        return new BatchDatabaseNode(id, this, batchTransactionData);
    }

    /**
     * Get a node by its ID from the superclass.
     *
     * @param id of the node to get.
     * @return Node with the given ID.
     */
    public Node getNodeByIdInternal(long id) {
        return super.getNodeById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getReferenceNode() {
        return new BatchDatabaseNode(super.getReferenceNode().getId(), this, batchTransactionData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Node> getAllNodes() {
        long highId;

        //this is a nasty hack because the Neo4j API for this is quite closed:
        try {
            Field neoStoreField = BatchInserterImpl.class.getDeclaredField("neoStore");
            neoStoreField.setAccessible(true);
            NeoStore neoStore = (NeoStore) neoStoreField.get(batchInserter);
            highId = neoStore.getNodeStore().getHighId();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("No field named neoStore in BatchInserterImpl. This is a bug.");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal access to neoStore in BatchInserterImpl. This is a bug.");
        }

        return new AllNodesIterator(highId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Relationship getRelationshipById(long id) {
        return new BatchDatabaseRelationship(id, this, batchTransactionData);
    }

    /**
     * Get a relationship by its ID from the superclass.
     *
     * @param id of the relationship to get.
     * @return Relationship with the given ID.
     */
    public Relationship getRelationshipByIdInternal(long id) {
        return super.getRelationshipById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        batchTransactionData.simulateCommit();

        for (KernelEventHandler handler : kernelEventHandlers) {
            handler.beforeShutdown();
        }

        super.shutdown();
    }

    private class AllNodesIterator extends PrefetchingIterator<Node> implements Iterable<Node> {

        private final long highId;
        private long lastId = 0;

        private AllNodesIterator(long highId) {
            this.highId = highId;
        }

        @Override
        public Iterator<Node> iterator() {
            return this;
        }

        @Override
        protected Node fetchNextOrNull() {
            while (++lastId <= highId) {
                try {
                    getNodeByIdInternal(lastId);
                    return getNodeById(lastId);
                } catch (NotFoundException e) {
                    //continue
                }
            }

            return null;
        }
    }
}
