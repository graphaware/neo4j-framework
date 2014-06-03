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

package com.graphaware.tx.event.improved.data;

import com.graphaware.tx.event.improved.api.Change;
import org.neo4j.graphdb.*;

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
     * {@inheritDoc}
     */
    public boolean hasBeenCreated(Node node) {
        return getNodeTransactionData().hasBeenCreated(node);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Node> getAllCreatedNodes() {
        return getNodeTransactionData().getAllCreated();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasBeenDeleted(Node node) {
        return getNodeTransactionData().hasBeenDeleted(node);
    }

    /**
     * {@inheritDoc}
     */
    public Node getDeleted(Node node) {
        return getNodeTransactionData().getDeleted(node);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Node> getAllDeletedNodes() {
        return getNodeTransactionData().getAllDeleted();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasBeenChanged(Node node) {
        return getNodeTransactionData().hasBeenChanged(node);
    }

    /**
     * {@inheritDoc}
     */
    public Change<Node> getChanged(Node node) {
        return getNodeTransactionData().getChanged(node);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Change<Node>> getAllChangedNodes() {
        return getNodeTransactionData().getAllChanged();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPropertyBeenCreated(Node node, String key) {
        return getNodeTransactionData().hasPropertyBeenCreated(node, key);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> createdProperties(Node node) {
        return getNodeTransactionData().createdProperties(node);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPropertyBeenDeleted(Node node, String key) {
        return getNodeTransactionData().hasPropertyBeenDeleted(node, key);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> deletedProperties(Node node) {
        return getNodeTransactionData().deletedProperties(node);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPropertyBeenChanged(Node node, String key) {
        return getNodeTransactionData().hasPropertyBeenChanged(node, key);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Change<Object>> changedProperties(Node node) {
        return getNodeTransactionData().changedProperties(node);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasLabelBeenAssigned(Node node, Label label) {
        return getNodeTransactionData().hasLabelBeenAssigned(node, label);
    }

    /**
     * {@inheritDoc}
     */
    public Set<Label> assignedLabels(Node node) {
        return getNodeTransactionData().assignedLabels(node);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasLabelBeenRemoved(Node node, Label label) {
        return getNodeTransactionData().hasLabelBeenRemoved(node, label);
    }

    /**
     * {@inheritDoc}
     */
    public Set<Label> removedLabels(Node node) {
        return getNodeTransactionData().removedLabels(node);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasBeenCreated(Relationship relationship) {
        return getRelationshipTransactionData().hasBeenCreated(relationship);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Relationship> getAllCreatedRelationships() {
        return getRelationshipTransactionData().getAllCreated();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasBeenDeleted(Relationship relationship) {
        return getRelationshipTransactionData().hasBeenDeleted(relationship);
    }

    /**
     * {@inheritDoc}
     */
    public Relationship getDeleted(Relationship relationship) {
        return getRelationshipTransactionData().getDeleted(relationship);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Relationship> getAllDeletedRelationships() {
        return getRelationshipTransactionData().getAllDeleted();
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Relationship> getDeletedRelationships(Node node, RelationshipType... types) {
        return getRelationshipTransactionData().getDeleted(node, types);
    }

    /**
     * {@inheritDoc}
     */

    public Collection<Relationship> getDeletedRelationships(Node node, Direction direction, RelationshipType... types) {
        return getRelationshipTransactionData().getDeleted(node, direction, types);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasBeenChanged(Relationship relationship) {
        return getRelationshipTransactionData().hasBeenChanged(relationship);
    }

    /**
     * {@inheritDoc}
     */
    public Change<Relationship> getChanged(Relationship relationship) {
        return getRelationshipTransactionData().getChanged(relationship);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Change<Relationship>> getAllChangedRelationships() {
        return getRelationshipTransactionData().getAllChanged();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPropertyBeenCreated(Relationship relationship, String key) {
        return getRelationshipTransactionData().hasPropertyBeenCreated(relationship, key);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> createdProperties(Relationship relationship) {
        return getRelationshipTransactionData().createdProperties(relationship);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPropertyBeenDeleted(Relationship relationship, String key) {
        return getRelationshipTransactionData().hasPropertyBeenDeleted(relationship, key);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> deletedProperties(Relationship relationship) {
        return getRelationshipTransactionData().deletedProperties(relationship);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPropertyBeenChanged(Relationship relationship, String key) {
        return getRelationshipTransactionData().hasPropertyBeenChanged(relationship, key);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Change<Object>> changedProperties(Relationship relationship) {
        return getRelationshipTransactionData().changedProperties(relationship);
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
}
