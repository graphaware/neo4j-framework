/*
 * Copyright (c) 2013-2019 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.representation;

import com.graphaware.common.expression.AttachedRelationshipExpressions;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class AttachedRelationship extends AttachedEntity<Relationship> implements AttachedRelationshipExpressions<AttachedNode> {

    private final Node pointOfView;

    public AttachedRelationship(Relationship entity) {
        this(entity, null);
    }

    public AttachedRelationship(Relationship entity, Node pointOfView) {
        super(entity);
        this.pointOfView = pointOfView;
    }

    @Override
    public String getType() {
        return entity.getType().name();
    }

    @Override
    public AttachedNode getStartNode() {
        return new AttachedNode(entity.getStartNode());
    }

    @Override
    public AttachedNode getEndNode() {
        return new AttachedNode(entity.getEndNode());
    }

    @Override
    public AttachedNode pointOfView() {
        return new AttachedNode(pointOfView);
    }
}
