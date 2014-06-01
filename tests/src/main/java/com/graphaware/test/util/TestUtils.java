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
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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

    public static void assertJsonEquals(String one, String two) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            assertTrue(mapper.readTree(one).equals(mapper.readTree(two)));
        } catch (IOException e) {
            fail();
        }
    }

    public static String jsonAsString(String fileName) {
        return jsonAsString("", fileName);
    }

    public static String jsonAsString(Class caller, String fileName) {
        return jsonAsString(caller.getClass().getPackage().getName().replace(".", "/") + "/", fileName);
    }

    public static String jsonAsString(String packagePath, String fileName) {
        try {
            return IOUtils.toString(new ClassPathResource(packagePath + fileName + ".json").getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String post(String url, final int expectedStatusCode) {
        return post(url, null, expectedStatusCode);
    }

    public static String post(String url, String json, final int expectedStatusCode) {
        try {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(url);
                if (json != null) {
                    httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
                }

                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                    public String handleResponse(final HttpResponse response) throws IOException {
                        assertEquals(expectedStatusCode, response.getStatusLine().getStatusCode());
                        if (response.getEntity() != null) {
                            return EntityUtils.toString(response.getEntity());
                        } else {
                            return null;
                        }
                    }
                };

                String result = httpClient.execute(httpPost, responseHandler);

                LOG.debug(result);

                return result;

            }
        } catch (IOException e) {
            fail(e.getMessage());
            return null;
        }
    }

    public static String get(String url, final int expectedStatusCode) {
        try {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet httpGet = new HttpGet(url);

                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                    public String handleResponse(final HttpResponse response) throws IOException {
                        assertEquals(expectedStatusCode, response.getStatusLine().getStatusCode());
                        if (response.getEntity() != null) {
                            return EntityUtils.toString(response.getEntity());
                        } else {
                            return null;
                        }
                    }
                };

                String result = httpClient.execute(httpGet, responseHandler);

                LOG.debug(result);

                return result;

            }
        } catch (IOException e) {
            fail(e.getMessage());
            return null;
        }
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
