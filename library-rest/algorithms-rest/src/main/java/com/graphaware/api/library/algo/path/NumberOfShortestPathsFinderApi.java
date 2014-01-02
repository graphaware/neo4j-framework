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

import org.neo4j.graphdb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

/**
 * REST API for {@link com.graphaware.api.library.algo.path.NumberOfShortestPathsFinder}.
 */
@Controller
@RequestMapping("/api/library/algorithm/path")
public class NumberOfShortestPathsFinderApi {

    private final GraphDatabaseService database;

    private NumberOfShortestPathsFinder pathFinder = new NumberOfShortestPathsFinder();

    @Autowired
    public NumberOfShortestPathsFinderApi(GraphDatabaseService database) {
        this.database = database;
    }

    @RequestMapping(value = "increasinglyLongerShortestPath", method = RequestMethod.POST)
    @ResponseBody
    public List<JsonPath> numberOfShortestPaths(@RequestBody JsonPathFinderInput jsonInput) {
        try (Transaction tx = database.beginTx()) {
            List<JsonPath> result = new LinkedList<>();

            for (Path path : pathFinder.findPaths(jsonInput.produceInput(database))) {
                result.add(new JsonPath(path, jsonInput));
            }

            return result;
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleIllegalArguments() {
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFound() {
    }
}
