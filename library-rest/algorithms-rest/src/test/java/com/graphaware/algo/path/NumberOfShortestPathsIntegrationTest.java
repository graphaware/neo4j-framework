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

package com.graphaware.algo.path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphaware.server.web.WebAppInitializer;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.*;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Integration test for {@link com.graphaware.algo.path.NumberOfShortestPaths}.
 */
public class NumberOfShortestPathsIntegrationTest {

    private static final int PORT = 8082;
    public static final String POST_URL = "http://localhost:" + PORT + "/graphaware/api/library/algorithm/path/increasinglyLongerShortestPath";

    private static final String NAME = "name";
    private static final String COST = "cost";

    private Server server;
    private GraphDatabaseService database;

    private enum RelTypes implements RelationshipType {
        R1, R2
    }

    private enum Labels implements Label {
        L1, L2
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

    @Test
    public void minimalOutputShouldBeProducedWithLegalMinimalInput() {
        assertJsonEquals(post(jsonAsString("minimalInput")), jsonAsString("minimalOutput"));
    }

    @Test
    public void nodePropertiesShouldBeIncludedWhenRequested() {
        assertJsonEquals(post(jsonAsString("requestNodePropsInput")), jsonAsString("requestNodePropsOutput"));
    }

    @Test
    public void nonExistingNodePropertiesShouldNotBeIncluded() {
        assertJsonEquals(post(jsonAsString("requestNonExistingNodePropsInput")), jsonAsString("minimalOutput"));
    }

    @Test
    public void relationshipPropertiesShouldBeIncludedWhenRequested() {
        assertJsonEquals(post(jsonAsString("requestRelationshipPropsInput")), jsonAsString("requestRelationshipPropsOutput"));
    }

    @Test
    public void nonExistingRelationshipPropertiesShouldNotBeIncluded() {
        assertJsonEquals(post(jsonAsString("requestNonExistingRelationshipPropsInput")), jsonAsString("minimalOutput"));
    }

    @Test
    public void nodeLabelsShouldOnlyBeIncludedWhenRequested() {
        assertJsonEquals(post(jsonAsString("requestNoNodeLabelsInput")), jsonAsString("minimalOutput"));
        assertJsonEquals(post(jsonAsString("requestNodeLabelsInput")), jsonAsString("requestNodeLabelsOutput"));
    }

    @Test
    public void maxDepthAndMaxResultsShouldBeRespected() {
        assertJsonEquals(post(jsonAsString("maxDepthInput")), jsonAsString("maxDepthOutput"));
        assertJsonEquals(post(jsonAsString("maxResultsInput")), jsonAsString("maxResultsOutput"));
    }

    @Test
    public void nonExistingCostPropertyShouldNotChangeOrder() {
        assertJsonEquals(post(jsonAsString("nonExistingCostPropertyInput")), jsonAsString("nonExistingCostPropertyOutput"));
    }

    @Test
    public void costPropertyShouldBeTakenIntoAccount() {
        assertJsonEquals(post(jsonAsString("costPropertyInput")), jsonAsString("costPropertyOutput"));
    }

    private void assertJsonEquals(String one, String two) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            assertTrue(mapper.readTree(one).equals(mapper.readTree(two)));
        } catch (IOException e) {
            fail();
        }
    }

    private String jsonAsString(String fileName) {
        try {
            return IOUtils.toString(new ClassPathResource("com/graphaware/algo/path/"+fileName+".json").getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void populateDatabase() {
        try (Transaction tx = database.beginTx()) {
            Node one = database.createNode();
            Node two = database.createNode();
            Node three = database.createNode();
            Node four = database.createNode();
            Node five = database.createNode();
            Node six = database.createNode();
            Node seven = database.createNode();

            one.setProperty(NAME, "one");
            one.addLabel(Labels.L1);
            one.addLabel(Labels.L2);

            two.setProperty(NAME, "two");
            two.addLabel(Labels.L2);

            three.setProperty(NAME, "three");
            three.addLabel(Labels.L1);
            three.addLabel(Labels.L2);

            four.setProperty(NAME, "four");
            four.addLabel(Labels.L2);

            five.setProperty(NAME, "five");
            five.addLabel(Labels.L1);

            six.setProperty(NAME, "six");
            six.addLabel(Labels.L1);

            seven.setProperty(NAME, "seven");
            seven.addLabel(Labels.L1);

            one.createRelationshipTo(two, RelTypes.R2).setProperty(COST, 5);
            two.createRelationshipTo(three, RelTypes.R1).setProperty(COST, 1);
            one.createRelationshipTo(four, RelTypes.R1).setProperty(COST, 1);
            four.createRelationshipTo(five, RelTypes.R1).setProperty(COST, 2);
            five.createRelationshipTo(three, RelTypes.R1).setProperty(COST, 1);
            four.createRelationshipTo(two, RelTypes.R1).setProperty(COST, 1);
            one.createRelationshipTo(six, RelTypes.R1).setProperty(COST, 1);
            six.createRelationshipTo(seven, RelTypes.R1);
            seven.createRelationshipTo(three, RelTypes.R1).setProperty(COST, 1);

            tx.success();
        }
    }


    private void startJetty() {
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

    private String post(String json) {
        try {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(POST_URL);
                httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                    public String handleResponse(final HttpResponse response) throws IOException {
                        assertEquals(200, response.getStatusLine().getStatusCode());
                        if (response.getEntity() != null) {
                            return EntityUtils.toString(response.getEntity());
                        } else {
                            return null;
                        }
                    }
                };

                String execute = httpClient.execute(httpPost, responseHandler);
                System.out.println(execute);
                return execute;

            }
        } catch (IOException e) {
            fail();
            return null;
        }
    }
}
