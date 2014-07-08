package com.graphaware.generator;

import com.graphaware.generator.config.WattsStrogatzConfig;
import com.graphaware.generator.relationship.WattsStrogatzRelationshipGenerator;
import junit.framework.TestCase;

public class WattsStrogatzGraphGeneratorTest extends TestCase {

    public void testDoGenerateEdges() throws Exception {
        WattsStrogatzRelationshipGenerator generator = new WattsStrogatzRelationshipGenerator(new WattsStrogatzConfig(100, 4, 0.5));

        for (int i = 0; i < 10; ++i)
            System.out.println(generator.generateEdges().toString());
            System.out.println("-------------------------------------");
    }
}