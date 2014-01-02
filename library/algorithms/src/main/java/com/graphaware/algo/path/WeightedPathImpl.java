/*
 * Copyright (c) 2014 GraphAware
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

package com.graphaware.algo.path;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import java.util.Iterator;

/**
 * Immutable implementation of {@link com.graphaware.algo.path.WeightedPath} wrapping a {@link Path} and a cost.
 */
public class WeightedPathImpl implements WeightedPath {

    private final Path wrapped;
    private final long cost;

    /**
     * Construct a new weighted path.
     *
     * @param wrapped the path.
     * @param cost    path's cost.
     */
    public WeightedPathImpl(Path wrapped, long cost) {
        this.wrapped = wrapped;
        this.cost = cost;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCost() {
        return cost;
    }

    //Delegates

    @Override
    public Node startNode() {
        return wrapped.startNode();
    }

    @Override
    public Node endNode() {
        return wrapped.endNode();
    }

    @Override
    public Relationship lastRelationship() {
        return wrapped.lastRelationship();
    }

    @Override
    public Iterable<Relationship> relationships() {
        return wrapped.relationships();
    }

    @Override
    public Iterable<Relationship> reverseRelationships() {
        return wrapped.reverseRelationships();
    }

    @Override
    public Iterable<Node> nodes() {
        return wrapped.nodes();
    }

    @Override
    public Iterable<Node> reverseNodes() {
        return wrapped.reverseNodes();
    }

    @Override
    public int length() {
        return wrapped.length();
    }

    @Override
    public String toString() {
        return wrapped.toString();
    }

    @Override
    public Iterator<PropertyContainer> iterator() {
        return wrapped.iterator();
    }
}
