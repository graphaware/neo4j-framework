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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.shell.ShellSettings;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static org.neo4j.kernel.configuration.Settings.FALSE;

/**
 * {@link DatabaseIntegrationTest} using an embedded database. Useful for low-level tests of algorithms, queries,
 * transaction event handlers, and much more, where the server isn't needed.
 */
public abstract class EmbeddedDatabaseIntegrationTest extends DatabaseIntegrationTest {

    /**
     * Instantiate a database. By default this will be {@link org.neo4j.test.ImpermanentGraphDatabase}.
     *
     * @return new database.
     */
    @Override
    protected GraphDatabaseService createDatabase() {
        GraphDatabaseBuilder builder = createGraphDatabaseBuilder();

        if (configFile() != null) {
            try {
                builder = builder.loadPropertiesFromFile(new ClassPathResource(configFile()).getFile().getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            populateConfig(builder);
        }

        GraphDatabaseService database = builder.newGraphDatabase();
        registerShutdownHook(database);
        return database;
    }

    /**
     * Instantiate a db builder.
     *
     * @return builder.
     */
    protected GraphDatabaseBuilder createGraphDatabaseBuilder() {
        return new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder(new File("target/test-data/impermanent-db-" + System.currentTimeMillis()))
                .setConfig("online_backup_enabled", FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE);
    }

    /**
     * Provide config on a {@link GraphDatabaseBuilder}. Only called iff {@link #configFile()} returns <code>null</code>.
     *
     * @param builder to populate config on.
     */
    protected void populateConfig(GraphDatabaseBuilder builder) {

    }
}
