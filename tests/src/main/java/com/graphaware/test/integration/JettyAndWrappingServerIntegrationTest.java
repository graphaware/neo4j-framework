/*
 * Copyright (c) 2014 GraphAware
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

import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;

/**
 * {@link WrappingServerIntegrationTest} that starts a Jetty server as well on port {@link #jettyServerPort()}, whilst
 * still providing access to {@link org.neo4j.graphdb.GraphDatabaseService} via {@link #getDatabase()}.
 * <p/>
 * <b>For internal framework use only.</b>
 */
public abstract class JettyAndWrappingServerIntegrationTest extends WrappingServerIntegrationTest {

    private static final int DEFAULT_JETTY_PORT = 8082;

    private Server server;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        startJetty();
    }

    @Override
    public void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

    private void startJetty() {
        server = new Server(jettyServerPort());

        modifyServer(server);

        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Provide the port number on which the server will run.
     *
     * @return port number.
     */
    protected int jettyServerPort() {
        return DEFAULT_JETTY_PORT;
    }

    /**
     * Modify the Jetty server by, for example, deploying custom servlet context handlers.
     *
     * @param server to modify.
     */
    protected void modifyServer(Server server) {
        //for subclasses
    }

    /**
     * Provide the base URL against which to execute tests.
     *
     * @return base URL.
     */
    protected String baseJettyUrl() {
        return "http://localhost:" + jettyServerPort();
    }
}
