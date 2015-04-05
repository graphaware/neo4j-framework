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

import com.graphaware.test.util.TestHttpClient;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.neo4j.server.configuration.ServerConfigurator;
import org.neo4j.server.configuration.ServerSettings;
import org.neo4j.server.configuration.ThirdPartyJaxRsPackage;

import java.util.Collections;
import java.util.Map;

/**
 * {@link DatabaseIntegrationTest} that starts the {@link WrappingNeoServerBootstrapper},
 * in order to make the Neo4j browser and potentially custom managed and unmanaged extensions available for testing.
 * <p/>
 * This is generally useful for developers who use Neo4j in server mode and want to test their extensions, whilst
 * being able to access the {@link org.neo4j.graphdb.GraphDatabaseService} object using {@link #getDatabase()},
 * for example to run {@link com.graphaware.test.unit.GraphUnit} test cases on it.
 * <p/>
 * Unmanaged extensions are registered by overriding the {@link #thirdPartyJaxRsPackageMappings()} and providing
 * key-value pairs, where key is the package in which extensions live and value is the URI of the mount point.
 * <p/>
 * By overriding {@link #neoServerPort()}, you can change the port number of which the server runs (7575 by default).
 * <p/>
 * By overriding {@link #additionalServerConfiguration()}, you can provide additional server configuration (which would
 * normally live in neo4j-server.properties).
 * <p/>
 * For testing pure Spring MVC code that uses the GraphAware Framework, please use {@link com.graphaware.test.integration.GraphAwareApiTest}.
 */
public abstract class WrappingServerIntegrationTest extends DatabaseIntegrationTest {

    private static final int DEFAULT_NEO_PORT = 7575;

    private WrappingNeoServerBootstrapper bootstrapper;

    protected TestHttpClient httpClient;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        startServerWrapper();
        httpClient = createHttpClient();
    }

    @Override
    public void tearDown() throws Exception {
        httpClient.close();
        bootstrapper.stop();
        super.tearDown();
    }

    protected TestHttpClient createHttpClient() {
        return new TestHttpClient();
    }

    /**
     * Start the server wrapper.
     * //todo change to CommunityServerBuilder
     */
    private void startServerWrapper() {
        ServerConfigurator configurator = new ServerConfigurator((GraphDatabaseAPI) getDatabase());
        populateConfigurator(configurator);
        bootstrapper = createBootstrapper(configurator);
        bootstrapper.start();
    }

    protected WrappingNeoServerBootstrapper createBootstrapper(ServerConfigurator configurator) {
        return new WrappingNeoServerBootstrapper((GraphDatabaseAPI) getDatabase(), configurator);
    }

    /**
     * Populate server configurator with additional configuration. This method should rarely be overridden. In order to
     * register extensions, provide additional server config (including changing the port on which the server runs),
     * please override one of the methods below.
     *
     * @param configurator to populate.
     */
    protected void populateConfigurator(ServerConfigurator configurator) {
        for (Map.Entry<String, String> mapping : thirdPartyJaxRsPackageMappings().entrySet()) {
            configurator.getThirdpartyJaxRsPackages().add(new ThirdPartyJaxRsPackage(mapping.getKey(), mapping.getValue()));
        }

        configurator.configuration().addProperty(ServerSettings.webserver_port.name(), neoServerPort());
        configurator.configuration().addProperty(ServerSettings.auth_enabled.name(), authEnabled());

        for (Map.Entry<String, String> config : additionalServerConfiguration().entrySet()) {
            configurator.configuration().addProperty(config.getKey(), config.getValue());
        }
    }

    /**
     * Provide information for registering unmanaged extensions.
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
}
