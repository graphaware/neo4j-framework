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

package com.graphaware.api.library.algo.path;

import com.graphaware.api.library.common.JsonInput;
import com.graphaware.api.library.common.JsonRelationshipTypeAndDirection;
import org.neo4j.graphdb.*;

import java.util.List;

/**
 * JSON-serializable input to a path finder.
 */
public class JsonPathFinderInput extends JsonInput {

    private Long startNode;
    private Long endNode;
    private Integer maxDepth;
    private Integer maxResults;

    //either concrete relationship types and directions
    private List<JsonRelationshipTypeAndDirection> typesAndDirections;

    //or all relationship types and direction
    private Direction direction;

    private SortOrder sortOrder;
    private String costProperty;

    /**
     * Produce path finder input from this JSON representation.
     *
     * @param database to find nodes in.
     * @return path finder input.
     */
    public PathFinderInput produceInput(GraphDatabaseService database) {
        if (getStartNode() == null || getEndNode() == null) {
            throw new IllegalArgumentException("Must specify at least start and end nodes!");
        }

        PathFinderInput input = new PathFinderInput(database.getNodeById(getStartNode()), database.getNodeById(getEndNode()));

        if (getSortOrder() != null) {
            input.setSortOrder(getSortOrder());
        }

        if (getCostProperty() != null) {
            input.setCostProperty(getCostProperty());
        }

        if (getMaxResults() != null) {
            input.setMaxResults(getMaxResults());
        }

        if (getDirection() != null) {
            input.setDirection(getDirection());
        }

        if (getMaxDepth() != null) {
            input.setMaxDepth(getMaxDepth());
        }

        if (getTypesAndDirections() != null) {
            if (getDirection() != null) {
                throw new IllegalArgumentException("Must specify either global direction, or specific types and directions, not both!");
            }
            for (JsonRelationshipTypeAndDirection jsonRelationshipTypeAndDirection : getTypesAndDirections()) {
                input.addTypeAndDirection(DynamicRelationshipType.withName(jsonRelationshipTypeAndDirection.getType()), jsonRelationshipTypeAndDirection.getDirection());
            }
        }
        return input;
    }

    //getters & setters

    public Long getStartNode() {
        return startNode;
    }

    public void setStartNode(Long startNode) {
        this.startNode = startNode;
    }

    public Long getEndNode() {
        return endNode;
    }

    public void setEndNode(Long endNode) {
        this.endNode = endNode;
    }

    public Integer getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public List<JsonRelationshipTypeAndDirection> getTypesAndDirections() {
        return typesAndDirections;
    }

    public void setTypesAndDirections(List<JsonRelationshipTypeAndDirection> typesAndDirections) {
        this.typesAndDirections = typesAndDirections;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getCostProperty() {
        return costProperty;
    }

    public void setCostProperty(String costProperty) {
        this.costProperty = costProperty;
    }
}
