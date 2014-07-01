package com.graphaware.generator;

import com.graphaware.generator.config.NumberOfNodes;
import com.graphaware.generator.relationship.CompleteGraphRelationshipGenerator;
import junit.framework.TestCase;

public class CompleteGraphRelationshipGeneratorTest extends TestCase {
    public void testDoGenerateEdges() {
        CompleteGraphRelationshipGenerator cg = new CompleteGraphRelationshipGenerator();

        System.out.println(cg.generateEdges(new NumberOfNodes(3)).toString());
        System.out.println(cg.generateEdges(new NumberOfNodes(4)).toString());
        System.out.println(cg.generateEdges(new NumberOfNodes(5)).toString());


    }
}