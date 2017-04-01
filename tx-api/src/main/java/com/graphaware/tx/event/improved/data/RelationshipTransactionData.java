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

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Collection;

/**
 * {@link PropertyContainerTransactionData} for {@link org.neo4j.graphdb.Relationship}s.
 */
public interface RelationshipTransactionData extends PropertyContainerTransactionData<Relationship> {

    /**
     * Get all relationships for the given node and of the given types, which have been created in the transaction.
     *
     * @param node  for which to get created relationships.
     * @param types of the created relationships. If no types are provided, all types are returned.
     * @return all created relationships for the given node of the given types.
     */
    Collection<Relationship> getCreated(Node node, RelationshipType... types);

    /**
     * Get all relationships for the given node and of the given directions and types, which have been created in the
     * transaction.
     *
     * @param node      for which to get created relationships.
     * @param direction of the created relationships
     * @param types     of the created relationships. If no types are provided, all types are returned.
     * @return all created relationships for the given node of the given direction and types.
     */
    Collection<Relationship> getCreated(Node node, Direction direction, RelationshipType... types);

    /**
     * Get all relationships for the given node and of the given types, which have been deleted in the transaction.
     *
     * @param node  for which to get deleted relationships.
     * @param types of the deleted relationships. If no types are provided, all types are returned.
     * @return snapshot of all deleted relationships for the given node of the given types, as they were before the
     *         transaction started.
     */
    Collection<Relationship> getDeleted(Node node, RelationshipType... types);

    /**
     * Get all relationships for the given node and of the given directions and types, which have been deleted in the
     * transaction.
     *
     * @param node      for which to get deleted relationships.
     * @param direction of the deleted relationships
     * @param types     of the deleted relationships. If no types are provided, all types are returned.
     * @return snapshot of all deleted relationships for the given node of the given direction and types, as they were
     *         before the transaction started.
     */
    Collection<Relationship> getDeleted(Node node, Direction direction, RelationshipType... types);
}
