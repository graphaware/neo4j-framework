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

package com.graphaware.runtime.walk;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * Mechanism by which a {@link Node} is selected in the {@link GraphDatabaseService}.
 * <p/>
 * This is useful for starting a graph walk or making a jump to another {@link Node} if there aren't any
 * {@link org.neo4j.graphdb.Relationship}s to follow.
 */
public interface NodeSelector {

    /**
     * Select a node from the graph.
     *
     * @param database in which to select a node.
     * @return A {@link org.neo4j.graphdb.Node} in the given database, null if a node can't be selected for whatever
     *         reason, perhaps because there is no (matching) node in the database.
     */
    Node selectNode(GraphDatabaseService database);
}
