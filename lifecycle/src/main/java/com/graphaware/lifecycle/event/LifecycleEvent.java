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

import java.util.Map;

import org.neo4j.graphdb.Entity;

public interface LifecycleEvent {

	/**
	 * Indicates whether the event applies to Nodes or Relationships. It is not currently supported for an event to
	 * apply to all entity types.
	 * @return Must be either Node.class or Relationship.class
	 */
	Class<? extends Entity> appliesTo();

	/**
	 * Configure the event prior to use. This method is invoked on startup by the module using either neo4j.conf
	 * or properties specified programmatically.
	 * @param config
	 *
	 * TODO: Support fine-grained injection of type-safe config, eg Spring @Value annotation.
	 */
	void configure(Map<String, String> config);

	/**
	 * The event name. The returned value must be unique among all lifecycle events that are registered with the
	 * module.
	 */
	default String name() {
		return this.getClass().getSimpleName();
	}

}
