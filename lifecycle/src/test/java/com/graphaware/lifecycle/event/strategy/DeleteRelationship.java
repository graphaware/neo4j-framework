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

package com.graphaware.lifecycle.event.strategy;

import org.neo4j.graphdb.Relationship;

/**
 * Default {@link Relationship} {@link LifecycleEventStrategy} that simply deletes the expired {@link Relationship}.
 */
public final class DeleteRelationship implements LifecycleEventStrategy<Relationship> {

	public static final String CONFIG_KEY = "delete";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean applyIfNeeded(Relationship entity) {
		entity.delete();
		return true;
	}
}
