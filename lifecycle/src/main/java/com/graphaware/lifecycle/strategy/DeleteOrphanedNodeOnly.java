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

package com.graphaware.lifecycle.strategy;

import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;
import com.graphaware.lifecycle.event.LifecycleEvent;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * {@link LifecycleEventStrategy} that deletes the expired {@link Node} if it has no {@link Relationship}s.
 */
public final class DeleteOrphanedNodeOnly extends LifecycleEventStrategy<Node> {

	static {
		Serializer.register(DeleteOrphanedNodeOnly.class, new SingletonSerializer());
	}

	private static final DeleteOrphanedNodeOnly INSTANCE = new DeleteOrphanedNodeOnly();

	public static DeleteOrphanedNodeOnly getInstance() {
		return INSTANCE;
	}

	private DeleteOrphanedNodeOnly() { }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean applyIfNeeded(Node node, LifecycleEvent event) {
		if (!node.hasRelationship()) {
			node.delete();
			return true;
		}
		return false;
	}
}
