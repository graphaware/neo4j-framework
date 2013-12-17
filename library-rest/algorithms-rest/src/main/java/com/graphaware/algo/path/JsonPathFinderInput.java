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

public class JsonPathFinderInput extends JsonInput {

    private long startNode;
    private long endNode;
    private Integer maxDepth;
    private Integer maxResults;

    //either concrete relationship types and directions
    private List<JsonRelationshipTypeAndDirection> relationshipsAndDirections;

    //or all relationship types and direction
    private Direction direction;

    private SortOrder sortOrder;
    private String costProperty;

    //getters & setters

    public long getStartNode() {
        return startNode;
    }

    public void setStartNode(long startNode) {
        this.startNode = startNode;
    }

    public long getEndNode() {
        return endNode;
    }

    public void setEndNode(long endNode) {
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

    public List<JsonRelationshipTypeAndDirection> getRelationshipsAndDirections() {
        return relationshipsAndDirections;
    }

    public void setRelationshipsAndDirections(List<JsonRelationshipTypeAndDirection> relationshipsAndDirections) {
        this.relationshipsAndDirections = relationshipsAndDirections;
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
