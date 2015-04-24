/*
 * Copyright (c) 2013 GraphAware
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

import com.graphaware.test.util.TestHttpClient;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;

/**
 * Base class for server mode integration tests that are as close to real Neo4j server deployment as possible. As a consequence,
 * low-level access to {@link org.neo4j.graphdb.GraphDatabaseService} is not provided. Instead, use {@link com.graphaware.test.util.TestHttpClient#executeCypher(String, String...)}
 * to populate/query the database.
 * <p/>
 * The target audience of this class are developers of GraphAware Framework (Runtime) Modules.
 * The primary purpose of tests that extend this class should be to verify that given a certain Neo4j configuration,
 * a (possibly runtime) module is bootstrapped and started correctly when the Neo4j server starts.
 * <p/>
 * The configuration file names are provided by overriding the {@link #neo4jConfigFile()} and {@link #neo4jServerConfigFile()}
 * methods. They default to "neo4j.properties" and "neo4j-server.properties" and if no such files are present
 * on the classpath of the implementing class, the ones that ships with Neo4j are used.
 */
public abstract class NeoServerIntegrationTest {

    private NeoTestServer neoTestServer;

    protected TestHttpClient httpClient;

    @Before
    public void setUp() throws IOException, InterruptedException {
        neoTestServer = new NeoTestServer(neo4jConfigFile(), neo4jServerConfigFile());
        neoTestServer.start();
        httpClient = createHttpClient();
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        httpClient.close();
        neoTestServer.stop();
    }

    protected TestHttpClient createHttpClient() {
        return new TestHttpClient();
    }

    protected String baseUrl() {
        return "http://localhost:7575";
    }

    /**
     * Get the name of the neo4j config file on the classpath.
     *
     * @return config file name.
     */
    protected String neo4jConfigFile() {
        return "neo4j.properties";
    }

    /**
     * Get the name of the neo4j server config file on the classpath.
     *
     * @return config file name.
     */
    protected String neo4jServerConfigFile() {
        return "neo4j-server.properties";
    }
}
