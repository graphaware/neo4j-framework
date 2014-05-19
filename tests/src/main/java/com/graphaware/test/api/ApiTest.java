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

package com.graphaware.test.api;

import com.graphaware.server.web.WebAppInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

import javax.servlet.ServletException;

/**
 * Abstract base-class for API tests. Starts a Neo4j server on the port specified in the constructor (or 8082 by default)
 * and deploys all {@link org.springframework.stereotype.Controller} annotated controllers. Before tests are run,
 * the database can be populated by overriding the {@link #populateDatabase()} method.
 * <p/>
 * The database running within the server can be accessed by calling {@link #getDatabase()}. Therefore, this class is
 * good for writing API integration tests that want to verify the database state after certain API calls.
 */
public abstract class ApiTest {

    private static final int PORT = 8082;

    private final int port;
    private Server server;
    private GraphDatabaseService database;

    protected ApiTest() {
        this(PORT);
    }

    protected ApiTest(int port) {
        this.port = port;
    }

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        populateDatabase();

        startJetty();
    }

    @After
    public void tearDown() throws Exception {
        database.shutdown();
        server.stop();
    }

    protected void populateDatabase() {
        //for subclasses
    }

    public GraphDatabaseService getDatabase() {
        return database;
    }

    public int getPort() {
        return port;
    }

    private void startJetty() {
        server = new Server(port);

        final ServletContextHandler handler = new ServletContextHandler(null, "/graphaware", ServletContextHandler.SESSIONS);

        handler.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarting(LifeCycle event) {
                try {
                    new WebAppInitializer(database).onStartup(handler.getServletContext());
                } catch (ServletException e) {
                    throw new RuntimeException();
                }
            }
        });

        server.setHandler(handler);

        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
