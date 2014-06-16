/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.graphaware.neo4j.configurationmodel;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 *
 * @author Vojtech Havlicek (Graphaware)
 */
public class SimpleTest {
    
    private GraphDatabaseService database;
    public SimpleTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        this.database = new TestGraphDatabaseFactory().newImpermanentDatabase();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of generateGraph method, of class Simple.
     */
    @Test
    public void testGenerateGraph() throws Exception {
        System.out.println("generateGraph");
        
        // Prepare the test distribution
        ArrayList<Integer> distribution = new ArrayList<>();
        distribution.add(2);
        distribution.add(2);
        distribution.add(2);
        distribution.add(2);
        
        // Test the method
        Simple instance = new Simple(database);
        boolean result = instance.generateGraph(distribution);
       
    }
    
}
