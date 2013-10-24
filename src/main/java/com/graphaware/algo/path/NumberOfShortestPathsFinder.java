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

package com.graphaware.algo.path;

import org.neo4j.graphalgo.impl.path.ShortestPath;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.helpers.collection.Iterables;

import java.util.LinkedList;
import java.util.List;

/**
 * A path finder that finds a given number of shortest paths between two nodes. It is different from {@link ShortestPath}
 * because it allows to specify the desired number of results. Provided that there are enough paths between the two
 * nodes in the graph, this path finder will first return all the shortest paths, then all the paths one hop longer,
 * then two hops longer, etc., until enough paths have been returned.
 * <p/>
 * Please note that nodes that are on a path with certain length will not be considered for paths with greater lengths.
 * For example, given the following graph:
 * <p/>
 * (1)->(2)->(3)
 * (1)->(4)->(5)->(3)
 * (4)->(2)
 * <p/>
 * the shortest path from (1) to (3) is (1)->(2)->(3) and has a length of 2. If more paths are needed, the next path
 * returned will be (1)->(4)->(5)->(3) with a length of 3. Note that there is another path of length 3:
 * (1)->(4)->(2)->(3), but it is not returned, since (2)->(3) is contained in a shorter path.
 */
public class NumberOfShortestPathsFinder {

    private final int maxDepth;
    private final int noResults;
    private final PathExpander expander;

    /**
     * Construct a new path finder.
     *
     * @param maxDepth  the maximum depth for the traversal. Returned paths will never have a greater
     *                  {@link org.neo4j.graphdb.Path#length()} than {@code maxDepth}.
     * @param noResults the desired number of paths to return, if at all possible. Once reached, search will stop.
     * @param expander  the {@link org.neo4j.graphdb.PathExpander} to use for deciding which relationships to expand.
     */
    public NumberOfShortestPathsFinder(int maxDepth, int noResults, PathExpander expander) {
        this.maxDepth = maxDepth;
        this.noResults = noResults;
        this.expander = expander;
    }

    /**
     * Find paths between the start and end nodes.
     *
     * @param start start node.
     * @param end   end node.
     * @return paths between the two nodes, sorted by path lengths.
     */
    public List<Path> findPaths(Node start, Node end) {
        List<Path> result = new LinkedList<Path>();

        //first attempt: classic shortest path
        result.addAll(Iterables.toList(new ShortestPath(maxDepth, expander).findAllPaths(start, end)));

        //If there are no results, there will never be any. If there are enough, then we just return them:
        if (result.isEmpty() || result.size() >= noResults) {
            return result;
        }

        //Now, we have some results, but not enough. All the resulting paths so far must have the same length (they are
        //the shortest paths after all). We try with longer path length until we have enough:
        for (int depth = result.get(0).length() + 1; depth <= maxDepth && result.size() < noResults; depth++) {
            result.addAll(Iterables.toList(new ShortestPath(depth, expander, Integer.MAX_VALUE, true).findAllPaths(start, end)));
        }

        return result;
    }
}
