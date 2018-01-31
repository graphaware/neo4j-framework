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

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * {@link LifecycleEventStrategy} that deletes the expired {@link Node} and a all its {@link Relationship}s.
 */
public final class DeleteNodeAndRelationships implements LifecycleEventStrategy<Node> {

	public static final String CONFIG_KEY = "force";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean applyIfNeeded(Node entity) {
		for (Relationship r : entity.getRelationships()) {
			r.delete();
		}
		entity.delete();
		return true;
	}
}
