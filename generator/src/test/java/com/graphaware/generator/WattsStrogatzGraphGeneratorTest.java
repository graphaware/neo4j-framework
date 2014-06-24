package com.graphaware.generator;

import com.graphaware.generator.relationship.WattsStrogatzGraphGenerator;
import junit.framework.TestCase;

public class WattsStrogatzGraphGeneratorTest extends TestCase {

    public void testDoGenerateEdges() throws Exception {
        WattsStrogatzGraphGenerator generator = new WattsStrogatzGraphGenerator();

        for (int i = 0; i < 10; ++i)
            System.out.println(generator.doGenerateEdges(100, 4, 0.5).toString());
            System.out.println("-------------------------------------");
    }
}