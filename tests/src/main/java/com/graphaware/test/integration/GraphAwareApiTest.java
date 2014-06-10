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

import com.graphaware.server.web.WebAppInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

import javax.servlet.ServletException;

/**
 * Base-class for tests of APIs that are written as Spring MVC {@link org.springframework.stereotype.Controller}s
 * and deployed using the GraphAware Framework.
 * <p/>
 * Starts a Neo4j server on the port specified using {@link #jettyServerPort()} (or 8082 by default) and deploys all
 * {@link org.springframework.stereotype.Controller} annotated controllers.
 * <p/>
 * Requires implementing tests to implement {@link #createDatabase()} and thus allows low-level access to the database
 * even when running within a server by calling {@link #getDatabase()}, for instance to allow using
 * {@link com.graphaware.test.unit.GraphUnit} to assert its state. Before tests are run, the database can be populated
 * by overriding the {@link #populateDatabase(org.neo4j.graphdb.GraphDatabaseService)} method.
 */
public abstract class GraphAwareApiTest extends JettyAndWrappingServerIntegrationTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected GraphDatabaseService createDatabase() {
        return new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modifyServer(Server server) {
        final ServletContextHandler handler = new ServletContextHandler(null, "/graphaware", ServletContextHandler.SESSIONS);

        handler.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarting(LifeCycle event) {
                try {
                    new WebAppInitializer(getDatabase()).onStartup(handler.getServletContext());
                } catch (ServletException e) {
                    throw new RuntimeException();
                }
            }
        });

        server.setHandler(handler);
    }

    /**
     * Get the URL against which tests would typically be executed.
     * @return base URL.
     */
    protected String baseUrl() {
        return baseJettyUrl() + "/graphaware";
    }
}
