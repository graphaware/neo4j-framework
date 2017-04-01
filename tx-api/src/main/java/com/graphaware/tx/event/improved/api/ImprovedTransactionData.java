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

package com.graphaware.tx.event.improved.api;

import com.graphaware.common.util.Change;
import org.neo4j.graphdb.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Within the context of a running transaction, this API gives users the ability to find out exactly what mutations
 * occurred on the graph during that transaction. Used in the same context as {@link org.neo4j.graphdb.event.TransactionData},
 * i.e. before or after a transaction commit, this API improves the {@link org.neo4j.graphdb.event.TransactionData} API
 * in a number of ways.
 * <p/>
 * It categorizes {@link org.neo4j.graphdb.PropertyContainer}s, i.e. {@link org.neo4j.graphdb.Node}s  and {@link org.neo4j.graphdb.Relationship}s into:
 * <ul>
 * <li>created in this transaction</li>
 * <li>deleted in this transaction</li>
 * <li>changed in this transaction, i.e those with at least one property created, deleted, or changed</li>
 * <li>untouched by this transaction</li>
 * </ul>
 * Users can find out, whether a {@link org.neo4j.graphdb.PropertyContainer} has been created, deleted, or changed in
 * this transaction and obtain all the created, deleted, and changed {@link org.neo4j.graphdb.PropertyContainer}s.
 * <p/>
 * Properties that have been created, deleted, and changed in the transaction are grouped by the <b>changed</b>
 * {@link org.neo4j.graphdb.PropertyContainer} they belong to. Users can find out, which properties have been created,
 * deleted, and changed for a given <b>changed</b> {@link org.neo4j.graphdb.PropertyContainer} and check, whether
 * a given property for a given <b>changed</b> {@link org.neo4j.graphdb.PropertyContainer} has been created, deleted,
 * or changed.
 * <p/>
 * Properties of created {@link org.neo4j.graphdb.PropertyContainer}s are available through the actual created
 * {@link org.neo4j.graphdb.PropertyContainer}. Properties of deleted {@link org.neo4j.graphdb.PropertyContainer}s
 * (as they were before the transaction started) are available through the snapshot of the deleted {@link org.neo4j.graphdb.PropertyContainer},
 * obtained by calling {@link #getDeleted(org.neo4j.graphdb.Node)} or {@link #getDeleted(org.neo4j.graphdb.Relationship)}.
 * Properties of created and deleted containers will not be returned by {@link #changedProperties(org.neo4j.graphdb.Node)} and {@link #changedProperties(org.neo4j.graphdb.Relationship)}
 * as these only return changed properties of changed {@link org.neo4j.graphdb.PropertyContainer}s.
 * <p/>
 * Changed {@link org.neo4j.graphdb.PropertyContainer}s and properties are wrapped in a {@link Change} object which holds
 * the previous state of the object before the transaction started, and the current state of the object (when the transaction
 * commits).
 * <p/>
 * All created {@link org.neo4j.graphdb.PropertyContainer}s + properties and current versions of changed {@link org.neo4j.graphdb.PropertyContainer}s
 * + properties can be accessed by native Neo4j API and the traversal API as one would expect. For example, one can
 * traverse the graph starting from a newly created node, using a mixture of newly created and already existing
 * relationships. In other words, one can traverse the graph as if the transaction has already been committed. This is
 * similar to using {@link org.neo4j.graphdb.event.TransactionData}.
 * <p/>
 * A major difference between this API and {@link org.neo4j.graphdb.event.TransactionData}, however, is what one can do
 * with the returned information about deleted {@link org.neo4j.graphdb.PropertyContainer}s + properties and the previous
 * versions thereof. With this API, one can traverse a <b>snapshot</b> of the graph as it was before the transaction started.
 * As opposed to the {@link org.neo4j.graphdb.event.TransactionData} API, this will not result in exceptions being thrown.
 * <p/>
 * For example, one can start traversing the graph from a deleted {@link org.neo4j.graphdb.Node}, or the previous version of a changed
 * {@link org.neo4j.graphdb.Node}. Such traversal will only traverse {@link org.neo4j.graphdb.Relationship}s that existed before the transaction started and
 * will return properties and their values as they were before the transaction started. This is achieved using {@link com.graphaware.tx.event.improved.propertycontainer.snapshot.NodeSnapshot}
 * and {@link com.graphaware.tx.event.improved.propertycontainer.snapshot.RelationshipSnapshot} decorators.
 * <p/>
 * One can even perform additional mutating operations on the previous version (snapshot) of the graph, provided that the
 * mutated objects have been changed in the transaction (as opposed to deleted). Mutating deleted {@link org.neo4j.graphdb.PropertyContainer}s
 * and properties does not make any sense and will cause exceptions.
 * <p/>
 * To summarize, this API gives access to two versions of the same graph. Through created {@link org.neo4j.graphdb.PropertyContainer}s
 * and/or their current versions, one can traverse the current version of the graph as it will be after the transaction
 * commits. Through deleted and/or previous versions of {@link org.neo4j.graphdb.PropertyContainer}s, one can traverse
 * the previous snapshot of the graph, as it was before the transaction started.
 */
public interface ImprovedTransactionData {

    /**
     * Check whether the given node has been created in the transaction.
     *
     * @param node to check.
     * @return true iff the node has been created.
     */
    boolean hasBeenCreated(Node node);

    /**
     * Get all nodes created in the transaction.
     *
     * @return read-only collection of all created nodes.
     */
    Collection<Node> getAllCreatedNodes();

    /**
     * Check whether the given node has been deleted in the transaction.
     *
     * @param node to check.
     * @return true iff the node has been deleted.
     */
    boolean hasBeenDeleted(Node node);

    /**
     * Get a node that has been deleted in this transaction as it was before the transaction started.
     *
     * @param node to get.
     * @return snapshot of the node before the transaction started.
     * @throws IllegalArgumentException in case the given node has not been deleted in the transaction.
     */
    Node getDeleted(Node node);

    /**
     * Get all nodes deleted in the transaction as they were before the transaction started.
     *
     * @return read-only collection of all deleted nodes as they were before the transaction started (snapshots).
     */
    Collection<Node> getAllDeletedNodes();

    /**
     * Check whether a node has been changed in the transaction, i.e. if any of its properties have been changed.
     *
     * @param node to check.
     * @return true iff the node has been changed.
     */
    boolean hasBeenChanged(Node node);

    /**
     * Get a node that has been changed in this transaction as it was before the transaction started and as it is now.
     *
     * @param node to get.
     * @return snapshot of the node before the transaction started and the current state of the node.
     * @throws IllegalArgumentException in case the given node has not been changed in the transaction.
     */
    Change<Node> getChanged(Node node);

    /**
     * Get all nodes changed in the transaction.
     *
     * @return a read-only collection of all changed nodes as they were before the transaction started and as they are now.
     */
    Collection<Change<Node>> getAllChangedNodes();

    /**
     * Check whether a property has been created in the transaction.
     *
     * @param node to check. Must be a changed {@link org.neo4j.graphdb.Node}, not a created one.
     * @param key  of the property to check.
     * @return true iff the property has been created on the {@link org.neo4j.graphdb.Node}.
     */
    boolean hasPropertyBeenCreated(Node node, String key);

    /**
     * Get properties created in the transaction.
     *
     * @param node for which to get created properties. Must be a changed {@link org.neo4j.graphdb.Node}, not a created one.
     * @return read-only properties created for the given {@link org.neo4j.graphdb.Node}.
     */
    Map<String, Object> createdProperties(Node node);

    /**
     * Check whether a property has been deleted in the transaction.
     *
     * @param node to check. Must be a changed {@link org.neo4j.graphdb.Node}, not a deleted one.
     * @param key  of the property to check.
     * @return true iff the property has been deleted for the given {@link org.neo4j.graphdb.Node}.
     */
    boolean hasPropertyBeenDeleted(Node node, String key);

    /**
     * Get properties deleted in the transaction.
     *
     * @param node for which to get deleted properties. Must be a changed {@link org.neo4j.graphdb.Node}, not a deleted one.
     * @return read-only properties deleted for the given  {@link org.neo4j.graphdb.Node}, where the value is the property value before
     *         the transaction started.
     */
    Map<String, Object> deletedProperties(Node node);

    /**
     * Check whether a property has been changed in the transaction.
     *
     * @param node to check.
     * @param key  of the property to check.
     * @return true iff the property has been changed.
     */
    boolean hasPropertyBeenChanged(Node node, String key);

    /**
     * Get properties changed in the transaction.
     *
     * @param node for which to get changed properties.
     * @return read-only properties changed for the given node, where the value is the property value before and
     *         after the transaction started, respectively.
     */
    Map<String, Change<Object>> changedProperties(Node node);

    /**
     * Check whether a label has been assigned in the transaction.
     *
     * @param node  to check.
     * @param label to check.
     * @return true iff the node has been assigned.
     */
    boolean hasLabelBeenAssigned(Node node, Label label);

    /**
     * Get labels assigned in the transaction.
     *
     * @param node for which to get assigned labels.
     * @return read-only labels created for the given node.
     */
    Set<Label> assignedLabels(Node node);

    /**
     * Check whether a label has been removed in the transaction.
     *
     * @param node  to check.
     * @param label to check.
     * @return true iff the label has been removed.
     */
    boolean hasLabelBeenRemoved(Node node, Label label);

    /**
     * Get labels removed in the transaction.
     *
     * @param node for which to get removed labels.
     * @return read-only labels removed for the given node.
     */
    Set<Label> removedLabels(Node node);

    /**
     * Check whether the given relationship has been created in the transaction.
     *
     * @param relationship to check.
     * @return true iff the relationship has been created.
     */
    boolean hasBeenCreated(Relationship relationship);

    /**
     * Get all relationships created in the transaction.
     *
     * @return read-only collection of all created relationships.
     */
    Collection<Relationship> getAllCreatedRelationships();

    /**
     * Check whether the given relationship has been deleted in the transaction.
     *
     * @param relationship to check.
     * @return true iff the relationship has been deleted.
     */
    boolean hasBeenDeleted(Relationship relationship);

    /**
     * Get a relationship that has been deleted in this transaction as it was before the transaction started.
     *
     * @param relationship to get.
     * @return snapshot of the relationship before the transaction started.
     * @throws IllegalArgumentException in case the given relationship has not been deleted in the transaction.
     */
    Relationship getDeleted(Relationship relationship);

    /**
     * Get all relationships deleted in the transaction as they were before the transaction started.
     *
     * @return read-only collection of all deleted relationships as they were before the transaction started (snapshots).
     */
    Collection<Relationship> getAllDeletedRelationships();

    /**
     * Get all relationships for the given node and of the given types, which have been deleted in the transaction.
     *
     * @param node  for which to get deleted relationships.
     * @param types of the deleted relationships. If no types are provided, all types are returned.
     * @return snapshot of all deleted relationships for the given node of the given types, as they were before the
     *         transaction started.
     */
    Collection<Relationship> getDeletedRelationships(Node node, RelationshipType... types);

    /**
     * Get all relationships for the given node and of the given directions and types, which have been deleted in the
     * transaction.
     *
     * @param node      for which to get deleted relationships.
     * @param direction of the deleted relationships
     * @param types     of the deleted relationships.
     * @return snapshot of all deleted relationships for the given node of the given direction and types, as they were
     *         before the transaction started.
     */
    Collection<Relationship> getDeletedRelationships(Node node, Direction direction, RelationshipType... types);

    /**
     * Check whether a relationship has been changed in the transaction, i.e. if any of its properties have been changed.
     *
     * @param relationship to check.
     * @return true iff the relationship has been changed.
     */
    boolean hasBeenChanged(Relationship relationship);

    /**
     * Get a relationship that has been changed in this transaction as it was before the transaction started and as it is now.
     *
     * @param relationship to get.
     * @return snapshot of the relationship before the transaction started and the current state of the relationship.
     * @throws IllegalArgumentException in case the given relationship has not been changed in the transaction.
     */
    Change<Relationship> getChanged(Relationship relationship);

    /**
     * Get all relationships changed in the transaction.
     *
     * @return a read-only collection of all changed relationships as they were before the transaction started and as they are now.
     */
    Collection<Change<Relationship>> getAllChangedRelationships();

    /**
     * Check whether a property has been created in the transaction.
     *
     * @param relationship to check. Must be a changed {@link org.neo4j.graphdb.Relationship}, not a created one.
     * @param key          of the property to check.
     * @return true iff the property has been created for the {@link org.neo4j.graphdb.Relationship}.
     */
    boolean hasPropertyBeenCreated(Relationship relationship, String key);

    /**
     * Get properties created in the transaction.
     *
     * @param relationship for which to get created properties. Must be a changed {@link org.neo4j.graphdb.Node}, not a created one.
     * @return read-only properties created for the given changed {@link org.neo4j.graphdb.Relationship}.
     */
    Map<String, Object> createdProperties(Relationship relationship);

    /**
     * Check whether a property has been deleted in the transaction.
     *
     * @param relationship to check. Must be a changed {@link org.neo4j.graphdb.Relationship}, not a deleted one.
     * @param key          of the property to check.
     * @return true iff the property has been deleted for the given {@link org.neo4j.graphdb.Relationship}.
     */
    boolean hasPropertyBeenDeleted(Relationship relationship, String key);

    /**
     * Get properties deleted in the transaction.
     *
     * @param relationship for which to get deleted properties. Must be a changed {@link org.neo4j.graphdb.Relationship}, not a deleted one.
     * @return read-only properties deleted for the given relationship, where the value is the property value before the
     *         transaction started.
     */
    Map<String, Object> deletedProperties(Relationship relationship);

    /**
     * Check whether a property has been changed in the transaction.
     *
     * @param relationship to check.
     * @param key          of the property to check.
     * @return true iff the property has been changed.
     */
    boolean hasPropertyBeenChanged(Relationship relationship, String key);

    /**
     * Get properties changed in the transaction.
     *
     * @param relationship for which to get changed properties.
     * @return read-only properties changed for the given relationship, where the value is the property value before and
     *         after the transaction started, respectively.
     */
    Map<String, Change<Object>> changedProperties(Relationship relationship);

    /**
     * Have any mutations actually occurred?
     *
     * @return true iff an least one of the following method calls returns a non-empty collection:
     *         {@link #getAllCreatedNodes()}, {@link #getAllCreatedRelationships()}, {@link #getAllDeletedNodes()},
     *         {@link #getAllDeletedRelationships()}, {@link #getAllChangedNodes()}, {@link #getAllChangedRelationships()}.
     */
    boolean mutationsOccurred();

    /**
     * Convert all mutations in the transaction to human-readable Strings.
     *
     * @return human-readable Strings.
     */
    Set<String> mutationsToStrings();
}
