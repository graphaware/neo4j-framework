package ${package};

import com.graphaware.test.integration.GraphAwareApiTest;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static com.graphaware.test.util.TestUtils.get;
import static org.junit.Assert.assertEquals;

/**
 * {@link GraphAwareApiTest} for {@link NodeCountApi}.
 */
public class NodeCountApiTest extends GraphAwareApiTest {

    @Test
    public void emptyDatabaseShouldHaveZeroNodes() {
        assertEquals("0", get(baseUrl() + "/count", HttpStatus.SC_OK));
    }

    @Test
    public void whenTwoNodesAreCreatedThenDatabaseShouldHaveTwoNodes() {
        try (Transaction tx = getDatabase().beginTx()) {
            getDatabase().createNode();
            getDatabase().createNode();

            tx.success();
        }

        assertEquals("2", get(baseUrl() + "/count", HttpStatus.SC_OK));
    }
}
