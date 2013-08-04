/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.propertycontainer.persistent;

import org.neo4j.graphdb.Node;

/**
 * A domain class backed by a node. By default, it is a detached new {@link PersistentNode}.
 * <p/>
 * Experimental, will probably go away / be changed
 */
public abstract class NodeBacked {

    private final Node node;

    /**
     * Construct a new domain object backed by a detached new {@link PersistentNode}.
     */
    protected NodeBacked() {
        this(new PersistentNode());
    }

    /**
     * Construct a new domain object backed by a specified {@link org.neo4j.graphdb.Node}.
     *
     * @param node that backs the object.
     */
    protected NodeBacked(Node node) {
        this.node = node;
    }

    /**
     * Get the backing node.
     *
     * @return node, never null.
     */
    public Node getNode() {
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeBacked that = (NodeBacked) o;

        if (!node.equals(that.node)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return node.hashCode();
    }
}
