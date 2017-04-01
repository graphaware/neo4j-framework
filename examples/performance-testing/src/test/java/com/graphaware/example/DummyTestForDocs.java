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

package com.graphaware.example;

import com.graphaware.test.performance.*;
import com.graphaware.tx.executor.NullItem;
import com.graphaware.tx.executor.batch.NoInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A {@link com.graphaware.test.performance.PerformanceTest} for documentation. Runs test for each of the scenarios
 * with 3 different {@link CacheConfiguration}s.
 */
public class DummyTestForDocs implements PerformanceTest {

    enum Scenario {
        SCENARIO_1,
        SCENARIO_2
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String shortName() {
        return "test-short-name";
    }

    @Override
    public String longName() {
        return "Test Long Name";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Parameter> parameters() {
        List<Parameter> result = new LinkedList<>();

        result.add(new CacheParameter("cache")); //no cache, low-level cache, high-level cache
        result.add(new EnumParameter("scenario", Scenario.class));

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int dryRuns(Map<String, Object> params) {
        return ((CacheConfiguration) params.get("cache")).needsWarmup() ? 10000 : 100;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int measuredRuns() {
        return 100;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> databaseParameters(Map<String, Object> params) {
        return ((CacheConfiguration) params.get("cache")).addToConfig(Collections.<String, String>emptyMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepare(GraphDatabaseService database, final Map<String, Object> params) {
        //create 100 nodes in batches of 100
        new NoInputBatchTransactionExecutor(database, 100, 100, new UnitOfWork<NullItem>() {
            @Override
            public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                database.createNode();
            }
        }).execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RebuildDatabase rebuildDatabase() {
        return RebuildDatabase.AFTER_PARAM_CHANGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long run(GraphDatabaseService database, Map<String, Object> params) {
        Scenario scenario = (Scenario) params.get("scenario");
        switch (scenario) {
            case SCENARIO_1:
                //run test for scenario 1
                return 20; //the time it took in microseconds
            case SCENARIO_2:
                //run test for scenario 2
                return 20; //the time it took in microseconds
            default:
                throw new IllegalStateException("Unknown scenario");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rebuildDatabase(Map<String, Object> params) {
        throw new UnsupportedOperationException("never needed, database rebuilt after every param change");
    }
}
