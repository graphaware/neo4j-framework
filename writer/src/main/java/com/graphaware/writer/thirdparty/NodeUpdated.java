/*
 * Copyright (c) 2013-2017 GraphAware
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

package com.graphaware.writer.thirdparty;

import com.graphaware.common.representation.DetachedNode;
import com.graphaware.common.representation.GraphDetachedNode;
import org.neo4j.graphdb.Node;

/**
 * {@link WriteOperation} representing a {@link Node} being updated.
 */
public class NodeUpdated<ID> extends Update<ID, DetachedNode<ID>, Node> {

    /**
     * Create the operation.
     *
     * @param previous representation of the previous state of the updated {@link Node}. Must not be <code>null</code>.
     * @param current representation of the current state of the updated {@link Node}. Must not be <code>null</code>.
     */
    public NodeUpdated(DetachedNode<ID> previous, DetachedNode<ID> current) {
        super(previous, current);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OperationType getType() {
        return OperationType.NODE_UPDATED;
    }
}
