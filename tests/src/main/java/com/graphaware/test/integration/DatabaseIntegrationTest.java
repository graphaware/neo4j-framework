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
import org.neo4j.server.NeoServer;
import org.neo4j.server.database.DatabaseService;
import org.neo4j.server.helpers.CommunityServerBuilder;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

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

    private NeoServer server;
    private GraphDatabaseService database;

    @BeforeEach
    public void setUp() throws Exception {
        CommunityServerBuilder builder = CommunityServerBuilder.serverOnRandomPorts();

        builder = configure(builder);

        if (shouldRegisterProceduresAndFunctions()) {
            builder = registerProceduresAndFunctions(builder);
        }

        server = builder.build();

        server.start();

        database = server.getDatabaseService().getDatabase();

        populateDatabase(database);
    }

    @AfterEach
    public void tearDown() throws Exception {
        server.stop();
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
    protected CommunityServerBuilder registerProceduresAndFunctions(CommunityServerBuilder builder) throws Exception {
        //no-op by default

        return builder;
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
    protected DatabaseService getServer() {
        return server.getDatabaseService();
    }

    public GraphDatabaseService getDatabase() {
        return database;
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
    protected CommunityServerBuilder configure(CommunityServerBuilder builder) throws IOException {
        for (Map.Entry<String, String> mapping : thirdPartyJaxRsPackageMappings().entrySet()) {
            builder = builder.withThirdPartyJaxRsPackage(mapping.getKey(), mapping.getValue());
        }

        builder = builder.withProperty(GraphDatabaseSettings.auth_enabled.name(), Boolean.toString(authEnabled()));

        if (configFile() != null) {
            Properties properties = new Properties();
            properties.load(new ClassPathResource(configFile()).getInputStream());
            for (String key : properties.stringPropertyNames()) {
                builder = builder.withProperty(key, properties.getProperty(key));
            }
        }

        for (Map.Entry<String, String> config : additionalServerConfiguration().entrySet()) {
            builder = builder.withProperty(config.getKey(), config.getValue());
        }

        return builder;
    }


    /**
     * Provide information for registering unmanaged extensions.
     * <p>
     * This method is only called iff {@link #configFile()} returns <code>null</code>.
     *
     * @return map where the key is the package in which a set of extensions live and value is the mount point of those
     * extensions, i.e., a URL under which they will be exposed relative to the server address
     * (typically http://localhost:7575 for tests).
     */
    protected Map<String, String> thirdPartyJaxRsPackageMappings() {
        return Collections.emptyMap();
    }

    /**
     * Provide additional server configuration.
     * <p>
     * This method is only called iff {@link #configFile()} returns <code>null</code>.
     *
     * @return map of configuration key-value pairs.
     */
    protected Map<String, String> additionalServerConfiguration() {
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
