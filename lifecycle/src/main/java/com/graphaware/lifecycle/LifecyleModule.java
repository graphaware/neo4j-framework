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
import com.graphaware.lifecycle.indexer.LifecycleIndexer;
import com.graphaware.lifecycle.config.LifecycleConfiguration;
import com.graphaware.lifecycle.event.commit.CommitEvent;
import com.graphaware.lifecycle.event.scheduled.ScheduledEvent;
import com.graphaware.lifecycle.indexer.LegacyLifecycleIndexer;
import com.graphaware.runtime.config.BaseTxAndTimerDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.EmptyContext;
import com.graphaware.runtime.metadata.TimerDrivenModuleContext;
import com.graphaware.runtime.module.BaseTxDrivenModule;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
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
	private final List<ScheduledEvent> scheduledEvents;
	private final List<CommitEvent> commitEvents;
	private final int batchSize;


	public LifecyleModule(String moduleId,
	                      GraphDatabaseService database,
	                      LifecycleConfiguration config,
	                      List<ScheduledEvent> scheduledEvents,
	                      List<CommitEvent> commitEvents,
	                      int batchSize) {
		super(moduleId);

		this.lifecycleIndexer = new LegacyLifecycleIndexer(database);
		this.config = config;
		this.scheduledEvents = scheduledEvents;
		this.commitEvents = commitEvents;
		this.batchSize = batchSize;

		scheduledEvents.forEach(ScheduledEvent::validate);
		commitEvents.forEach(CommitEvent::validate);

		if (scheduledEvents.size() == 0 && commitEvents.size() == 0) {
			throw new IllegalStateException("Configuration contains no lifecycle events.");
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

		scheduledEvents.forEach(lifecycleEvent -> {
			if (lifecycleEvent.relationshipIndex() != null) {
				LOG.info("Looking at all relationships to see if they have an expiry date or TTL...");

				new IterableInputBatchTransactionExecutor<>(database, batchSize, new AllRelationships(database, batchSize), new UnitOfWork<Relationship>() {
					@Override
					public void execute(GraphDatabaseService database, Relationship r, int batchNumber, int stepNumber) {
						lifecycleIndexer.indexRelationship(lifecycleEvent, r);
					}
				}).execute();
			}

			if (lifecycleEvent.nodeIndex() != null) {
				LOG.info("Looking at all nodes to see if they have an expiry date or TTL...");

				new IterableInputBatchTransactionExecutor<>(database, batchSize, new AllNodes(database, batchSize), new UnitOfWork<Node>() {
					@Override
					public void execute(GraphDatabaseService database, Node n, int batchNumber, int stepNumber) {
						lifecycleIndexer.indexNode(lifecycleEvent, n);
					}
				}).execute();
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

		scheduledEvents.forEach(lifecycleEvent -> {
			applyToRelationships(lifecycleEvent, now);
			applyToNodes(lifecycleEvent, now);
		});

		return new EmptyContext();
	}

	private void applyCommitEvents(ImprovedTransactionData td) {
		commitEvents.forEach(commitEvent -> {

			Stream<Node> nodes = Stream.concat(
					td.getAllCreatedNodes().stream(),
					td.getAllChangedNodes().stream().map(Change::getCurrent));
			nodes.forEach(node -> {
				if (commitEvent.applicableToNode(node)) {
					commitEvent.nodeStrategy().applyIfNeeded(node, commitEvent);
				}
			});

			Stream<Relationship> rels = Stream.concat(
					td.getAllCreatedRelationships().stream(),
					td.getAllChangedRelationships().stream().map(Change::getCurrent));
			rels.forEach(rel -> {
				if (commitEvent.applicableToRelationship(rel)) {
					commitEvent.relationshipStrategy().applyIfNeeded(rel, commitEvent);
				}
			});
		});
	}


	private void applyToRelationships(ScheduledEvent event, long now) {
		int applied = 0;
		IndexHits<Relationship> eligibleRelationships = lifecycleIndexer.relationshipsEligibleFor(event, now);
		if (eligibleRelationships != null) {
			for (Relationship relationship : eligibleRelationships) {
				if (applied < batchSize) {
					boolean didApply = event.relationshipStrategy().applyIfNeeded(relationship, event);
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

	private void applyToNodes(ScheduledEvent event, long now) {
		int applied = 0;
		IndexHits<Node> eligibleNodes = lifecycleIndexer.nodesEligibleFor(event, now);
		if (eligibleNodes != null) {
			for (Node node : eligibleNodes) {
				if (applied < batchSize) {
					boolean didApply = event.nodeStrategy().applyIfNeeded(node, event);
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
			scheduledEvents.forEach(event -> lifecycleIndexer.indexNode(event, node));
		}
	}

	private void indexNewRelationships(ImprovedTransactionData td) {
		for (Relationship relationship : td.getAllCreatedRelationships()) {
			scheduledEvents.forEach(event -> lifecycleIndexer.indexRelationship(event, relationship));
		}
	}

	private void indexChangedNodes(ImprovedTransactionData td) {

		for (Change<Node> change : td.getAllChangedNodes()) {
			Node current = change.getCurrent();

			scheduledEvents.forEach(event -> {
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

			scheduledEvents.forEach(event -> {
				if (event.shouldIndexChanged(current, td)) {
					lifecycleIndexer.removeRelationship(event, change.getPrevious());
					lifecycleIndexer.indexRelationship(event, current);
				}
			});
		}
	}
}
