package com.graphaware.runtime.walk;

import com.graphaware.common.policy.fluent.IncludeNodes;
import com.graphaware.common.policy.none.IncludeNoNodes;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.assertEquals;
import static org.neo4j.graphdb.DynamicLabel.label;

/**
 *  Test for {@link ContinuousNodeSelector}.
 */
public class ContinuousNodeSelectorTest extends DatabaseIntegrationTest {

    @Override
    protected void populateDatabase(GraphDatabaseService database) {
        database.createNode(label("Person")).setProperty("name", "Michal");
        database.createNode(label("Person")).setProperty("name", "Daniela");
        database.createNode(label("Person")).setProperty("name", "Vince");
        database.createNode(label("Company")).setProperty("name", "GraphAware");
        database.createNode(label("Company")).setProperty("name", "Neo");
        database.createNode(label("Person")).setProperty("name", "Adam");

        database.getNodeById(2).delete();
    }

    @Test
    public void shouldSelectCorrectNodes() {
        NodeSelector selector = new ContinuousNodeSelector(IncludeNodes.all().with(label("Person")));

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals("Michal", selector.selectNode(getDatabase()).getProperty("name"));
            assertEquals("Daniela", selector.selectNode(getDatabase()).getProperty("name"));
            assertEquals("Adam", selector.selectNode(getDatabase()).getProperty("name"));
            assertEquals("Michal", selector.selectNode(getDatabase()).getProperty("name"));
            assertEquals("Daniela", selector.selectNode(getDatabase()).getProperty("name"));
            assertEquals("Adam", selector.selectNode(getDatabase()).getProperty("name"));

            tx.success();
        }
    }

    @Test
    public void shouldTerminateWhenNoSuitableNodesExist() {
        NodeSelector selector = new ContinuousNodeSelector(IncludeNoNodes.getInstance());

        try (Transaction tx = getDatabase().beginTx()) {
            selector.selectNode(getDatabase());
            tx.success();
        }
    }
}
