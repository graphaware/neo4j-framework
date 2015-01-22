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

import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;

/**
 * Base class for all kinds of Neo4j integration tests.
 * <p/>
 * Creates an {@link org.neo4j.test.ImpermanentGraphDatabase} (by default) at the beginning of each test and allows
 * subclasses to populate it by overriding the {@link #populateDatabase(org.neo4j.graphdb.GraphDatabaseService)} method,
 * which is guaranteed to run in a transaction.
 * <p/>
 * Shuts the database down at the end of each test.
 */
public abstract class DatabaseIntegrationTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() throws Exception {
        database = createDatabase();
        populateDatabaseInTransaction();
    }

    @After
    public void tearDown() throws Exception {
        database.shutdown();
    }

    /**
     * Populate database in a transaction.
     */
    private void populateDatabaseInTransaction() {
        try (Transaction tx = database.beginTx()) {
            populateDatabase(database);
            tx.success();
        }
    }

    /**
     * Instantiate a database. By default this will be {@link org.neo4j.test.ImpermanentGraphDatabase}.
     *
     * @return new database.
     */
    protected GraphDatabaseService createDatabase() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        registerShutdownHook(database);
        return database;
    }

    /**
     * Populate the database that will drive this test. A transaction is running when this method gets called.
     *
     * @param database to populate.
     */
    protected void populateDatabase(GraphDatabaseService database) {
        //for subclasses
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
