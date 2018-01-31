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
import org.neo4j.graphdb.Node;

public class NodeRevivalEvent implements CommitEvent<Node> {

	public static final String NODE_REVIVAL_PROPERTY = "nodeRevivalProperty";
	public static final String NODE_REVIVAL_STRATEGY = "nodeRevivalStrategy";
	public static final String REVIVAL_OFFSET = "revivalOffset";

	private String revivalProperty;
	private LifecycleEventStrategy<Node> strategy;
	private Long revivalOffset;

	@Override
	public Class<Node> appliesTo() {
		return Node.class;
	}

	@Override
	public void configure(Map<String, String> config) {
		this.revivalProperty = config.getOrDefault(NODE_REVIVAL_PROPERTY, null);
		this.strategy = strategy(config.getOrDefault(NODE_REVIVAL_STRATEGY, null), config);
		this.revivalOffset = Long.parseLong(config.getOrDefault(REVIVAL_OFFSET, "0"));
	}


	@Override
	public boolean applyIfNeeded(Node node) {
		if (applicable(node)) {
			return strategy.applyIfNeeded(node);
		}
		return false;
	}

	private boolean applicable(Node entity) {
		if (entity.hasProperty(revivalProperty)) {
			long revivalProperty = Double.valueOf((entity.getProperty(this.revivalProperty).toString())).longValue();
			long revival = revivalProperty + revivalOffset;
			long now = System.currentTimeMillis();
			if (revival >= now) {
				return true;
			}
		}
		return false;
	}
}
