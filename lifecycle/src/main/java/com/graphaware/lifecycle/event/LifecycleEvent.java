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

import com.graphaware.lifecycle.strategy.CompositeStrategy;
import com.graphaware.lifecycle.strategy.LifecycleEventStrategy;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public interface LifecycleEvent {

	/**
	 * The strategy to apply on nodes, if any, when this lifecycle event fires. This may be a CompositeStrategy, if
	 * multiple actions are required, or null if the event does not apply to nodes.
	 *
	 * @see CompositeStrategy
	 */
	LifecycleEventStrategy<Node> nodeStrategy();

	/**
	 * The strategy to apply on nodes, if any, when this lifecycle event fires. This may be a CompositeStrategy, if
	 * multiple actions are required, or null if the event does not apply to relationships.
	 *
	 * @see CompositeStrategy
	 */
	LifecycleEventStrategy<Relationship> relationshipStrategy();

	/**
	 * The event name. The returned value must be unique among all lifecycle events that are registered with the
	 * module.
	 */
	default String name() {
		return this.getClass().getSimpleName();
	}

	void validate() throws RuntimeException;

}
