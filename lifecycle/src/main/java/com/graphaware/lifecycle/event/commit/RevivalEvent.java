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

package com.graphaware.lifecycle.event.commit;

import com.graphaware.lifecycle.strategy.LifecycleEventStrategy;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

public class RevivalEvent implements CommitEvent {


	private String nodeRevivalProperty;
	private String relationshipRevivalProperty;
	private LifecycleEventStrategy<Node> nodeStrategy;
	private LifecycleEventStrategy<Relationship> relationshipStrategy;
	private Long revivalOffset;

	public RevivalEvent(String nodeRevivalProperty,
	                    String relationshipRevivalProperty,
	                    LifecycleEventStrategy<Node> nodeStrategy,
	                    LifecycleEventStrategy<Relationship> relationshipStrategy,
	                    Long revivalOffset) {

		this.nodeRevivalProperty = nodeRevivalProperty;
		this.relationshipRevivalProperty = relationshipRevivalProperty;
		this.nodeStrategy = nodeStrategy;
		this.relationshipStrategy = relationshipStrategy;
		this.revivalOffset = revivalOffset;
	}

	@Override
	public boolean applicableToNode(Node node) {
		return appliesToContainer(node, nodeRevivalProperty);
	}

	@Override
	public boolean applicableToRelationship(Relationship relationship) {
		return appliesToContainer(relationship, relationshipRevivalProperty);
	}

	@Override
	public LifecycleEventStrategy<Node> nodeStrategy() {
		return nodeStrategy;
	}

	@Override
	public LifecycleEventStrategy<Relationship> relationshipStrategy() {
		return relationshipStrategy;
	}

	private boolean appliesToContainer(PropertyContainer pc, String propertyName) {
		if (pc.hasProperty(propertyName)) {
			long revivalProperty = Double.valueOf((pc.getProperty(propertyName).toString())).longValue();
			long revival = revivalProperty + revivalOffset;
			long now = System.currentTimeMillis();
			if (revival >= now) {
				return true;
			}
		}
		return false;
	}
}
