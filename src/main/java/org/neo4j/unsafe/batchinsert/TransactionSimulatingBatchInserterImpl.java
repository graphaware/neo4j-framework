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

import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import com.graphaware.tx.event.batch.data.BatchTransactionData;
import com.graphaware.tx.event.batch.propertycontainer.inserter.BatchInserterNode;
import com.graphaware.tx.event.batch.propertycontainer.inserter.BatchInserterRelationship;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.helpers.collection.PrefetchingIterator;
import org.neo4j.kernel.impl.nioneo.store.FileSystemAbstraction;
import org.neo4j.kernel.impl.nioneo.store.NeoStore;

import java.lang.reflect.Field;
import java.util.*;

/**
 * {@link com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter} that simulates a transaction commit every {@link com.graphaware.tx.event.batch.data.BatchTransactionData#commitTxAfterMutations}
 * mutations. It is a decorator for a real {@link org.neo4j.unsafe.batchinsert.BatchInserter}.
 * <p/>
 *
 * @see org.neo4j.unsafe.batchinsert.BatchInserter - same limitations apply to this class.
 */
public class TransactionSimulatingBatchInserterImpl extends BatchInserterImpl implements TransactionSimulatingBatchInserter {

    private final BatchTransactionData transactionData;
    private final List<KernelEventHandler> kernelEventHandlers = new LinkedList<>();

    /**
     * Construct a new inserter.
     *
     * @param storeDir database directory.
     */
    public TransactionSimulatingBatchInserterImpl(String storeDir) {
        super(storeDir);
        this.transactionData = new BatchTransactionData();
    }

    /**
     * Construct a new inserter.
     *
     * @param storeDir     database directory.
     * @param stringParams config parameters.
     */
    public TransactionSimulatingBatchInserterImpl(String storeDir, Map<String, String> stringParams) {
        super(storeDir, stringParams);
        this.transactionData = new BatchTransactionData();
    }

    /**
     * Construct a new inserter.
     *
     * @param storeDir     database directory.
     * @param fileSystem   file system.
     * @param stringParams config parameters.
     */
    public TransactionSimulatingBatchInserterImpl(String storeDir, FileSystemAbstraction fileSystem, Map<String, String> stringParams) {
        super(storeDir, fileSystem, stringParams);
        this.transactionData = new BatchTransactionData();
    }

    /**
     * Construct a new inserter.
     *
     * @param storeDir               database directory.
     * @param commitTxAfterMutations how many mutations it should take before a commit is simulated.
     */
    public TransactionSimulatingBatchInserterImpl(String storeDir, int commitTxAfterMutations) {
        super(storeDir);
        this.transactionData = new BatchTransactionData(commitTxAfterMutations);
    }

    /**
     * Construct a new inserter.
     *
     * @param storeDir               database directory.
     * @param stringParams           config parameters.
     * @param commitTxAfterMutations how many mutations it should take before a commit is simulated.
     */
    public TransactionSimulatingBatchInserterImpl(String storeDir, Map<String, String> stringParams, int commitTxAfterMutations) {
        super(storeDir, stringParams);
        this.transactionData = new BatchTransactionData(commitTxAfterMutations);
    }

    /**
     * Construct a new inserter.
     *
     * @param storeDir               database directory.
     * @param fileSystem             file system.
     * @param stringParams           config parameters.
     * @param commitTxAfterMutations how many mutations it should take before a commit is simulated.
     */
    public TransactionSimulatingBatchInserterImpl(String storeDir, FileSystemAbstraction fileSystem, Map<String, String> stringParams, int commitTxAfterMutations) {
        super(storeDir, fileSystem, stringParams);
        this.transactionData = new BatchTransactionData(commitTxAfterMutations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerTransactionEventHandler(TransactionEventHandler handler) {
        transactionData.registerTransactionEventHandler(handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerKernelEventHandler(KernelEventHandler handler) {
        if (!kernelEventHandlers.contains(handler)) {
            kernelEventHandlers.add(handler);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long createNode(Map<String, Object> properties) {
        long nodeId = super.createNode(properties);
        transactionData.nodeCreated(nodeById(nodeId));
        return nodeId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNodeProperties(long nodeId, Map<String, Object> properties) {
        Set<String> removedProperties = super.getNodeProperties(nodeId).keySet();

        Node node = nodeById(nodeId);

        for (String key : removedProperties) {
            transactionData.nodePropertyToBeRemoved(node, key);
        }

        for (String key : properties.keySet()) {
            transactionData.nodePropertyToBeSet(node, key, properties.get(key));
        }

        super.setNodeProperties(nodeId, properties);

        for (String key : removedProperties) {
            transactionData.nodePropertyRemoved(node, key);
        }

        for (String key : properties.keySet()) {
            transactionData.nodePropertySet(node, key, properties.get(key));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNodeProperty(long nodeId, String propertyName, Object propertyValue) {
        transactionData.nodePropertyToBeSet(nodeById(nodeId), propertyName, propertyValue);
        super.setNodeProperty(nodeId, propertyName, propertyValue);
        transactionData.nodePropertySet(nodeById(nodeId), propertyName, propertyValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRelationshipProperty(long relationshipId, String propertyName, Object propertyValue) {
        transactionData.relationshipPropertyToBeSet(relationshipById(relationshipId), propertyName, propertyValue);
        super.setRelationshipProperty(relationshipId, propertyName, propertyValue);
        transactionData.relationshipPropertySet(relationshipById(relationshipId), propertyName, propertyValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNode(long nodeId, Map<String, Object> properties) {
        super.createNode(nodeId, properties);
        transactionData.nodeCreated(nodeById(nodeId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long createRelationship(long node1, long node2, RelationshipType type, Map<String, Object> properties) {
        long relationshipId = super.createRelationship(node1, node2, type, properties);
        transactionData.relationshipCreated(relationshipById(relationshipId));
        return relationshipId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRelationshipProperties(long relationshipId, Map<String, Object> properties) {
        Set<String> removedProperties = super.getRelationshipProperties(relationshipId).keySet();

        Relationship relationship = relationshipById(relationshipId);

        for (String key : removedProperties) {
            transactionData.relationshipPropertyToBeRemoved(relationship, key);
        }

        for (String key : properties.keySet()) {
            transactionData.relationshipPropertyToBeSet(relationship, key, properties.get(key));
        }

        super.setRelationshipProperties(relationshipId, properties);

        for (String key : removedProperties) {
            transactionData.relationshipPropertyRemoved(relationship, key);
        }

        for (String key : properties.keySet()) {
            transactionData.relationshipPropertySet(relationship, key, properties.get(key));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNodeProperty(long nodeId, String property) {
        transactionData.nodePropertyToBeRemoved(nodeById(nodeId), property);
        super.removeNodeProperty(nodeId, property);
        transactionData.nodePropertyRemoved(nodeById(nodeId), property);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeRelationshipProperty(long relationshipId, String property) {
        transactionData.relationshipPropertyToBeRemoved(relationshipById(relationshipId), property);
        super.removeRelationshipProperty(relationshipId, property);
        transactionData.relationshipPropertyRemoved(relationshipById(relationshipId), property);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        transactionData.simulateCommit();

        for (KernelEventHandler handler : kernelEventHandlers) {
            handler.beforeShutdown();
        }

        super.shutdown();
    }

    /**
     * Get a {@link org.neo4j.graphdb.Node} object by its ID from the wrapped batch inserter.
     *
     * @param id of the node.
     * @return node as object.
     */
    private Node nodeById(long id) {
        return new BatchInserterNode(id, this);
    }

    /**
     * Get a {@link org.neo4j.graphdb.Relationship} object by its ID from the wrapped batch inserter.
     */
    private Relationship relationshipById(long id) {
        return new BatchInserterRelationship(super.getRelationshipById(id), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Long> getAllNodes() {
        long highId;

        //this is a nasty hack because the Neo4j API for this is quite closed:
        try {
            Field neoStoreField = BatchInserterImpl.class.getDeclaredField("neoStore");
            neoStoreField.setAccessible(true);
            NeoStore neoStore = (NeoStore) neoStoreField.get(this);
            highId = neoStore.getNodeStore().getHighId();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("No field named neoStore in BatchInserterImpl. This is a bug.");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal access to neoStore in BatchInserterImpl. This is a bug.");
        }

        return new AllNodeIdsIterator(highId);
    }

    private class AllNodeIdsIterator extends PrefetchingIterator<Long> implements Iterable<Long> {

        private final long highId;
        private long lastId = 0;

        private AllNodeIdsIterator(long highId) {
            this.highId = highId;
        }

        @Override
        public Iterator<Long> iterator() {
            return this;
        }

        @Override
        protected Long fetchNextOrNull() {
            while (++lastId <= highId) {
                if (nodeExists(lastId)) {
                    return lastId;
                }
            }

            return null;
        }
    }
}
