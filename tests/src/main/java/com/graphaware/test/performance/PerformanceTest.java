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

package com.graphaware.test.performance;

import org.neo4j.graphdb.GraphDatabaseService;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A performance test. Each implementation should represent one type of test, e.g.:
 * - write throughput when creating relationships
 * - typical shortest path query
 * - ...
 */
public interface PerformanceTest {

    Random RANDOM = new Random(System.currentTimeMillis());

    /**
     * Get the name of this test. Will be used in the name of the results file.
     *
     * @return test name.
     */
    String shortName();

    /**
     * Get the name of this test. Will be used in descriptions.
     *
     * @return test name.
     */
    String longName();

    /**
     * Get all the parameters (variables) of this test. They are tried in the order they are returned from this method.
     *
     * @return test parameters.
     */
    List<Parameter> parameters();

    /**
     * How many dry runs of this test should be executed before measured runs start. This could be used, for example,
     * to warm up caches.
     *
     * @param params for current sequence of runs.
     * @return how many dry runs to execute, 0 for none.
     */
    int dryRuns(Map<String, Object> params);

    /**
     * How many measured runs of this test should be executed.
     *
     * @return the number of measured runs.
     */
    int measuredRuns();

    /**
     * Get the parameters for Neo4j (esp. cache config) for the current sequence of runs, based on the test parameters.
     *
     * @param params test parameters.
     * @return Neo4j database params.
     */
    Map<String, String> databaseParameters(Map<String, Object> params);

    /**
     * Prepare the database for performance test, for instance, register the runtime, pre-populate with data, etc.
     * Prepare other things that last as long as the database.
     *
     * @param database to prepare.
     * @param params   test parameters.
     */
    void prepare(GraphDatabaseService database, Map<String, Object> params);

    /**
     * Run the performance test.
     *
     * @param params test parameters.
     * @return measurement result in microseconds.
     */
    long run(GraphDatabaseService database, Map<String, Object> params);

    /**
     * When should the database be rebuilt?
     */
    enum RebuildDatabase {
        /**
         * Database created once for all runs of the test.
         */
        NEVER,
        /**
         * New database created (and prepared) for each new set of parameters.
         */
        AFTER_PARAM_CHANGE,
        /**
         * New database created (and prepared) for each run.
         */
        AFTER_EVERY_RUN,
        /**
         * New database created (and prepared) when the test says so.
         */
        TEST_DECIDES
    }

    /**
     * When should the database be rebuilt?
     *
     * @return when the database should be rebuilt.
     */
    RebuildDatabase rebuildDatabase();

    /**
     * Should the database be rebuilt before a run with the given parameters? Never called unless
     * {@link #rebuildDatabase()} returns {@link RebuildDatabase#TEST_DECIDES}.
     *
     * @param params with which the next test will be run.
     * @return true for db rebuild.
     */
    boolean rebuildDatabase(Map<String, Object> params);
}
