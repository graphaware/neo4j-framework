/*
 * Copyright (c) 2015 GraphAware
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

package com.graphaware.test.integration;

import com.graphaware.test.data.DatabasePopulator;
import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;

/**
 * Base class for all kinds of Neo4j integration tests.
 * <p/>
 * Creates an {@link org.neo4j.test.ImpermanentGraphDatabase} (by default) at the beginning of each test and allows
 * subclasses to populate it by overriding the {@link #populateDatabase(org.neo4j.graphdb.GraphDatabaseService)} method,
 * or by providing a {@link com.graphaware.test.data.DatabasePopulator} by overriding the {@link #databasePopulator()} method.
 * <p/>
 * Shuts the database down at the end of each test.
 */
public abstract class DatabaseIntegrationTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() throws Exception {
        database = createDatabase();
        populateDatabase(database);
    }

    @After
    public void tearDown() throws Exception {
        database.shutdown();
    }

    /**
     * Instantiate a db builder.
     *
     * @return builder.
     */
    protected GraphDatabaseBuilder createGraphDatabaseBuilder() {
        return new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder();
    }

    /**
     * Instantiate a database. By default this will be {@link org.neo4j.test.ImpermanentGraphDatabase}.
     *
     * @return new database.
     */
    protected GraphDatabaseService createDatabase() {
        GraphDatabaseBuilder builder = createGraphDatabaseBuilder();

        if (propertiesFile() != null) {
            builder = builder.loadPropertiesFromFile(propertiesFile());
        }
        else {
            populateConfig(builder);
        }

        GraphDatabaseService database = builder.newGraphDatabase();
        registerShutdownHook(database);
        return database;
    }

    /**
     * Provide config on a {@link GraphDatabaseBuilder}. Only called iff {@link #propertiesFile()} returns <code>null</code>.
     *
     * @param builder to populate config on.
     */
    protected void populateConfig(GraphDatabaseBuilder builder) {

    }

    /**
     * Get the name of properties file used to configure the database.
     *
     * @return properties file, <code>null</code> for none.
     */
    protected String propertiesFile() {
        return null;
    }

    /**
     * Populate the database. Can be overridden. By default, it populates the database using {@link #databasePopulator()}.
     *
     * @param database to populate.
     */
    protected void populateDatabase(GraphDatabaseService database) {
        DatabasePopulator populator = databasePopulator();
        if (populator != null) {
            populator.populate(database);
        }
    }

    /**
     * @return {@link com.graphaware.test.data.DatabasePopulator}, <code>null</code> (no population) by default.
     */
    protected DatabasePopulator databasePopulator() {
        return null;
    }

    /**
     * Get the database instantiated for this test.
     *
     * @return database.
     */
    protected GraphDatabaseService getDatabase() {
        return database;
    }
}
