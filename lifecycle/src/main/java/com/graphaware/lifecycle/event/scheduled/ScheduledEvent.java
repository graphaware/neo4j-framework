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

package com.graphaware.lifecycle.event.scheduled;

import com.graphaware.lifecycle.event.LifecycleEvent;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public interface ScheduledEvent extends LifecycleEvent {

	/**
	 * Evaluates the date, if any, on which this scheduled event will be executed for a given node.
	 *
	 * @param node to evaluate.
	 * @return execution date.
	 */
	Long effectiveDate(Node node);

	/**
	 * Evaluates the date, if any, on which this scheduled event will be executed for a given relationship.
	 *
	 * @param relationship to evaluate.
	 * @return execution date.
	 */
	Long effectiveDate(Relationship relationship);

	/**
	 * The node index name for this scheduled event. If the event does not apply to nodes, it should return null.
	 */
	String nodeIndex();

	/**
	 * The relationship index name for this scheduled event. If the event does not apply to relationships, it should
	 * return null.
	 */
	String relationshipIndex();

	/**
	 * Given transaction data for a changed node, evaluate if the index should be updated.
	 */
	boolean shouldIndexChanged(Node node, ImprovedTransactionData td);

	/**
	 * Given transaction data for a changed relationship, evaluate if the index should be updated.
	 */
	boolean shouldIndexChanged(Relationship relationship, ImprovedTransactionData td);

	/**
	 * Called on startup to validate that the lifecycle event is in a correctly configured state for usage.
	 *
	 * @throws Exception
	 */
	default void validate() throws RuntimeException {
		if (StringUtils.isBlank(nodeIndex()) && StringUtils.isBlank(relationshipIndex())) {
			String msg = String.format("%s : neither node nor relationship index is configured.", name());
			throw new IllegalStateException(msg);
		}

		if (nodeIndex() != null && nodeStrategy() == null) {
			String msg = String.format("%s : node index is configured without a strategy.", name());
			throw new IllegalStateException(msg);
		}

		if (relationshipIndex() != null && relationshipStrategy() == null) {
			String msg = String.format("%s : relationship index is configured without a strategy.", name());
			throw new IllegalStateException(msg);
		}
	}
}

