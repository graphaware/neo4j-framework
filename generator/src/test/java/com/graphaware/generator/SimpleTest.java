/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.graphaware.generator;

import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.ArrayList;
import java.util.Arrays;

import static com.graphaware.common.util.IterableUtils.count;
import static org.junit.Assert.assertEquals;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * @author Vojtech Havlicek (Graphaware)
 */
public class SimpleTest extends DatabaseIntegrationTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected GraphDatabaseService createDatabase() {
        return new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    /**
     * Test of generateGraph method, of class Simple.
     */
    @Test
    public void testGenerateGraph() {
        System.out.println("generateGraph");

        ArrayList<Integer> distribution = new ArrayList<>(Arrays.asList(2, 2, 2, 2));

        new Simple(getDatabase()).generateGraph(distribution);

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(4, count(at(getDatabase()).getAllNodes()));
            assertEquals(4, count(at(getDatabase()).getAllRelationships()));

            for (Node node : at(getDatabase()).getAllNodes()) {
                assertEquals(2, node.getDegree());
            }

            tx.success();
        }
    }

}
