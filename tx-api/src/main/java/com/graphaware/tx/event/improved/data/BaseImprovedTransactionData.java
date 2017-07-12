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

package com.graphaware.tx.event.improved.data;

import com.graphaware.common.util.Change;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.TransactionData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.graphaware.common.util.PropertyContainerUtils.nodeToString;
import static com.graphaware.common.util.PropertyContainerUtils.relationshipToString;

/**
 * Base-class for {@link com.graphaware.tx.event.improved.api.ImprovedTransactionData} implementations that delegates all work to
 * {@link NodeTransactionData} and {@link RelationshipTransactionData} provided by subclasses.
 */
public abstract class BaseImprovedTransactionData {

    private final TransactionData wrapped;

    public BaseImprovedTransactionData(TransactionData wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * Get {@link NodeTransactionData} to delegate to.
     *
     * @return delegate.
     */
    protected abstract NodeTransactionData getNodeTransactionData();

    /**
     * Get {@link RelationshipTransactionData} to delegate to.
     *
     * @return delegate.
     */
    protected abstract RelationshipTransactionData getRelationshipTransactionData();

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#hasBeenCreated(org.neo4j.graphdb.Node)
     */
    public boolean hasBeenCreated(Node node) {
        return getNodeTransactionData().hasBeenCreated(node);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#getAllCreatedNodes()
     */
    public Collection<Node> getAllCreatedNodes() {
        return getNodeTransactionData().getAllCreated();
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#hasBeenDeleted(org.neo4j.graphdb.Node)
     */
    public boolean hasBeenDeleted(Node node) {
        return getNodeTransactionData().hasBeenDeleted(node);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#getDeleted(org.neo4j.graphdb.Node)
     */
    public Node getDeleted(Node node) {
        return getNodeTransactionData().getDeleted(node);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#getAllDeletedNodes()
     */
    public Collection<Node> getAllDeletedNodes() {
        return getNodeTransactionData().getAllDeleted();
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#hasBeenChanged(org.neo4j.graphdb.Node)
     */
    public boolean hasBeenChanged(Node node) {
        return getNodeTransactionData().hasBeenChanged(node);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#getChanged(org.neo4j.graphdb.Node)
     */
    public Change<Node> getChanged(Node node) {
        return getNodeTransactionData().getChanged(node);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#getAllChangedNodes()
     */
    public Collection<Change<Node>> getAllChangedNodes() {
        return getNodeTransactionData().getAllChanged();
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#hasPropertyBeenCreated(org.neo4j.graphdb.Node, String)
     */
    public boolean hasPropertyBeenCreated(Node node, String key) {
        return getNodeTransactionData().hasPropertyBeenCreated(node, key);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#createdProperties(org.neo4j.graphdb.Node)
     */
    public Map<String, Object> createdProperties(Node node) {
        return getNodeTransactionData().createdProperties(node);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#hasPropertyBeenDeleted(org.neo4j.graphdb.Node, String)
     */
    public boolean hasPropertyBeenDeleted(Node node, String key) {
        return getNodeTransactionData().hasPropertyBeenDeleted(node, key);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#deletedProperties(org.neo4j.graphdb.Node)
     */
    public Map<String, Object> deletedProperties(Node node) {
        return getNodeTransactionData().deletedProperties(node);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#hasPropertyBeenChanged(org.neo4j.graphdb.Node, String)
     */
    public boolean hasPropertyBeenChanged(Node node, String key) {
        return getNodeTransactionData().hasPropertyBeenChanged(node, key);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#changedProperties(org.neo4j.graphdb.Node)
     */
    public Map<String, Change<Object>> changedProperties(Node node) {
        return getNodeTransactionData().changedProperties(node);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#hasLabelBeenAssigned(org.neo4j.graphdb.Node, org.neo4j.graphdb.Label)
     */
    public boolean hasLabelBeenAssigned(Node node, Label label) {
        return getNodeTransactionData().hasLabelBeenAssigned(node, label);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#assignedLabels(org.neo4j.graphdb.Node)
     */
    public Set<Label> assignedLabels(Node node) {
        return getNodeTransactionData().assignedLabels(node);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#hasLabelBeenRemoved(org.neo4j.graphdb.Node, org.neo4j.graphdb.Label)
     */
    public boolean hasLabelBeenRemoved(Node node, Label label) {
        return getNodeTransactionData().hasLabelBeenRemoved(node, label);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#removedLabels(org.neo4j.graphdb.Node)
     */
    public Set<Label> removedLabels(Node node) {
        return getNodeTransactionData().removedLabels(node);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#hasBeenCreated(org.neo4j.graphdb.Relationship)
     */
    public boolean hasBeenCreated(Relationship relationship) {
        return getRelationshipTransactionData().hasBeenCreated(relationship);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#getAllCreatedRelationships()
     */
    public Collection<Relationship> getAllCreatedRelationships() {
        return getRelationshipTransactionData().getAllCreated();
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#hasBeenDeleted(org.neo4j.graphdb.Relationship)
     */
    public boolean hasBeenDeleted(Relationship relationship) {
        return getRelationshipTransactionData().hasBeenDeleted(relationship);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#getDeleted(org.neo4j.graphdb.Relationship)
     */
    public Relationship getDeleted(Relationship relationship) {
        return getRelationshipTransactionData().getDeleted(relationship);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#getAllDeletedRelationships()
     */
    public Collection<Relationship> getAllDeletedRelationships() {
        return getRelationshipTransactionData().getAllDeleted();
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#getDeletedRelationships(org.neo4j.graphdb.Node, org.neo4j.graphdb.RelationshipType...)
     */
    public Collection<Relationship> getDeletedRelationships(Node node, RelationshipType... types) {
        return getRelationshipTransactionData().getDeleted(node, types);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#getDeletedRelationships(org.neo4j.graphdb.Node, org.neo4j.graphdb.Direction, org.neo4j.graphdb.RelationshipType...)
     */
    public Collection<Relationship> getDeletedRelationships(Node node, Direction direction, RelationshipType... types) {
        return getRelationshipTransactionData().getDeleted(node, direction, types);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#hasBeenChanged(org.neo4j.graphdb.Relationship)
     */
    public boolean hasBeenChanged(Relationship relationship) {
        return getRelationshipTransactionData().hasBeenChanged(relationship);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#getChanged(org.neo4j.graphdb.Relationship)
     */
    public Change<Relationship> getChanged(Relationship relationship) {
        return getRelationshipTransactionData().getChanged(relationship);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#getAllChangedRelationships()
     */
    public Collection<Change<Relationship>> getAllChangedRelationships() {
        return getRelationshipTransactionData().getAllChanged();
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#hasPropertyBeenCreated(org.neo4j.graphdb.Relationship, String)
     */
    public boolean hasPropertyBeenCreated(Relationship relationship, String key) {
        return getRelationshipTransactionData().hasPropertyBeenCreated(relationship, key);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#createdProperties(org.neo4j.graphdb.Relationship)
     */
    public Map<String, Object> createdProperties(Relationship relationship) {
        return getRelationshipTransactionData().createdProperties(relationship);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#hasPropertyBeenDeleted(org.neo4j.graphdb.Relationship, String)
     */
    public boolean hasPropertyBeenDeleted(Relationship relationship, String key) {
        return getRelationshipTransactionData().hasPropertyBeenDeleted(relationship, key);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#deletedProperties(org.neo4j.graphdb.Relationship)
     */
    public Map<String, Object> deletedProperties(Relationship relationship) {
        return getRelationshipTransactionData().deletedProperties(relationship);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#hasPropertyBeenChanged(org.neo4j.graphdb.Relationship, String)
     */
    public boolean hasPropertyBeenChanged(Relationship relationship, String key) {
        return getRelationshipTransactionData().hasPropertyBeenChanged(relationship, key);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#changedProperties(org.neo4j.graphdb.Relationship)
     */
    public Map<String, Change<Object>> changedProperties(Relationship relationship) {
        return getRelationshipTransactionData().changedProperties(relationship);
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#mutationsOccurred()
     */
    public boolean mutationsOccurred() {
        return !getAllCreatedNodes().isEmpty()
                || !getAllCreatedRelationships().isEmpty()
                || !getAllDeletedNodes().isEmpty()
                || !getAllDeletedRelationships().isEmpty()
                || !getAllChangedNodes().isEmpty()
                || !getAllChangedRelationships().isEmpty();
    }

    /**
     * @see com.graphaware.tx.event.improved.api.ImprovedTransactionData#mutationsToStrings()
     */
    public Set<String> mutationsToStrings() {
        Set<String> result = new HashSet<>();

        for (Node createdNode : getAllCreatedNodes()) {
            result.add("Created node " + nodeToString(createdNode));
        }

        for (Node deletedNode : getAllDeletedNodes()) {
            result.add("Deleted node " + nodeToString(deletedNode));
        }

        for (Change<Node> changedNode : getAllChangedNodes()) {
            result.add("Changed node " + nodeToString(changedNode.getPrevious()) + " to " + nodeToString(changedNode.getCurrent()));
        }

        for (Relationship createdRelationship : getAllCreatedRelationships()) {
            result.add("Created relationship " + relationshipToString(createdRelationship));
        }

        for (Relationship deletedRelationship : getAllDeletedRelationships()) {
            result.add("Deleted relationship " + relationshipToString(deletedRelationship));
        }

        for (Change<Relationship> changedRelationship : getAllChangedRelationships()) {
            result.add("Changed relationship " + relationshipToString(changedRelationship.getPrevious()) + " to " + relationshipToString(changedRelationship.getCurrent()));
        }

        return result;
    }

    public TransactionData getWrapped() {
        return wrapped;
    }
}
