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

import org.neo4j.graphdb.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulation of parameters for path finders, with fluent interface.
 * The default maximum traversal depth is 3, the default maximum number of results (paths) is {@link Integer#MAX_VALUE},
 * and the default relationships to be traversed are all relationships in all directions. {@link org.neo4j.graphdb.RelationshipType}s
 * and {@link org.neo4j.graphdb.Direction}s can be explicitly added, which overrides the default "traverse-all" policy.
 * <p/>
 * The default sorting for paths is by increasing length, where the sorting of paths with the same length is undefined.
 * This can be overridden by explicitly setting a different ordering (which sorts paths with the same length using a cost
 * defined on each relationship), in which case the relationship property representing that cost must also be defined
 * (using {@link com.graphaware.algo.path.PathFinderInput#setCostProperty(String)}).
 * <p/>
 * This class is not thread-safe, it should not be shared among threads (there should be no reason to do so).
 */
public class PathFinderInput {

    private final Node start;
    private final Node end;
    private int maxDepth = 3;
    private int maxResults = Integer.MAX_VALUE;
    private final List<Object> relationshipsAndDirections = new ArrayList<>();
    private Direction direction;
    private SortOrder sortOrder = SortOrder.LENGTH_ASC;
    private String costProperty;

    /**
     * Construct new path finder input.
     *
     * @param start start node.
     * @param end   end node.
     */
    public PathFinderInput(Node start, Node end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Set maximum traversal depth.
     *
     * @param maxDepth new max depth.
     * @return self.
     */
    public PathFinderInput setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    /**
     * Set maximum number of results.
     *
     * @param maxResults new max number of results.
     * @return self.
     */
    public PathFinderInput setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public PathFinderInput setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    /**
     * Add a relationship type to be traversed in {@link Direction#BOTH} directions.
     *
     * @param type relationship type.
     * @return self.
     */
    public PathFinderInput addType(RelationshipType type) {
        return addTypeAndDirection(type, Direction.BOTH);
    }

    /**
     * Add a relationship type to be traversed in the given direction.
     *
     * @param type      relationship type.
     * @param direction direction.
     * @return self.
     */
    public PathFinderInput addTypeAndDirection(RelationshipType type, Direction direction) {
        relationshipsAndDirections.add(type);
        relationshipsAndDirections.add(direction);
        return this;
    }

    /**
     * Set the sort order of the found paths.
     *
     * @param sortOrder sort order.
     * @return self.
     */
    public PathFinderInput setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    /**
     * Set the cost property on relationships on the found paths. Only relevant when
     * {@link com.graphaware.algo.path.SortOrder#LENGTH_ASC_THEN_COST_ASC} or
     * {@link com.graphaware.algo.path.SortOrder#LENGTH_ASC_THEN_COST_DESC} is used.
     *
     * @param costProperty name of the numerical property on relationships representing cost.
     * @return self.
     */
    public PathFinderInput setCostProperty(String costProperty) {
        this.costProperty = costProperty;
        return this;
    }

    //Getters

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Get a path expander representing this input.
     *
     * @return path expander.
     */
    public PathExpander getExpander() {
        if (relationshipsAndDirections.isEmpty()) {
            return PathExpanders.forDirection(direction == null ? Direction.BOTH : direction);
        }

        //Maybe there's a less ugly way of doing this using Neo4j APIs, who knows:
        if (relationshipsAndDirections.size() == 2) {
            return PathExpanders.forTypeAndDirection(
                    (RelationshipType) relationshipsAndDirections.get(0),
                    (Direction) relationshipsAndDirections.get(1)
            );
        }

        return PathExpanders.forTypesAndDirections(
                (RelationshipType) relationshipsAndDirections.get(0),
                (Direction) relationshipsAndDirections.get(1),
                (RelationshipType) relationshipsAndDirections.get(2),
                (Direction) relationshipsAndDirections.get(3),
                relationshipsAndDirections.size() > 3 ? relationshipsAndDirections.subList(4, relationshipsAndDirections.size()).toArray() : new Object[0]
        );
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public String getCostProperty() {
        return costProperty;
    }
}
