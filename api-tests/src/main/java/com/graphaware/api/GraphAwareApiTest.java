package com.graphaware.api;

import com.graphaware.server.web.WebAppInitializer;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 *
 */
public abstract class GraphAwareApiTest {

    protected static final int PORT = 8082;

    private Server server;
    protected GraphDatabaseService database;

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

    protected final void startJetty() {
        server = new Server(PORT);

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

    protected String jsonAsString(String fileName) {
        try {
            String path = this.getClass().getPackage().getName().replace(".", "/") + "/";
            return IOUtils.toString(new ClassPathResource(path + fileName + ".json").getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
