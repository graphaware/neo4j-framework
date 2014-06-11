package com.graphaware.test.integration;

import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 * Base class for all kinds of Neo4j integration tests.
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
     * Instantiate a database.
     *
     * @return new database.
     */
    protected abstract GraphDatabaseService createDatabase();

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
