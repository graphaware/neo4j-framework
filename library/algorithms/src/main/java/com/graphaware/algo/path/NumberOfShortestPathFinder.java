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
import org.neo4j.graphdb.Path;
import org.neo4j.helpers.collection.Iterables;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A path finder that finds a given number of shortest paths between two nodes. It is different from {@link ShortestPath}
 * because it allows to specify the desired number of results and path ordering, optionally based on a total path cost.
 * <p/>
 * Provided that there are enough paths between the two nodes in the graph, this path finder will first return all the
 * shortest paths, then all the paths one hop longer, then two hops longer, etc., until enough paths have been returned.
 * <p/>
 * If {@link PathFinderInput#getSortOrder()} is {@link com.graphaware.algo.path.SortOrder#LENGTH_ASC_THEN_COST_ASC} or
 * {@link com.graphaware.algo.path.SortOrder#LENGTH_ASC_THEN_COST_DESC}, then {@link PathFinderInput#getCostProperty()}
 * must also be provided and paths with the same length are ordered by total cost ascending or descending, respectively.
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
public class NumberOfShortestPathFinder {

    /**
     * Find paths between the start and end nodes.
     *
     * @param input path finder input.
     * @return paths between the two nodes.
     */
    public List<? extends Path> findPaths(PathFinderInput input) {
        List<Path> paths = findPathsSortedByLength(input);

        if (SortOrder.LENGTH_ASC.equals(input.getSortOrder())) {
            return paths;
        }

        if (input.getCostProperty() == null) {
            throw new IllegalArgumentException("Cost property is null, but sort order is " + input.getSortOrder());
        }

        List<WeightedPath> weightedPaths;

        switch (input.getSortOrder()) {
            case LENGTH_ASC_THEN_COST_ASC:
                weightedPaths = calculateCost(paths, new PathCostCalculatorImpl(new MaxLongDefaultingRelationshipCostFinder(input.getCostProperty())));
                Collections.sort(weightedPaths, new LengthThenCostWeightedPathComparator(LengthThenCostWeightedPathComparator.SortOrder.ASC));
                return weightedPaths;
            case LENGTH_ASC_THEN_COST_DESC:
                weightedPaths = calculateCost(paths, new PathCostCalculatorImpl(new ZeroDefaultingRelationshipCostFinder(input.getCostProperty())));
                Collections.sort(weightedPaths, new LengthThenCostWeightedPathComparator(LengthThenCostWeightedPathComparator.SortOrder.DESC));
                return weightedPaths;
            default:
                throw new IllegalStateException("Illegal sort order " + input.getSortOrder() + ". This is a bug");
        }
    }

    /**
     * Find paths between the start and end nodes.
     *
     * @param input path finder input.
     * @return paths between the two nodes.
     */
    private List<Path> findPathsSortedByLength(PathFinderInput input) {
        List<Path> result = new LinkedList<Path>();

        //first attempt: classic shortest path
        result.addAll(Iterables.toList(new ShortestPath(input.getMaxDepth(), input.getExpander()).findAllPaths(input.getStart(), input.getEnd())));

        //If there are no results, there will never be any. If there are enough, then we just return them:
        if (result.isEmpty() || result.size() >= input.getMaxResults()) {
            return result;
        }

        //Now, we have some results, but not enough. All the resulting paths so far must have the same length (they are
        //the shortest paths after all). We try with longer path length until we have enough:
        for (int depth = result.get(0).length() + 1; depth <= input.getMaxDepth() && result.size() < input.getMaxResults(); depth++) {
            result.addAll(Iterables.toList(new ShortestPath(depth, input.getExpander(), Integer.MAX_VALUE, true).findAllPaths(input.getStart(), input.getEnd())));
        }

        return result;
    }

    /**
     * Convert paths to weighted paths.
     *
     * @param paths to convert.
     * @return weighted paths.
     */
    private List<WeightedPath> calculateCost(List<Path> paths, PathCostCalculator costCalculator) {
        List<WeightedPath> result = new LinkedList<>();
        for (Path path : paths) {
            result.add(new WeightedPathImpl(path, costCalculator.calculateCost(path)));
        }
        return result;
    }
}
