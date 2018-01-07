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

package com.graphaware.lifecycle;


import java.util.List;
import java.util.stream.Stream;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.common.util.Change;
import com.graphaware.lifecycle.event.CommitEvent;
import com.graphaware.lifecycle.event.ScheduledEvent;
import com.graphaware.lifecycle.indexer.LegacyLifecycleIndexer;
import com.graphaware.lifecycle.indexer.LifecycleIndexer;
import com.graphaware.runtime.config.BaseTxAndTimerDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.EmptyContext;
import com.graphaware.runtime.metadata.TimerDrivenModuleContext;
import com.graphaware.runtime.module.BaseTxDrivenModule;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.input.AllNodes;
import com.graphaware.tx.executor.input.AllRelationships;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.logging.Log;

/**
 * A {@link TxDrivenModule} and a {@link TimerDrivenModule} that allows for setting an expiry date or ttl on nodes
 * and relationships and deletes them when they have reached that date.
 */
public class LifecyleModule extends BaseTxDrivenModule<Void> implements TimerDrivenModule {

	private static final Log LOG = LoggerFactory.getLogger(LifecyleModule.class);

	private final LifecycleIndexer lifecycleIndexer;
	private final LifecycleConfiguration config;
	private final List<ScheduledEvent<Node>> nodeScheduledEvents;
	private final List<ScheduledEvent<Relationship>> relationshipScheduledEvents;
	private final List<CommitEvent<Node>> nodeCommitEvents;
	private final List<CommitEvent<Relationship>> relationshipCommitEvents;
	private final int batchSize;


	public LifecyleModule(String moduleId,
	                      GraphDatabaseService database,
	                      LifecycleConfiguration config,
	                      List<ScheduledEvent<Node>> nodeScheduledEvents,
	                      List<ScheduledEvent<Relationship>> relationshipScheduledEvents,
	                      List<CommitEvent<Node>> nodeCommitEvents,
	                      List<CommitEvent<Relationship>> relationshipCommitEvents,
	                      int batchSize) {
		super(moduleId);

		this.lifecycleIndexer = new LegacyLifecycleIndexer(database);
		this.config = config;
		this.nodeScheduledEvents = nodeScheduledEvents;
		this.relationshipScheduledEvents = relationshipScheduledEvents;
		this.nodeCommitEvents = nodeCommitEvents;
		this.relationshipCommitEvents = relationshipCommitEvents;
		this.batchSize = batchSize;

		if (nodeScheduledEvents.stream().noneMatch(it -> it.indexName() != null)
				&& relationshipScheduledEvents.stream().noneMatch(it -> it.indexName() != null)
				&& nodeCommitEvents.size() == 0
				&& relationshipCommitEvents.size() == 0) {
			throw new IllegalStateException("Configuration contains no valid lifecycle events.");
		}
	}

	@Override
	public Void beforeCommit(ImprovedTransactionData td) throws DeliberateTransactionRollbackException {
		applyCommitEvents(td);
		indexNewNodes(td);
		indexNewRelationships(td);
		indexChangedNodes(td);
		indexChangedRelationships(td);
		return null;
	}

	@Override
	public void initialize(GraphDatabaseService database) {
		int batchSize = 1000;

		relationshipScheduledEvents.forEach(event -> {
			if (event.indexName() != null) {
				new IterableInputBatchTransactionExecutor<>(database, batchSize, new AllRelationships(database, batchSize),
						(database12, r, batchNumber, stepNumber) -> lifecycleIndexer.indexRelationship(event, r)).execute();
			}
		});

		nodeScheduledEvents.forEach(event -> {
			if (event.indexName() != null) {
				new IterableInputBatchTransactionExecutor<>(database, batchSize, new AllNodes(database, batchSize),
						(database1, n, batchNumber, stepNumber) -> lifecycleIndexer.indexNode(event, n)).execute();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaseTxAndTimerDrivenModuleConfiguration getConfiguration() {
		return config;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TimerDrivenModuleContext createInitialContext(GraphDatabaseService graphDatabaseService) {
		return new EmptyContext();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TimerDrivenModuleContext doSomeWork(TimerDrivenModuleContext timerDrivenModuleContext, GraphDatabaseService graphDatabaseService) {
		long now = System.currentTimeMillis();

		relationshipScheduledEvents.forEach(lifecycleEvent -> applyToRelationships(lifecycleEvent, now));
		nodeScheduledEvents.forEach(lifecycleEvent -> applyToNodes(lifecycleEvent, now));

		return new EmptyContext();
	}

	private void applyCommitEvents(ImprovedTransactionData td) {

		nodeCommitEvents.forEach(commitEvent -> Stream.concat(
				td.getAllCreatedNodes().stream(),
				td.getAllChangedNodes().stream().map(Change::getCurrent))
				.forEach(commitEvent::applyIfNeeded));

		relationshipCommitEvents.forEach(commitEvent -> {
			Stream.concat(
					td.getAllCreatedRelationships().stream(),
					td.getAllChangedRelationships().stream().map(Change::getCurrent))
					.forEach(commitEvent::applyIfNeeded);
		});
	}


	private void applyToRelationships(ScheduledEvent<Relationship> event, long now) {
		int applied = 0;
		IndexHits<Relationship> eligibleRelationships = lifecycleIndexer.relationshipsEligibleFor(event, now);
		if (eligibleRelationships != null) {
			for (Relationship relationship : eligibleRelationships) {
				if (applied < batchSize) {
					boolean didApply = event.applyIfNeeded(relationship);
					if (didApply) {
						lifecycleIndexer.removeRelationship(event, relationship);
					}
					applied++;
				} else {
					break;
				}
			}
		}
	}

	private void applyToNodes(ScheduledEvent<Node> event, long now) {
		int applied = 0;
		IndexHits<Node> eligibleNodes = lifecycleIndexer.nodesEligibleFor(event, now);
		if (eligibleNodes != null) {
			for (Node node : eligibleNodes) {
				if (applied < batchSize) {
					boolean didApply = event.applyIfNeeded(node);
					if (didApply) {
						lifecycleIndexer.removeNode(event, node);
					}
					applied++;
				} else {
					break;
				}
			}
		}
	}


	private void indexNewNodes(ImprovedTransactionData td) {
		for (Node node : td.getAllCreatedNodes()) {
			nodeScheduledEvents.forEach(event -> lifecycleIndexer.indexNode(event, node));
		}
	}

	private void indexNewRelationships(ImprovedTransactionData td) {
		for (Relationship relationship : td.getAllCreatedRelationships()) {
			relationshipScheduledEvents.forEach(event -> lifecycleIndexer.indexRelationship(event, relationship));
		}
	}

	private void indexChangedNodes(ImprovedTransactionData td) {

		for (Change<Node> change : td.getAllChangedNodes()) {
			Node current = change.getCurrent();

			nodeScheduledEvents.forEach(event -> {
				if (event.shouldIndexChanged(current, td)) {
					lifecycleIndexer.removeNode(event, change.getPrevious());
					lifecycleIndexer.indexNode(event, current);
				}
			});
		}
	}

	private void indexChangedRelationships(ImprovedTransactionData td) {
		for (Change<Relationship> change : td.getAllChangedRelationships()) {
			Relationship current = change.getCurrent();

			relationshipScheduledEvents.forEach(event -> {
				if (event.shouldIndexChanged(current, td)) {
					lifecycleIndexer.removeRelationship(event, change.getPrevious());
					lifecycleIndexer.indexRelationship(event, current);
				}
			});
		}
	}
}
