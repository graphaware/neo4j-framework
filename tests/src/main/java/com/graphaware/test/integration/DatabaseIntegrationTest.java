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

package com.graphaware.test.integration;

import com.graphaware.test.data.DatabasePopulator;
import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.kernel.impl.proc.Procedures;

/**
 * Base class for all kinds of Neo4j integration tests.
 * <p>
 * Allows subclasses to create a database at the beginning of each test by overriding {@link #createDatabase()} and allows
 * them to populate it by overriding {@link #populateDatabase(org.neo4j.graphdb.GraphDatabaseService)},
 * or by providing a {@link com.graphaware.test.data.DatabasePopulator} by overriding {@link #databasePopulator()}.
 * <p>
 * Shuts the database down at the end of each test.
 */
public abstract class DatabaseIntegrationTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() throws Exception {
        database = createDatabase();
        populateDatabase(database);

        if (shouldRegisterProceduresAndFunctions()) {
            registerProceduresAndFunctions(((GraphDatabaseFacade) database).getDependencyResolver().resolveDependency(Procedures.class));
        }
    }

    @After
    public void tearDown() throws Exception {
        database.shutdown();
    }

    /**
     * Create a database.
     *
     * @return database.
     */
    protected abstract GraphDatabaseService createDatabase();

    /**
     * Get the name of config file used to configure the database.
     *
     * @return config file, <code>null</code> for none.
     */
    protected String configFile() {
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
     * @return <code>iff</code> the {@link #registerProceduresAndFunctions(Procedures)} method should be called during {@link #setUp()}.
     */
    protected boolean shouldRegisterProceduresAndFunctions() {
        return true;
    }

    /**
     * Register procedures and functions.
     *
     * @param procedures to register against.
     */
    protected void registerProceduresAndFunctions(Procedures procedures) throws Exception {
        //no-op by default
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
