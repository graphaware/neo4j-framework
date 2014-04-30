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

import com.graphaware.common.wrapper.Wrapper;
import com.graphaware.tx.event.batch.data.BatchTransactionData;
import com.graphaware.tx.event.batch.propertycontainer.database.BatchDatabaseNode;
import com.graphaware.tx.event.batch.propertycontainer.database.BatchDatabaseRelationship;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.graphdb.traversal.BidirectionalTraversalDescription;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.helpers.collection.PrefetchingIterator;
import org.neo4j.kernel.impl.nioneo.store.NeoStore;
import org.neo4j.kernel.impl.traversal.MonoDirectionalTraversalDescription;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link org.neo4j.unsafe.batchinsert.BatchGraphDatabaseImpl} that can produce {@link org.neo4j.graphdb.event.TransactionData} despite the fact
 * that there are no transactions involved. Therefore, {@link org.neo4j.graphdb.event.TransactionEventHandler} can be registered on it, using
 * {@link #registerTransactionEventHandler(org.neo4j.graphdb.event.TransactionEventHandler)}.
 * <p/>
 * By default, a transaction commit is simulated every {@link com.graphaware.tx.event.batch.data.BatchTransactionData#COMMIT_TX_AFTER_MUTATIONS} mutations,
 * but this can be changed by constructing this object using one of the appropriate constructors.
 * <p/>
 * This is a hacky decorator of {@link org.neo4j.unsafe.batchinsert.BatchGraphDatabaseImpl}, which isn't really maintained by Neo4j as it is "fake"
 * and doesn't, for example, take indices into account. Thus, it is preferable to use {@link com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter}.
 */
public class TransactionSimulatingBatchGraphDatabase implements GraphDatabaseService, Wrapper<GraphDatabaseService> {

    private final BatchTransactionData batchTransactionData;
    private final List<KernelEventHandler> kernelEventHandlers = new LinkedList<>();
    private final GraphDatabaseService wrapped;

    public TransactionSimulatingBatchGraphDatabase(GraphDatabaseService database) {
        if (!(database instanceof BatchGraphDatabaseImpl)) {
            throw new IllegalArgumentException("This wrapper is only intended for BatchGraphDatabaseImpl");
        }
        this.wrapped = database;
        batchTransactionData = new BatchTransactionData();
    }

    public TransactionSimulatingBatchGraphDatabase(GraphDatabaseService database, int commitTxAfterMutations) {
        if (!(database instanceof BatchGraphDatabaseImpl)) {
            throw new IllegalArgumentException("This wrapper is only intended for BatchGraphDatabaseImpl");
        }
        this.wrapped = database;
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
    public <T> TransactionEventHandler<T> unregisterTransactionEventHandler(TransactionEventHandler<T> handler) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KernelEventHandler unregisterKernelEventHandler(KernelEventHandler handler) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node createNode() {
        BatchDatabaseNode result = new BatchDatabaseNode(wrapped.createNode().getId(), this, batchTransactionData);
        batchTransactionData.nodeCreated(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node createNode(Label... labels) {
        BatchDatabaseNode result = new BatchDatabaseNode(wrapped.createNode(labels).getId(), this, batchTransactionData);
        batchTransactionData.nodeCreated(result);
        batchTransactionData.nodeLabelsToBeSet(result, labels);
        batchTransactionData.nodeLabelsSet(result, labels);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getNodeById(long id) {
        return new BatchDatabaseNode(getNodeByIdInternal(id).getId(), this, batchTransactionData);
    }

    /**
     * Get a node by its ID from the superclass.
     *
     * @param id of the node to get.
     * @return Node with the given ID.
     */
    public Node getNodeByIdInternal(long id) {
        return wrapped.getNodeById(id);
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
            NeoStore neoStore = (NeoStore) neoStoreField.get(((BatchGraphDatabaseImpl) wrapped).batchInserter);
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
        return new BatchDatabaseRelationship(getRelationshipByIdInternal(id).getId(), this, batchTransactionData);
    }

    /**
     * Get a relationship by its ID from the superclass.
     *
     * @param id of the relationship to get.
     * @return Relationship with the given ID.
     */
    public Relationship getRelationshipByIdInternal(long id) {
        return wrapped.getRelationshipById(id);
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

        wrapped.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TraversalDescription traversalDescription() {
        return new MonoDirectionalTraversalDescription();
    }

    private class AllNodesIterator extends PrefetchingIterator<Node> implements Iterable<Node> {

        private final long highId;
        private long lastId = -1;

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

    //pure delegates

    @Override
    public ResourceIterable<Node> findNodesByLabelAndProperty(Label label, String key, Object value) {
        return wrapped.findNodesByLabelAndProperty(label, key, value);
    }

    @Override
    public Schema schema() {
        return wrapped.schema();
    }

    @Override
    public BidirectionalTraversalDescription bidirectionalTraversalDescription() {
        return wrapped.bidirectionalTraversalDescription();
    }

    @Override
    public Iterable<RelationshipType> getRelationshipTypes() {
        return wrapped.getRelationshipTypes();
    }

    @Override
    public Transaction beginTx() {
        return wrapped.beginTx();
    }

    @Override
    public IndexManager index() {
        return wrapped.index();
    }

    @Override
    public boolean isAvailable(long timeout) {
        return wrapped.isAvailable(timeout);
    }

    @Override
    public GraphDatabaseService getWrapped() {
        return wrapped;
    }
}
