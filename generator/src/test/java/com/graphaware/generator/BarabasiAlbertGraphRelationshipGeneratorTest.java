package com.graphaware.generator;

import com.graphaware.generator.config.BarabasiAlbertConfig;
import com.graphaware.generator.relationship.BarabasiAlbertGraphRelationshipGenerator;
import junit.framework.TestCase;

public class BarabasiAlbertGraphRelationshipGeneratorTest extends TestCase {

    public void testDoGenerateEdges() throws Exception {
        new BarabasiAlbertGraphRelationshipGenerator(new BarabasiAlbertConfig(100, 3)).generateEdges();
    }
}