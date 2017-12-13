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

package com.graphaware.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphaware.test.integration.GraphAwareIntegrationTest;
import org.apache.http.HttpStatus;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.server.enterprise.helpers.EnterpriseServerBuilder;
import org.neo4j.server.helpers.CommunityServerBuilder;

import java.io.IOException;
import java.util.Collections;

public class EnterpriseTxParticipationIntegrationTest extends GraphAwareIntegrationTest {

    @Override
    protected CommunityServerBuilder createServerBuilder() {
        return EnterpriseServerBuilder.server();
    }

    @Test
    public void invalidTransactionShouldResultInException() {
        httpClient.get(baseUrl() + "/greeting", Collections.singletonMap("_GA_TX_ID", "invalid"), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void nonExistingTransactionShouldResultInException() {
        httpClient.get(baseUrl() + "/greeting", Collections.singletonMap("_GA_TX_ID", "1"), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void moduleApiShouldParticipateInOpenTransaction() throws IOException {
        //First transaction over Cypher transactional rest endpoint, keep open:
        String response = httpClient.post(baseNeoUrl() + "/db/data/transaction", "{\n" +
                "  \"statements\" : [ {\n" +
                "    \"statement\" : \"CREATE (p:Person {props}) RETURN id(p)\",\n" +
                "    \"parameters\" : {\n" +
                "      \"props\" : {\n" +
                "        \"name\" : \"Michal\"\n" +
                "      }\n" +
                "    }\n" +
                "  } ]\n" +
                "}", HttpStatus.SC_CREATED);

        String commitUrl = new ObjectMapper().readTree(response).get("commit").asText();
        String txUrl = commitUrl.substring(0, commitUrl.length() - "commit".length());

        //Second transaction over Cypher transactional rest endpoint, keep open:
        httpClient.post(txUrl, "{\n" +
                "  \"statements\" : [ {\n" +
                "    \"statement\" : \"CREATE (p:Person {props}) RETURN id(p)\",\n" +
                "    \"parameters\" : {\n" +
                "      \"props\" : {\n" +
                "        \"name\" : \"Daniela\"\n" +
                "      }\n" +
                "    }\n" +
                "  } ]\n" +
                "}", HttpStatus.SC_OK);

        //Third transaction over REST to an extension
        httpClient.post(baseUrl() + "/link/0/1", null, Collections.singletonMap("_GA_TX_ID", "1"), HttpStatus.SC_CREATED);

        //Commit transaction over transactional endpoint
        httpClient.post(commitUrl, HttpStatus.SC_OK);

        httpClient.post(baseUrl() + "/resttest/assertSameGraph", "{\"cypher\": \"CREATE (m:Person {name:'Michal'})-[:TEST]->(d:Person {name:'Daniela'})\"}", HttpStatus.SC_OK);
    }
}
