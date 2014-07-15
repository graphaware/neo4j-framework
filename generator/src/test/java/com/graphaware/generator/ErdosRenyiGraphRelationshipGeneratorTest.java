package com.graphaware.generator;

import com.graphaware.generator.config.ErdosRenyiConfig;
import com.graphaware.generator.relationship.ErdosRenyiGraphRelationshipGenerator;
import junit.framework.TestCase;
import org.junit.Ignore;

@Ignore
public class ErdosRenyiGraphRelationshipGeneratorTest extends TestCase {

    public void testDoGenerateEdges() {
        // EXPERIMENTAL:
        ErdosRenyiConfig config = new ErdosRenyiConfig(1000000, 5000000); // Works fine up to ~ 1000000
        ErdosRenyiGraphRelationshipGenerator er = new ErdosRenyiGraphRelationshipGenerator(config);
        System.out.println(er.generateEdges().size());

//        ErdosRenyiConfig config;
//        config = new ErdosRenyiConfig(20, 190);
//        System.out.println(er.generateEdges(config));
    }
}