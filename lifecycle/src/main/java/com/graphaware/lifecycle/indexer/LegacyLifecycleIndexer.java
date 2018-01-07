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

import com.graphaware.lifecycle.event.ScheduledEvent;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.index.lucene.QueryContext;
import org.neo4j.index.lucene.ValueContext;

public class LegacyLifecycleIndexer implements LifecycleIndexer {

	private GraphDatabaseService database;

	public LegacyLifecycleIndexer(GraphDatabaseService database) {
		this.database = database;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void indexNode(ScheduledEvent<Node> event, Node node) {

		Long effectiveDate = event.effectiveDate(node);
		if (effectiveDate != null) {
			database.index().forNodes(event.indexName())
					.add(node, event.name(), new ValueContext(effectiveDate).indexNumeric());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void indexRelationship(ScheduledEvent<Relationship> event, Relationship relationship) {

		Long effectiveDate = event.effectiveDate(relationship);
		if (effectiveDate != null) {
			database.index().forRelationships(event.indexName())
					.add(relationship, event.name(), new ValueContext(effectiveDate).indexNumeric());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndexHits<Node> nodesEligibleFor(ScheduledEvent<Node> event, long timestamp) {
		String indexName = event.indexName();
		if (indexName == null) {
			return null;
		}

		IndexHits<Node> result;

		try (Transaction tx = database.beginTx()) {
			Index<Node> index = database.index().forNodes(indexName);
			result = index.query(QueryContext.numericRange(event.name(), 0L, timestamp));
			tx.success();
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IndexHits<Relationship> relationshipsEligibleFor(ScheduledEvent<Relationship> event, long timestamp) {
		String indexName = event.indexName();
		if (indexName == null) {
			return null;
		}

		IndexHits<Relationship> result;

		try (Transaction tx = database.beginTx()) {
			Index<Relationship> index = database.index().forRelationships(indexName);
			result = index.query(QueryContext.numericRange(event.name(), 0L, timestamp));
			tx.success();
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeNode(ScheduledEvent<Node> event, Node node) {
		try (Transaction tx = database.beginTx()) {
			Index<Node> index = database.index().forNodes(event.indexName());
			index.remove(node, event.name());
			tx.success();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeRelationship(ScheduledEvent<Relationship> event, Relationship relationship) {
		try (Transaction tx = database.beginTx()) {
			Index<Relationship> index = database.index().forRelationships(event.indexName());
			index.remove(relationship, event.name());
			tx.success();
		}
	}

}
