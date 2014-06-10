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

package com.graphaware.test.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Utilities mainly intended for testing.
 */
public final class TestUtils {

    private static final Logger LOG = Logger.getLogger(TestUtils.class);

    /**
     * Assert that two JSON objects represented as Strings are semantically equal.
     *
     * @param one one.
     * @param two two.
     */
    public static void assertJsonEquals(String one, String two) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            assertTrue(mapper.readTree(one).equals(mapper.readTree(two)));
        } catch (IOException e) {
            fail();
        }
    }

    /**
     * Convert a JSON file to String.
     *
     * @param fileName name of the file present in the root of the resources directory.
     * @return JSON as String.
     */
    public static String jsonAsString(String fileName) {
        return jsonAsString("", fileName);
    }

    /**
     * Convert a JSON file to String.
     *
     * @param caller   the class calling this method. The file is expected to be in the resources directory in the same
     *                 package as this class.
     * @param fileName name of the file present in the resources directory in the same package as the class above.
     * @return JSON as String.
     */
    public static String jsonAsString(Class caller, String fileName) {
        return jsonAsString(caller.getClass().getPackage().getName().replace(".", "/") + "/", fileName);
    }

    /**
     * Convert a JSON file to String.
     *
     * @param packagePath path to package. The file is expected to be in the resources directory in the same
     *                    package.
     * @param fileName    name of the file present in the resources directory in the package defined above.
     * @return JSON as String.
     */
    public static String jsonAsString(String packagePath, String fileName) {
        try {
            return IOUtils.toString(new ClassPathResource(packagePath + fileName + ".json").getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Issue an HTTP GET and assert the response status code.
     *
     * @param url                to GET.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    public static String get(String url, final int expectedStatusCode) {
        return method(new HttpGet(url), expectedStatusCode);
    }

    /**
     * Issue an HTTP POST with empty body and assert the response status code.
     *
     * @param url                to POST to.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    public static String post(String url, final int expectedStatusCode) {
        return post(url, null, expectedStatusCode);
    }

    /**
     * Issue an HTTP POST and assert the response status code.
     *
     * @param url                to POST to.
     * @param json               request body.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    public static String post(String url, String json, final int expectedStatusCode) {
        HttpPost post = new HttpPost(url);
        if (json != null) {
            post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        }

        return method(post, expectedStatusCode);
    }

    /**
     * Issue an HTTP PUT with empty body and assert the response status code.
     *
     * @param url                to PUT to.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    public static String put(String url, final int expectedStatusCode) {
        return put(url, null, expectedStatusCode);
    }

    /**
     * Issue an HTTP PUT and assert the response status code.
     *
     * @param url                to POST to.
     * @param json               request body.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    public static String put(String url, String json, final int expectedStatusCode) {
        HttpPut put = new HttpPut(url);
        if (json != null) {
            put.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        }

        return method(put, expectedStatusCode);
    }

    /**
     * Issue an HTTP DELETE and assert the response status code.
     *
     * @param url                to DELETE.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    public static String delete(String url, final int expectedStatusCode) {
        return method(new HttpDelete(url), expectedStatusCode);
    }

    /**
     * Issue a HTTP call and assert the response status code.
     *
     * @param method             HTTP method.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    protected static String method(HttpRequestBase method, final int expectedStatusCode) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                @Override
                public String handleResponse(final HttpResponse response) throws IOException {
                    assertEquals(expectedStatusCode, response.getStatusLine().getStatusCode());
                    if (response.getEntity() != null) {
                        return EntityUtils.toString(response.getEntity());
                    } else {
                        return null;
                    }
                }
            };

            String result = httpClient.execute(method, responseHandler);

            LOG.debug("HTTP " + method.getMethod() + " returned: " + result);

            return result;

        } catch (IOException e) {
            fail(e.getMessage());
            return null;
        }
    }

    /**
     * Execute a set of cypher statements against a database in a single transaction.
     *
     * @param serverUrl        URL of the database server.
     * @param cypherStatements to execute.
     * @return body of the server response.
     */
    public static String executeCypher(String serverUrl, String... cypherStatements) {
        StringBuilder stringBuilder = new StringBuilder("{\"statements\" : [ {");
        for (String statement : cypherStatements) {
            stringBuilder.append("\"statement\" : \"").append(statement).append("\"");
        }
        stringBuilder.append("}]}");

        while (serverUrl.endsWith("/")) {
            serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        }

        return post(serverUrl + "/db/data/transaction/commit", stringBuilder.toString(), HttpStatus.SC_OK);
    }

    /**
     * Measure the time of the timed callback.
     *
     * @param timed callback.
     * @return time in microseconds
     */
    public static long time(Timed timed) {
        long start = System.nanoTime();
        timed.time();
        return (System.nanoTime() - start) / 1000;
    }

    /**
     * Timed operation.
     */
    public interface Timed {

        /**
         * Perform the operation to be timed.
         */
        void time();
    }

    private TestUtils() {
    }
}
