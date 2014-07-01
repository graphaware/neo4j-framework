package com.graphaware.generator;

import com.graphaware.generator.relationship.WattsStrogatzGraphRelationshipGenerator;
import junit.framework.TestCase;

public class WattsStrogatzGraphGeneratorTest extends TestCase {

    public void testDoGenerateEdges() throws Exception {
        WattsStrogatzGraphRelationshipGenerator generator = new WattsStrogatzGraphRelationshipGenerator();

        for (int i = 0; i < 10; ++i)
            System.out.println(generator.doGenerateEdges(100, 4, 0.5).toString());
            System.out.println("-------------------------------------");
    }
}