package com.graphaware.generator;

import com.graphaware.generator.config.BarabasiAlbertConfig;
import com.graphaware.generator.relationship.BarabasiAlbertGraphRelationshipGenerator;
import junit.framework.TestCase;

public class BarabasiAlbertGraphRelationshipGeneratorTest extends TestCase {

    public void testDoGenerateEdges() throws Exception {
        BarabasiAlbertGraphRelationshipGenerator bg = new BarabasiAlbertGraphRelationshipGenerator();
        bg.generateEdges(new BarabasiAlbertConfig(100, 3));
    }
}