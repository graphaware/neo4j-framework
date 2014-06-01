package com.graphaware.example;

import com.graphaware.test.api.ApiTest;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static com.graphaware.test.util.TestUtils.get;
import static org.junit.Assert.assertEquals;

/**
 *  {@link ApiTest} for {@link NodeCountApi}.
 */
public class NodeCountApiTest extends ApiTest {

    @Test
    public void emptyDatabaseShouldHaveZeroNodes() {
          assertEquals("0", get("http://localhost:"+getPort()+"/graphaware/count", HttpStatus.SC_OK));
    }

    @Test
    public void whenTwoNodesAreCreatedThenDatabaseShouldHaveTwoNodes() {
        try (Transaction tx = getDatabase().beginTx()) {
            getDatabase().createNode();
            getDatabase().createNode();

            tx.success();
        }

        assertEquals("2", get("http://localhost:"+getPort()+"/graphaware/count", HttpStatus.SC_OK));
    }
}
