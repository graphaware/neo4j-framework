/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilder;
import org.neo4j.harness.Neo4jBuilders;

import java.util.Collections;
import java.util.Map;

/**
 * Base class for all kinds of Neo4j integration tests.
 * <p>
 * Allows subclasses to populate a test database by overriding {@link #populateDatabase(org.neo4j.graphdb.GraphDatabaseService)},
 * or by providing a {@link com.graphaware.test.data.DatabasePopulator} by overriding {@link #databasePopulator()}.
 * <p>
 * Shuts the database down at the end of each test.
 */
public abstract class DatabaseIntegrationTest {

    private Neo4j database;

    @BeforeEach
    public void setUp() throws Exception {
        Neo4jBuilder builder = Neo4jBuilders.newInProcessBuilder();

        builder = configure(builder);

        if (shouldRegisterProceduresAndFunctions()) {
            builder = registerProceduresAndFunctions(builder);
        }

        database = builder.build();

        populateDatabase(database.defaultDatabaseService());
    }

    @AfterEach
    public void tearDown() throws Exception {
        database.close();
    }

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
     * @return <code>iff</code> the {@link #registerProceduresAndFunctions(TestServerBuilder)} method should be called during {@link #setUp()}.
     */
    protected boolean shouldRegisterProceduresAndFunctions() {
        return true;
    }

    /**
     * Register procedures and functions.
     *
     * @param builder to register against.
     */
    protected Neo4jBuilder registerProceduresAndFunctions(Neo4jBuilder builder) throws Exception {
        //no-op by default

        return builder;
    }

    /**
     * @return {@link com.graphaware.test.data.DatabasePopulator}, <code>null</code> (no population) by default.
     */
    protected DatabasePopulator databasePopulator() {
        return null;
    }

    protected Neo4j getNeo4j() {
        return database;
    }

    /**
     * Get the database instantiated for this test.
     *
     * @return database.
     */
    protected GraphDatabaseService getDatabase() {
        return database.defaultDatabaseService();
    }

    /**
     * Populate server configurator with additional configuration. This method should rarely be overridden. In order to
     * register extensions, provide additional server config (including changing the port on which the server runs),
     * please override one of the methods below.
     * <p>
     * This method is only called iff {@link #configFile()} returns <code>null</code>.
     *
     * @param builder to populate.
     */
    protected Neo4jBuilder configure(Neo4jBuilder builder) {
        builder = builder.withConfig(GraphDatabaseSettings.auth_enabled, authEnabled());

        if (configFile() != null) {
            IntegrationTestUtils.applyConfig(builder, configFile());
        }

        for (Map.Entry<Setting, String> config : additionalServerConfiguration().entrySet()) {
            builder = builder.withConfig(config.getKey(), config.getValue());
        }

        return builder;
    }

    /**
     * Provide additional server configuration.
     * <p>
     * This method is only called iff {@link #configFile()} returns <code>null</code>.
     *
     * @return map of configuration key-value pairs.
     */
    protected Map<Setting, String> additionalServerConfiguration() {
        return Collections.emptyMap();
    }

    /**
     * This method is only called iff {@link #configFile()} returns <code>null</code>.
     *
     * @return <code>true</code> iff Neo4j's native auth functionality should be enable, <code>false</code> (default) for disabled.
     */
    protected boolean authEnabled() {
        return false;
    }
}
