/*
 * Copyright (c) 2013-2018 GraphAware
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.neo4j.graphdb.Node;

import java.util.Map;

public class GraphDetachedNode extends DetachedNode<Long> {

    public GraphDetachedNode() {
    }

    public GraphDetachedNode(Node node) {
        super(node);
    }

    public GraphDetachedNode(Node node, String[] properties) {
        super(node, properties);
    }

    public GraphDetachedNode(long graphId) {
        super(graphId);
    }

    public GraphDetachedNode(String[] labels, Map<String, Object> properties) {
        super(labels, properties);
    }

    public GraphDetachedNode(long graphId, String[] labels, Map<String, Object> properties) {
        super(graphId, labels, properties);
    }

    @JsonIgnore
    @Override
    public Long getId() {
        return getGraphId();
    }
}

