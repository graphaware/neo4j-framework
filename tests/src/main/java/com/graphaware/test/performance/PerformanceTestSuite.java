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

import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;

/**
 * Base class for suites of {@link PerformanceTest}s.
 */
public abstract class PerformanceTestSuite {

    private static final Log LOG = LoggerFactory.getLogger(PerformanceTestSuite.class);

    protected TemporaryFolder temporaryFolder;
    protected GraphDatabaseService database;

    /**
     * Get the performance tests run as a part of this test suite.
     *
     * @return performance tests, which will be run in this order.
     */
    protected abstract PerformanceTest[] getPerfTests();

    /**
     * Perform the actual measurements and report results.
     */
    @Test
    public void measurePerformance() {
        LOG.info("Performance test suite started.");

        for (PerformanceTest performanceTest : getPerfTests()) {
            LOG.info("Started performance test: " + performanceTest.longName());

            TestResults testResults = run(performanceTest);
            testResults.printToFile(performanceTest.longName(), getFileName(performanceTest));

            LOG.info("Finished performance test: " + performanceTest.longName());
        }

        LOG.info("Performance test suite finished.");
    }

    /**
     * Get the name of the file into which the results of this test will be written.
     *
     * @param performanceTest test.
     * @return file name.
     */
    protected String getFileName(PerformanceTest performanceTest) {
        return performanceTest.shortName() + "-" + System.currentTimeMillis() + ".txt";
    }

    /**
     * Run the performance test a number of times (specified by the test itself) for each combination of parameters.
     *
     * @param performanceTest to run.
     * @return test results.
     */
    private TestResults run(PerformanceTest performanceTest) {
        List<Map<String, Object>> parameterCombinations = generateParameterCombinations(performanceTest.parameters());
        TestResults testResults = new TestResultsImpl();

        for (Map<String, Object> params : parameterCombinations) {
            run(performanceTest, params, testResults);
        }

        if (performanceTest.rebuildDatabase().equals(PerformanceTest.RebuildDatabase.NEVER)) {
            closeDatabase();
        }

        return testResults;
    }

    /**
     * Run the performance test a number of times (specified by the test itself) for a concrete combination of parameters.
     *
     * @param performanceTest to run.
     * @param params          with which to run.
     * @param testResults     of tests run thus far.
     */
    private void run(PerformanceTest performanceTest, Map<String, Object> params, TestResults testResults) {
        LOG.info("Will run tests with " + paramsToString(params));

        createDatabaseIfNeeded(performanceTest, params);

        LOG.info("Starting dry runs...");

        //dry runs - ignore results
        for (int i = 0; i < performanceTest.dryRuns(params); i++) {
            LOG.debug("Dry run " + (i + 1));
            run(performanceTest, params);
        }

        LOG.info("Starting measured runs...");

        //real runs
        for (int i = 0; i < performanceTest.measuredRuns(); i++) {
            LOG.debug("Measured run " + (i + 1));
            testResults.acceptResult(params, run(performanceTest, params));
        }

        LOG.info("Finished test runs.");

        if (performanceTest.rebuildDatabase().equals(PerformanceTest.RebuildDatabase.AFTER_PARAM_CHANGE)) {
            closeDatabase();
        }
    }

    /**
     * Run the performance test once.
     *
     * @param performanceTest to run.
     * @param params          with which to run.
     * @return time the test has taken in microseconds.
     */
    private long run(PerformanceTest performanceTest, Map<String, Object> params) {
        if (performanceTest.rebuildDatabase().equals(PerformanceTest.RebuildDatabase.TEST_DECIDES)
                && performanceTest.rebuildDatabase(params)) {
            closeDatabase();
        }

        createDatabaseIfNeeded(performanceTest, params);

        long result = performanceTest.run(database, params);

        if (performanceTest.rebuildDatabase().equals(PerformanceTest.RebuildDatabase.AFTER_EVERY_RUN)) {
            closeDatabase();
        }

        return result;
    }

    /**
     * Create the database for the given performance test, if needed.
     *
     * @param performanceTest to create a database for.
     * @param params          with which the test will be run.
     */
    private void createDatabaseIfNeeded(PerformanceTest performanceTest, Map<String, Object> params) {
        if (database != null) {
            return;
        }

        LOG.info("(Re)creating database...");

        createTemporaryFolder();
        createDatabase(performanceTest, params);
    }

    /**
     * Create the database for the given performance test.
     *
     * @param performanceTest to create a database for.
     * @param params          with which the test will be run.
     */
    private void createDatabase(PerformanceTest performanceTest, Map<String, Object> params) {
        GraphDatabaseBuilder graphDatabaseBuilder = createGraphDatabaseBuilder();

        Map<String, String> dbConfig = performanceTest.databaseParameters(params);
        if (dbConfig != null) {
            graphDatabaseBuilder = graphDatabaseBuilder.setConfig(dbConfig);
        }

        database = graphDatabaseBuilder.newGraphDatabase();

        registerShutdownHook(database);

        performanceTest.prepare(database, params);
    }

    /**
     * Create a builder for the database. By default, creates an embedded database builder set up to create an empty
     * database in a temporary folder.
     *
     * @return builder.
     */
    protected GraphDatabaseBuilder createGraphDatabaseBuilder() {
        return new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(temporaryFolder.getRoot());
    }

    /**
     * Create a temporary folder.
     */
    private void createTemporaryFolder() {
        temporaryFolder = new TemporaryFolder();
        try {
            temporaryFolder.create();
            temporaryFolder.getRoot().deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Close the database and delete its data.
     */
    private void closeDatabase() {
        if (database != null) {
            LOG.info("Closing database...");

            database.shutdown();
            temporaryFolder.delete();
            database = null;
        }
    }

    /**
     * Given a list of test parameters, generate all parameter combinations that tests will be run with.
     *
     * @param parameters params.
     * @return all parameter combination.
     */
    private List<Map<String, Object>> generateParameterCombinations(List<Parameter> parameters) {
        if (parameters.isEmpty()) {
            return Collections.singletonList(Collections.<String, Object>emptyMap());
        }

        Parameter parameter = parameters.remove(parameters.size() - 1);
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> existing : generateParameterCombinations(parameters)) {
            for (Object value : parameter.getValues()) {
                Map<String, Object> toAdd = new LinkedHashMap<>(existing);
                toAdd.put(parameter.getName(), value);
                result.add(toAdd);
            }
        }

        return result;
    }

    private String paramsToString(Map<String, Object> params) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            result.append(entry.getKey()).append("=").append(entry.getValue().toString()).append(", ");
        }
        return result.toString();
    }
}
