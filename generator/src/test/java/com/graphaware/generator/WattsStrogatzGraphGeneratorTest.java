package com.graphaware.generator;

import com.graphaware.generator.relationship.WattsStrogatzGraphGenerator;
import junit.framework.TestCase;

public class WattsStrogatzGraphGeneratorTest extends TestCase {

    public void testDoGenerateEdges() throws Exception {
        WattsStrogatzGraphGenerator generator = new WattsStrogatzGraphGenerator();

        for (int i = 0; i < 10000; ++i)
            System.out.println(generator.doGenerateEdges(6, 4, 1.0).toString());
    }
}