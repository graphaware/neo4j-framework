/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.graphaware.generator;

import java.util.ArrayList;

import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.*;

/**
 *
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

        ArrayList<Integer> distribution = new ArrayList<>();
        distribution.add(2);
        distribution.add(2);
        distribution.add(2);
        distribution.add(2);
        
        Simple instance = new Simple(getDatabase());
        boolean result = instance.generateGraph(distribution);

        assertTrue(result);
    }
    
}
