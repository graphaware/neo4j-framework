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

import com.graphaware.api.common.GraphAwareApi;
import com.graphaware.library.algo.path.NumberOfShortestPathsFinder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedList;
import java.util.List;

/**
 * REST API for {@link com.graphaware.library.algo.path.NumberOfShortestPathsFinder}.
 */
@Controller
@RequestMapping("/api/library/algorithm/path")
public class NumberOfShortestPathsFinderApi extends GraphAwareApi {

    private final GraphDatabaseService database;

    private NumberOfShortestPathsFinder pathFinder = new NumberOfShortestPathsFinder();

    @Autowired
    public NumberOfShortestPathsFinderApi(GraphDatabaseService database) {
        this.database = database;
    }

    @RequestMapping(value = "increasinglyLongerShortestPath", method = RequestMethod.POST)
    @ResponseBody
    public List<JsonPath> numberOfShortestPaths(@RequestBody JsonPathFinderInput jsonInput) {
        List<JsonPath> result = new LinkedList<>();

        try (Transaction tx = database.beginTx()) {
            for (Path path : pathFinder.findPaths(jsonInput.produceInput(database))) {
                result.add(new JsonPath(path, jsonInput));
            }
            tx.success();
        }

        return result;
    }
}
