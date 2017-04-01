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

import com.graphaware.test.util.TestHttpClient;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.helpers.HostnamePort;
import org.neo4j.helpers.ListenSocketAddress;
import org.neo4j.server.NeoServer;
import org.neo4j.server.enterprise.helpers.EnterpriseServerBuilder;
import org.neo4j.server.helpers.CommunityServerBuilder;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * {@link DatabaseIntegrationTest} that starts a {@link NeoServer},
 * in order to make the Neo4j browser and potentially custom managed and unmanaged extensions available for testing.
 * <p>
 * This is generally useful for developers who use Neo4j in server mode and want to test their extensions, whilst
 * being able to access the {@link org.neo4j.graphdb.GraphDatabaseService} object using {@link #getDatabase()},
 * for example to run {@link com.graphaware.test.unit.GraphUnit} test cases on it.
 * <p>
 * Unmanaged extensions are registered by overriding the {@link #thirdPartyJaxRsPackageMappings()} and providing
 * key-value pairs, where key is the package in which extensions live and value is the URI of the mount point.
 * <p>
 * By overriding {@link #neoServerPort()}, you can change the port number of which the server runs (7575 by default).
 * <p>
 * By overriding {@link #additionalServerConfiguration()}, you can provide additional server configuration (which would
 * normally live in neo4j.conf).
 * <p>
 * By overriding {@link #configFile()}, you can provide a name of a Neo4j configuration file, which must be one
 * the classpath. This file is then used to supply additional configuration.
 * <p>
 * Use {@link TestHttpClient} for convenient testing of REST APIs. And instance is provided by this class.
 * <p>
 * For testing Spring MVC code that uses the GraphAware Framework, please use {@link GraphAwareIntegrationTest}.
 */
public abstract class ServerIntegrationTest extends DatabaseIntegrationTest {

    private static final int DEFAULT_NEO_PORT = 7575;

    private NeoServer server;

    protected TestHttpClient httpClient;

    @Override
    public void setUp() throws Exception {
        if (autoStart()) {
            startServer(); //super.setUp() called there!
        }
        httpClient = createHttpClient();
    }

    @Override
    public void tearDown() throws Exception {
        if (httpClient != null) {
            httpClient.close();
        }
        stopServer();
        super.tearDown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final GraphDatabaseService createDatabase() {
        return server.getDatabase().getGraph();
    }

    /**
     * Creates a {@link TestHttpClient}. Override for configuring the client.
     *
     * @return test client.
     */
    protected TestHttpClient createHttpClient() {
        return new TestHttpClient();
    }

    /**
     * Start the server.
     */
    protected final void startServer() {
        try {
            doStartServer();
            super.setUp();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stop the server.
     */
    protected final void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    /**
     * Override and set to <code>false</code> in order to control the starting/stopping of the server within a test.
     * This is useful when the server needs to be started/stopped multiple times within a single test.
     *
     * @return <code>true</code> iff the server should start and stop automatically. Defaults to <code>true</code>.
     */
    protected boolean autoStart() {
        return true;
    }

    /**
     * Create the server builder. By default, this produces {@link CommunityServerBuilder} but can be overridden
     * to produce {@link EnterpriseServerBuilder} instead.
     *
     * @return server builder.
     */
    protected CommunityServerBuilder createServerBuilder() {
        return CommunityServerBuilder.server();
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
        builder = builder.onAddress(new ListenSocketAddress("localhost", neoServerPort()));

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
     * Provide the port number on which the server will run.
     *
     * @return port number.
     */
    protected int neoServerPort() {
        return DEFAULT_NEO_PORT;
    }

    /**
     * This method is only called iff {@link #configFile()} returns <code>null</code>.
     *
     * @return <code>true</code> iff Neo4j's native auth functionality should be enable, <code>false</code> (default) for disabled.
     */
    protected boolean authEnabled() {
        return false;
    }

    /**
     * Provide the base URL against which to execute tests.
     *
     * @return base URL.
     */
    protected String baseNeoUrl() {
        return "http://localhost:" + neoServerPort();
    }

    private void doStartServer() throws IOException {
        CommunityServerBuilder builder = createServerBuilder();

        builder = configure(builder);

        this.server = builder.build();
        this.server.start();
    }
}
