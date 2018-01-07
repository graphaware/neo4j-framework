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

package com.graphaware.lifecycle.event;

import static com.graphaware.lifecycle.event.strategy.LifecycleEventStrategyFactory.*;

import java.util.Map;

import com.graphaware.lifecycle.event.strategy.LifecycleEventStrategy;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.Node;

public class NodeExpiryEvent extends ExpiryEvent implements ScheduledEvent<Node> {

	public static final String NODE_EXPIRATION_PROPERTY = "nodeExpirationProperty";
	public static final String NODE_TTL_PROPERTY = "nodeTtlProperty";
	public static final String NODE_EXPIRATION_STRATEGY = "nodeExpirationStrategy";
	public static final String NODE_EXPIRATION_INDEX = "nodeExpirationIndex";

	private LifecycleEventStrategy<Node> strategy;

	@Override
	public Class<Node> appliesTo() {
		return Node.class;
	}

	@Override
	public void configure(Map<String, String> config) {

		expirationProperty = config.getOrDefault(NODE_EXPIRATION_PROPERTY, null);
		ttlProperty = config.getOrDefault(NODE_TTL_PROPERTY, null);
		expiryOffset = Long.valueOf(config.getOrDefault(EXPIRY_OFFSET, "0"));
		strategy = strategy(config.getOrDefault(NODE_EXPIRATION_STRATEGY, "orphan"), config);

		if (expirationProperty != null || ttlProperty != null) {
			indexName = config.getOrDefault(NODE_EXPIRATION_INDEX, NODE_EXPIRATION_INDEX);
		}

		validate();
	}

	@Override
	public Long effectiveDate(Node entity) {
		return getExpirationDate(entity, this.expirationProperty, this.ttlProperty);
	}

	@Override
	public String indexName() {
		return indexName;
	}


	@Override
	public boolean applyIfNeeded(Node node) {
		return strategy.applyIfNeeded(node);
	}

	@Override
	public boolean shouldIndexChanged(Node entity, ImprovedTransactionData td) {
		return (td.hasPropertyBeenCreated(entity, this.expirationProperty)
				|| td.hasPropertyBeenCreated(entity, this.ttlProperty)
				|| td.hasPropertyBeenChanged(entity, this.expirationProperty)
				|| td.hasPropertyBeenChanged(entity, this.ttlProperty)
				|| td.hasPropertyBeenDeleted(entity, this.expirationProperty)
				|| td.hasPropertyBeenDeleted(entity, this.ttlProperty));
	}


}
