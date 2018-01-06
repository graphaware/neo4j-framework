/*
 * Copyright (c) 2013-2016 GraphAware
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

package com.graphaware.lifecycle.indexer;

import com.graphaware.lifecycle.event.scheduled.ScheduledEvent;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;

/**
 * A component responsible for handling the indexing of lifecycle dates on nodes and relationships.
 */
public interface LifecycleIndexer {

	/**
	 * Add a given node with any relevant ttl/lifecycle property to the index corresponding to the specified event.
	 * If there is no, or a non-numeric ttl/lifecycle property, do nothing.
	 *
	 * @param event
	 * @param node to index.
	 */
	void indexNode(ScheduledEvent event, Node node);

	/**
	 * Add a given relationship with any relevant ttl/lifecycle property to the index corresponding to the
	 * specified event.
	 * If there is no, or a non-numeric ttl/lifecycle property, do nothing.
	 *
	 * @param event
	 * @param relationship to index.
	 */
	void indexRelationship(ScheduledEvent event, Relationship relationship);

	/**
	 * Finds all indexed nodes that are eligible for the specified lifecycle event at the specified time.
	 *
	 * @param timestamp The timestamp to query for, given as milliseconds since epoch.
	 * @return Iterable of all nodes expiring before timestamp.
	 */
	IndexHits<Node> nodesEligibleFor(ScheduledEvent event, long timestamp);

	/**
	 * Finds all indexed relationships that are eligible for the specified lifecycle event at the specified time.
	 *
	 * @param timestamp The timestamp to query for, given as milliseconds since epoch.
	 * @return Iterable of all relationships expiring before timestamp.
	 */
	IndexHits<Relationship> relationshipsEligibleFor(ScheduledEvent event, long timestamp);

	/**
	 * Removes node from the specified event index. If node is not in the index, it does nothing.
	 *
	 * @param node Node to remove from index.
	 */
	void removeNode(ScheduledEvent event, Node node);

	/**
	 * Removes relationship from the specified index. If relationship is not in the index, it does nothing.
	 *
	 * @param relationship Relationship to remove from index.
	 */
	void removeRelationship(ScheduledEvent event, Relationship relationship);

}
