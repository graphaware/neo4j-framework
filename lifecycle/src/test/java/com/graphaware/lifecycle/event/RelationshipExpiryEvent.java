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
import org.neo4j.graphdb.Relationship;

public class RelationshipExpiryEvent extends ExpiryEvent implements ScheduledEvent<Relationship> {

	public static final String RELATIONSHIP_EXPIRATION_PROPERTY = "relationshipExpirationProperty";
	public static final String RELATIONSHIP_TTL_PROPERTY = "relationshipTtlProperty";
	public static final String RELATIONSHIP_EXPIRATION_STRATEGY = "relationshipExpirationStrategy";
	public static final String EXPIRY_OFFSET = "expiryOffset";
	public static final String RELATIONSHIP_EXPIRATION_INDEX = "relationshipExpirationIndex";

	private LifecycleEventStrategy<Relationship> strategy;

	@Override
	public Class<Relationship> appliesTo() {
		return Relationship.class;
	}

	@Override
	public void configure(Map<String, String> config) {

		expirationProperty = config.getOrDefault(RELATIONSHIP_EXPIRATION_PROPERTY, null);
		ttlProperty = config.getOrDefault(RELATIONSHIP_TTL_PROPERTY, null);
		expiryOffset = Long.valueOf(config.getOrDefault(EXPIRY_OFFSET, "0"));

		strategy = strategy(config.getOrDefault(RELATIONSHIP_EXPIRATION_STRATEGY, "delete"), config);

		if (expirationProperty != null || ttlProperty != null) {
			indexName = config.getOrDefault(RELATIONSHIP_EXPIRATION_INDEX, RELATIONSHIP_EXPIRATION_INDEX);
		}

		validate();
	}

	@Override
	public Long effectiveDate(Relationship rel) {
		return getExpirationDate(rel, this.expirationProperty, this.ttlProperty);
	}

	@Override
	public String indexName() {
		return indexName;
	}

	@Override
	public boolean applyIfNeeded(Relationship rel) {
		return strategy.applyIfNeeded(rel);
	}

	@Override
	public boolean shouldIndexChanged(Relationship rel, ImprovedTransactionData td) {
		return (td.hasPropertyBeenCreated(rel, this.expirationProperty)
				|| td.hasPropertyBeenCreated(rel, this.ttlProperty)
				|| td.hasPropertyBeenChanged(rel, this.expirationProperty)
				|| td.hasPropertyBeenChanged(rel, this.ttlProperty)
				|| td.hasPropertyBeenDeleted(rel, this.expirationProperty)
				|| td.hasPropertyBeenDeleted(rel, this.ttlProperty));

	}

}
