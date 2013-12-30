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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.RelationshipType;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Unit test for {@link com.graphaware.server.plugin.algo.path.NumberOfShortestPaths}.
 */
public class NumberOfShortestPathsIntegrationTest {

    private static final int PORT = 8082;
    private static Server server;

    private RestTemplate restTemplate = new RestTemplate();

    private static final String COST = "cost";

    private static final String SERVER_URL = "http://localhost:7474";

    private enum RelTypes implements RelationshipType {
        R1, R2
    }

    @BeforeClass
    public static void startJetty() throws Exception {
        server = new Server(PORT);

        final ServletContextHandler handler = new ServletContextHandler(null, "/graphaware", ServletContextHandler.SESSIONS);

        handler.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarting(LifeCycle event) {
                try {
                    new TestWebAppInitializer().onStartup(handler.getServletContext());
                } catch (ServletException e) {
                    throw new RuntimeException();
                }
            }
        });

        server.setHandler(handler);

        server.start();
    }

    @AfterClass
    public static void stopJetty() throws Exception {
        server.stop();
    }

    private static class TestWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

        @Override
        protected Class<?>[] getRootConfigClasses() {
            return new Class<?>[0];
        }

        @Override
        protected Class<?>[] getServletConfigClasses() {
            return new Class<?>[]{AppConfig.class};
        }

        @Override
        protected String[] getServletMappings() {
            return new String[]{"/"};
        }
    }

    @Test
    public void test() throws InterruptedException, IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("http://localhost:8082/graphaware/api/library/algorithm/path/increasinglyLongerShortestPath");
            httpPost.setEntity(new StringEntity());
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                public String handleResponse(final HttpResponse response) throws IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            String responseBody = httpClient.execute(httpPost, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            System.out.println("----------------------------------------");
        }
    }

//    private Bootstrapper bootstrapper;
//    private GraphDatabaseService database;
//    private Node one;
//    private Node three;
//
//    /**
//     * Graph:
//     * (1)-[:R2 {cost:1}]->(2)-[:R1 {cost:1}]->(3)
//     * (1)-[:R1 {cost:1}]->(4)-[:R1 {cost:2}]->(5)-[:R1 {cost:1}]->(3)
//     * (4)-[:R1 {cost:1}]->(2)
//     */
//    @Before
//    public void setUp() {
//        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
//
//        try (Transaction tx = database.beginTx()) {
//            database.createNode(); //ID = 0
//            one = database.createNode();
//            Node two = database.createNode();
//            three = database.createNode();
//            Node four = database.createNode();
//            Node five = database.createNode();
//            database.createNode(); //six = disconnected
//
//            one.createRelationshipTo(two, RelTypes.R2).setProperty(COST, 1);
//            two.createRelationshipTo(three, RelTypes.R1).setProperty(COST, 1);
//            one.createRelationshipTo(four, RelTypes.R1).setProperty(COST, 1);
//            four.createRelationshipTo(five, RelTypes.R1).setProperty(COST, 2);
//            five.createRelationshipTo(three, RelTypes.R1).setProperty(COST, 1);
//            four.createRelationshipTo(two, RelTypes.R1).setProperty(COST, 1);
//
//            tx.success();
//        }
//
//        bootstrapper = new WrappingNeoServerBootstrapper((AbstractGraphDatabase) database);
//        bootstrapper.start();
//    }
//
//    @After
//    public void tearDown() {
//        bootstrapper.stop();
//        database.shutdown();
//    }
//
//    @Test
//    public void shouldReturnAllPaths() throws IOException {
//        HttpClient client = new HttpClient();
//        PostMethod method = new PostMethod(SERVER_URL + "/db/data/ext/NumberOfShortestPaths/node/1/paths");
//        method.setRequestBody(new NameValuePair[]{new NameValuePair("target", "3")});
//
//        assertEquals(200, client.executeMethod(method));
//        assertEquals("[ {\n" +
//                "  \"start\" : \"" + SERVER_URL + "/db/data/node/1\",\n" +
//                "  \"nodes\" : [ \"" + SERVER_URL + "/db/data/node/1\", \"http://localhost:7474/db/data/node/2\", \"http://localhost:7474/db/data/node/3\" ],\n" +
//                "  \"length\" : 2,\n" +
//                "  \"relationships\" : [ \"" + SERVER_URL + "/db/data/relationship/0\", \"http://localhost:7474/db/data/relationship/1\" ],\n" +
//                "  \"end\" : \"" + SERVER_URL + "/db/data/node/3\"\n" +
//                "}, {\n" +
//                "  \"start\" : \"" + SERVER_URL + "/db/data/node/1\",\n" +
//                "  \"nodes\" : [ \"" + SERVER_URL + "/db/data/node/1\", \"http://localhost:7474/db/data/node/4\", \"http://localhost:7474/db/data/node/5\", \"http://localhost:7474/db/data/node/3\" ],\n" +
//                "  \"length\" : 3,\n" +
//                "  \"relationships\" : [ \"" + SERVER_URL + "/db/data/relationship/2\", \"http://localhost:7474/db/data/relationship/3\", \"http://localhost:7474/db/data/relationship/4\" ],\n" +
//                "  \"end\" : \"" + SERVER_URL + "/db/data/node/3\"\n" +
//                "} ]", method.getResponseBodyAsString());
//
//        method.releaseConnection();
//    }
//
//    @Test
//    public void shouldReturnNoPathsWhenNoneExist() throws IOException {
//        HttpClient client = new HttpClient();
//        PostMethod method = new PostMethod(SERVER_URL + "/db/data/ext/NumberOfShortestPaths/node/1/paths");
//        method.setRequestBody(new NameValuePair[]{new NameValuePair("target", "6")});
//
//        assertEquals(200, client.executeMethod(method));
//        assertEquals("[ ]", method.getResponseBodyAsString());
//
//        method.releaseConnection();
//    }
//
//    @Test
//    public void shouldReturnErrorCodeWhenSourceDoesNotExist() throws IOException {
//        HttpClient client = new HttpClient();
//        PostMethod method = new PostMethod(SERVER_URL + "/db/data/ext/NumberOfShortestPaths/node/7/paths");
//        method.setRequestBody(new NameValuePair[]{new NameValuePair("target", "6")});
//
//        assertEquals(404, client.executeMethod(method));
//
//        method.releaseConnection();
//    }
//
//    @Test
//    public void shouldReturnErrorCodeWhenTargetDoesNotExist() throws IOException {
//        HttpClient client = new HttpClient();
//        PostMethod method = new PostMethod(SERVER_URL + "/db/data/ext/NumberOfShortestPaths/node/1/paths");
//        method.setRequestBody(new NameValuePair[]{new NameValuePair("target", "7")});
//
//        assertEquals(400, client.executeMethod(method));
//
//        method.releaseConnection();
//    }
//
//    @Test
//    public void printApi() throws IOException {
//        HttpClient client = new HttpClient();
//        GetMethod method = new GetMethod(SERVER_URL + "/db/data/ext/NumberOfShortestPaths/node/1/paths");
//
//        assertEquals(200, client.executeMethod(method));
//        System.out.println(method.getResponseBodyAsString());
//
//        method.releaseConnection();
//    }
}
