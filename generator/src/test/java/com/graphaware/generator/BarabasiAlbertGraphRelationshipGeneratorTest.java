package com.graphaware.generator;

import com.graphaware.generator.config.BarabasiAlbertConfig;
import com.graphaware.generator.relationship.BarabasiAlbertGraphRelationshipGenerator;
import junit.framework.TestCase;

public class BarabasiAlbertGraphRelationshipGeneratorTest extends TestCase {

    public void testDoGenerateEdges() throws Exception {
        System.out.println(new BarabasiAlbertGraphRelationshipGenerator(new BarabasiAlbertConfig(1000000, 2)).generateEdges().size());

    }
}