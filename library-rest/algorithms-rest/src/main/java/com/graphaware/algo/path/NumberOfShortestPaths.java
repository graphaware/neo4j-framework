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

import com.graphaware.common.util.DirectionUtils;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.Iterables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

/**
 * A managed plugin that finds finds a given number of shortest paths between two nodes. It is different from
 * standard shortest path finding, because it allows to specify the desired number of results.
 * Provided that there are enough paths between the two nodes in the graph, this path finder will first return all the
 * shortest paths, then all the paths one hop longer, then two hops longer, etc., until enough paths have been returned.
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
@Controller
@RequestMapping("/api/library/algorithm/path")
public class NumberOfShortestPaths {

    private final GraphDatabaseService database;

    private NumberOfShortestPathFinder pathFinder = new NumberOfShortestPathFinder();

    @Autowired
    public NumberOfShortestPaths(GraphDatabaseService database) {
        this.database = database;
    }

    @RequestMapping(value = "increasinglyLongerShortestPath", method = RequestMethod.POST)
    @ResponseBody
    public List<JsonPath> numberOfShortestPaths(@RequestBody JsonPathFinderInput jsonInput) {
        try (Transaction tx = database.beginTx()) {
            PathFinderInput input = createInput(jsonInput);

            List<? extends Path> paths = pathFinder.findPaths(input);

            return convertPaths(paths, jsonInput);
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleException() {

    }

    private List<JsonPath> convertPaths(List<? extends Path> paths, JsonPathFinderInput jsonInput) {
        List<JsonPath> result = new LinkedList<>();
        for (Path path : paths) {
            List<Node> nodes = Iterables.toList(path.nodes());
            List<JsonNode> jsonNodes = new LinkedList<>();
            for (Node node : path.nodes()) {
                JsonNode jsonNode = new JsonNode(node.getId());

                if (jsonInput.getNodeProperties() != null) {
                    for (String property : jsonInput.getNodeProperties()) {
                        if (node.hasProperty(property)) {
                            jsonNode.putProperty(property, node.getProperty(property));
                        }
                    }
                }

                if (Boolean.TRUE.equals(jsonInput.getIncludeNodeLabels())) {
                    jsonNode.setLabels(labelsToStringArray(node.getLabels()));
                }

                jsonNodes.add(jsonNode);
            }

            List<JsonRelationship> jsonRelationships = new LinkedList<>();
            int i = 0;
            for (Relationship relationship : path.relationships()) {
                JsonRelationship jsonRelationship = new JsonRelationship(relationship.getId());

                if (jsonInput.getRelationshipProperties() != null) {
                    for (String property : jsonInput.getRelationshipProperties()) {
                        if (relationship.hasProperty(property)) {
                            jsonRelationship.putProperty(property, relationship.getProperty(property));
                        }
                    }
                }

                jsonRelationship.setType(relationship.getType().name());
                jsonRelationship.setDirection(DirectionUtils.resolveDirection(relationship, nodes.get(i)));

                jsonRelationships.add(jsonRelationship);
                i++;
            }

            JsonPath jsonPath = new JsonPath();
            jsonPath.setNodes(jsonNodes.toArray(new JsonNode[jsonNodes.size()]));
            jsonPath.setRelationships(jsonRelationships.toArray(new JsonRelationship[jsonRelationships.size()]));

            if (path instanceof WeightedPath) {
                jsonPath.setCost(((WeightedPath) path).getCost());
            }

            result.add(jsonPath);
        }

        return result;
    }

    private String[] labelsToStringArray(Iterable<Label> labels) {
        List<String> labelsAsList = new LinkedList<>();
        for (Label label : labels) {
            labelsAsList.add(label.name());
        }
        return labelsAsList.toArray(new String[labelsAsList.size()]);
    }

    private PathFinderInput createInput(JsonPathFinderInput jsonInput) {
        if (jsonInput.getStartNode() == null || jsonInput.getEndNode() == null) {
            throw new IllegalArgumentException("Must specify at least start and end nodes!");
        }

        PathFinderInput input = new PathFinderInput(database.getNodeById(jsonInput.getStartNode()), database.getNodeById(jsonInput.getEndNode()));

        if (jsonInput.getSortOrder() != null) {
            input.setSortOrder(jsonInput.getSortOrder());
        }

        if (jsonInput.getCostProperty() != null) {
            input.setCostProperty(jsonInput.getCostProperty());
        }

        if (jsonInput.getMaxResults() != null) {
            input.setMaxResults(jsonInput.getMaxResults());
        }

        if (jsonInput.getDirection() != null) {
            input.setDirection(jsonInput.getDirection());
        }

        if (jsonInput.getMaxDepth() != null) {
            input.setMaxDepth(jsonInput.getMaxDepth());
        }

        if (jsonInput.getTypesAndDirections() != null) {
            if (jsonInput.getDirection() != null) {
                throw new IllegalArgumentException("Must specify either global direction, or specific types and directions, not both!");
            }
            for (JsonRelationshipTypeAndDirection jsonRelationshipTypeAndDirection : jsonInput.getTypesAndDirections()) {
                input.addTypeAndDirection(DynamicRelationshipType.withName(jsonRelationshipTypeAndDirection.getType()), jsonRelationshipTypeAndDirection.getDirection());
            }
        }
        return input;
    }

}
