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

package com.graphaware.test.util;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Http client for testing APIs.
 */
public class TestHttpClient {

    private static final Log LOG = LoggerFactory.getLogger(TestHttpClient.class);

    private final CloseableHttpClient client;

    /**
     * Create a default client.
     */
    public TestHttpClient() {
        this(HttpClientBuilder.create());
    }

    /**
     * Create a client from the provided builder.
     *
     * @param builder builder.
     */
    public TestHttpClient(HttpClientBuilder builder) {
        this.client = builder.build();
    }

    /**
     * Issue an HTTP GET and assert the response status code.
     *
     * @param url                to GET.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    public String get(String url, final int expectedStatusCode) {
        return get(url, Collections.<String, String>emptyMap(), expectedStatusCode);
    }

    /**
     * Issue an HTTP GET and assert the response status code.
     *
     * @param url                to GET.
     * @param headers            request headers as map.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    public String get(String url, Map<String, String> headers, final int expectedStatusCode) {
        HttpGet method = new HttpGet(url);

        setHeaders(method, headers);

        return method(method, expectedStatusCode);
    }

    /**
     * Issue an HTTP POST with empty body and assert the response status code.
     *
     * @param url                to POST to.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    public String post(String url, final int expectedStatusCode) {
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
    public String post(String url, String json, final int expectedStatusCode) {
        return post(url, json, Collections.<String, String>emptyMap(), expectedStatusCode);
    }

    /**
     * Issue an HTTP POST and assert the response status code.
     *
     * @param url                to POST to.
     * @param json               request body.
     * @param headers            request headers as map.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    public String post(String url, String json, Map<String, String> headers, final int expectedStatusCode) {
        return post(url, json, headers, Collections.<String, String>emptyMap(), expectedStatusCode);
    }

    /**
     * Issue an HTTP POST and assert the response status code.
     *
     * @param url                to POST to.
     * @param json               request body.
     * @param headers            request headers as map.
     * @param params             request parameters as map.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    public String post(String url, String json, Map<String, String> headers, Map<String, String> params, final int expectedStatusCode) {
        HttpPost post = new HttpPost(url);
        if (json != null) {
            post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        }

        setHeaders(post, headers);
        setParams(post, params);

        return method(post, expectedStatusCode);
    }

    /**
     * Issue an HTTP PUT with empty body and assert the response status code.
     *
     * @param url                to PUT to.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    public String put(String url, final int expectedStatusCode) {
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
    public String put(String url, String json, final int expectedStatusCode) {
        return put(url, json, Collections.<String, String>emptyMap(), expectedStatusCode);
    }

    /**
     * Issue an HTTP PUT and assert the response status code.
     *
     * @param url                to POST to.
     * @param json               request body.
     * @param headers            request headers as map.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    public String put(String url, String json, Map<String, String> headers, final int expectedStatusCode) {
        HttpPut put = new HttpPut(url);
        if (json != null) {
            put.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        }

        setHeaders(put, headers);

        return method(put, expectedStatusCode);
    }

    /**
     * Issue an HTTP DELETE and assert the response status code.
     *
     * @param url                to DELETE.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    public String delete(String url, final int expectedStatusCode) {
        return delete(url, Collections.<String, String>emptyMap(), expectedStatusCode);
    }

    /**
     * Issue an HTTP DELETE and assert the response status code.
     *
     * @param url                to DELETE.
     * @param headers            request headers as map.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    public String delete(String url, Map<String, String> headers, final int expectedStatusCode) {
        HttpDelete method = new HttpDelete(url);

        setHeaders(method, headers);

        return method(method, expectedStatusCode);
    }

    /**
     * Issue a HTTP call and assert the response status code.
     *
     * @param method             HTTP method.
     * @param expectedStatusCode expected status code.
     * @return the body of the response.
     */
    public String method(HttpRequestBase method, final int expectedStatusCode) {
        try {
            String result = client.execute(method, responseHandler(expectedStatusCode));

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
    public String executeCypher(String serverUrl, String... cypherStatements) {
        return executeCypher(serverUrl, Collections.<String, String>emptyMap(), cypherStatements);
    }

    /**
     * Execute a set of cypher statements against a database in a single transaction.
     *
     * @param serverUrl        URL of the database server.
     * @param headers          request headers as map.
     * @param cypherStatements to execute.
     * @return body of the server response.
     */
    public String executeCypher(String serverUrl, Map<String, String> headers, String... cypherStatements) {
        StringBuilder stringBuilder = new StringBuilder("{\"statements\" : [");
        for (String statement : cypherStatements) {
            stringBuilder.append("{\"statement\" : \"").append(statement).append("\"}").append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        stringBuilder.append("]}");

        while (serverUrl.endsWith("/")) {
            serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        }

        return post(serverUrl + "/db/data/transaction/commit", stringBuilder.toString(), headers, HttpStatus.SC_OK);
    }

    protected void setHeaders(HttpRequestBase method, Map<String, String> headers) {
        if (headers == null) {
            return;
        }

        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
            method.setHeader(headerEntry.getKey(), headerEntry.getValue());
        }
    }

    protected void setParams(HttpRequestBase method, Map<String, String> params) {
        if (params == null) {
            return;
        }

        HttpParams httpParams = new BasicHttpParams();
        for (Map.Entry<String, String> paramEntry : params.entrySet()) {
            httpParams.setParameter(paramEntry.getKey(), paramEntry.getValue());
        }
        method.setParams(httpParams);
    }

    /**
     * Create a response handler. By default, it returns the body of the response and verifies expected status code
     * using JUnit assert.
     *
     * @param expectedStatusCode expected status code.
     * @return response body. Can be <code>null</code> of the response has no body.
     */
    protected ResponseHandler<String> responseHandler(final int expectedStatusCode) {
        return new ResponseHandler<String>() {
            @Override
            public String handleResponse(final HttpResponse response) throws IOException {
                String body = null;
                if (response.getEntity() != null) {
                    body = EntityUtils.toString(response.getEntity());
                }
                assertEquals("Expected and actual status codes don't match. Response body: " + body, expectedStatusCode, response.getStatusLine().getStatusCode());
                return body;
            }
        };
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
