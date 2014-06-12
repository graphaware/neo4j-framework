/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.graphaware.neo4j.configurationmodel;

import java.util.ArrayList;
import static java.util.Collections.shuffle;
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
public class SelfLoopsTest {
    private GraphDatabaseService database;
    
    
    public SelfLoopsTest() {
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
     * Test of generateGraph method, of class SelfLoops.
     */
    @Test
    public void testGenerateGraph() throws Exception {
        System.out.println("generateGraph");
        ArrayList<Integer> distribution = new ArrayList<>();
        
        for (int j = 0; j <= 51; ++j) {
            distribution.add(3);
        }
      
        System.out.println(distribution);
        
        SelfLoops instance = new SelfLoops(database);
        boolean expResult = false;
        boolean result = instance.generateGraph(distribution);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        // fail("The test case is a prototype.");
    }
    
}
