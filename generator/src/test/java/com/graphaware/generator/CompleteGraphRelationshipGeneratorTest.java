package com.graphaware.generator;

import com.graphaware.generator.config.NumberOfNodes;
import com.graphaware.generator.relationship.CompleteGraphRelationshipGenerator;
import junit.framework.TestCase;

public class CompleteGraphRelationshipGeneratorTest extends TestCase {
    public void testDoGenerateEdges() {
        CompleteGraphRelationshipGenerator cg1 = new CompleteGraphRelationshipGenerator(new NumberOfNodes(3));
        System.out.println(cg1.generateEdges().toString());

        CompleteGraphRelationshipGenerator cg2 = new CompleteGraphRelationshipGenerator(new NumberOfNodes(4));
        System.out.println(cg2.generateEdges().toString());

        CompleteGraphRelationshipGenerator cg3 = new CompleteGraphRelationshipGenerator(new NumberOfNodes(5));
        System.out.println(cg3.generateEdges().toString());
    }
}