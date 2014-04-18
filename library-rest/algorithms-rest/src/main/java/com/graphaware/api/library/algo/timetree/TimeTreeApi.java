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

package com.graphaware.api.library.algo.timetree;

import com.graphaware.api.common.GraphAwareApi;
import com.graphaware.api.library.algo.path.JsonPath;
import com.graphaware.api.library.algo.path.JsonPathFinderInput;
import com.graphaware.library.algo.path.NumberOfShortestPathsFinder;
import com.graphaware.library.algo.timetree.Resolution;
import com.graphaware.library.algo.timetree.TimeTree;
import com.graphaware.library.algo.timetree.TimeTreeImpl;
import org.joda.time.DateTimeZone;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

/**
 * REST API for {@link com.graphaware.library.algo.timetree.TimeTree}.
 */
@Controller
@RequestMapping("/api/library/algorithm/timetree")
public class TimeTreeApi extends GraphAwareApi {

    private final GraphDatabaseService database;
    private final TimeTree timeTree;

    @Autowired
    public TimeTreeApi(GraphDatabaseService database) {
        this.database = database;
        timeTree = new TimeTreeImpl(database);
    }

    @RequestMapping(value = "instant", method = RequestMethod.GET)
    @ResponseBody
    public long getInstant(
            @RequestParam(value = "time") long timeParam,
            @RequestParam(value = "resolution", required = false) String resolutionParam,
            @RequestParam(value = "timezone", required = false) String timeZoneParam) {

        long result;

        try (Transaction tx = database.beginTx()) {
            result = timeTree.getInstant(timeParam, resolveTimeZone(timeZoneParam), resolveResolution(resolutionParam)).getId();
            tx.success();
        }

        return result;
    }

    @RequestMapping(value = "now", method = RequestMethod.GET)
    @ResponseBody
    public long getNow(
            @RequestParam(value = "resolution", required = false) String resolutionParam,
            @RequestParam(value = "timezone", required = false) String timeZoneParam) {

        long result;

        try (Transaction tx = database.beginTx()) {
            result = timeTree.getNow(resolveTimeZone(timeZoneParam), resolveResolution(resolutionParam)).getId();
            tx.success();
        }

        return result;
    }

    private DateTimeZone resolveTimeZone(String timeZoneParam) {
        DateTimeZone timeZone = null;
        if (timeZoneParam != null) {
            timeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone(timeZoneParam));
        }
        return timeZone;
    }

    private Resolution resolveResolution(String resolutionParam) {
        Resolution resolution = null;
        if (resolutionParam != null) {
            resolution = Resolution.valueOf(resolutionParam.toUpperCase());
        }
        return resolution;
    }
}
