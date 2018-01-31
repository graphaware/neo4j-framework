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

import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.Entity;

public interface ScheduledEvent<E extends Entity> extends LifecycleEvent {

	/**
	 * {@inheritDoc}
	 */
	Class<E> appliesTo();

	/**
	 * Evaluates the date, if any, on which this scheduled event will be executed for a given Property Container.
	 *
	 * @param entity to evaluate.
	 * @return execution date.
	 */
	Long effectiveDate(E entity);

	/**
	 * Evaluate necessity of, and execute expiry of an Entity.
	 *
	 * @param entity to expire
	 */
	boolean applyIfNeeded(E entity);

	/**
	 * The index name for this scheduled event. Must not be null.
	 */
	String indexName();

	/**
	 * Given transaction data for a changed node, evaluate if the index should be updated.
	 */
	boolean shouldIndexChanged(E entity, ImprovedTransactionData td);

}

