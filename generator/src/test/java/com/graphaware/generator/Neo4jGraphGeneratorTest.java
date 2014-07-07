/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.graphaware.generator;

import com.graphaware.generator.config.BasicGeneratorConfiguration;
import com.graphaware.generator.config.GeneratorConfiguration;
import com.graphaware.generator.config.DegreeDistribution;
import com.graphaware.generator.config.InvalidConfigException;
import com.graphaware.generator.config.SimpleDegreeDistribution;
import com.graphaware.generator.node.NodeCreator;
import com.graphaware.generator.node.SocialNetworkNodeCreator;
import com.graphaware.generator.relationship.RelationshipCreator;
import com.graphaware.generator.relationship.RelationshipGenerator;
import com.graphaware.generator.relationship.SimpleGraphRelationshipGenerator;
import com.graphaware.generator.relationship.SocialNetworkRelationshipCreator;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.Arrays;

import static com.graphaware.common.util.IterableUtils.count;
import static org.junit.Assert.assertEquals;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * Smoke test for {@link Neo4jGraphGenerator}.
 */
public class Neo4jGraphGeneratorTest extends DatabaseIntegrationTest {

    @Test
    public void validDistributionShouldGenerateGraph() {
        NodeCreator nodeCreator = SocialNetworkNodeCreator.getInstance();
        RelationshipCreator relationshipCreator = SocialNetworkRelationshipCreator.getInstance();
        SimpleDegreeDistribution distribution = new SimpleDegreeDistribution(Arrays.asList(2, 2, 2, 2));
        SimpleGraphRelationshipGenerator relationshipGenerator = new SimpleGraphRelationshipGenerator(distribution);

        GeneratorConfiguration config = new BasicGeneratorConfiguration(4, relationshipGenerator, nodeCreator, relationshipCreator);

        new Neo4jGraphGenerator(getDatabase()).generateGraph(config);

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(4, count(at(getDatabase()).getAllNodes()));
            assertEquals(4, count(at(getDatabase()).getAllRelationships()));

            for (Node node : at(getDatabase()).getAllNodes()) {
                assertEquals(2, node.getDegree());
            }

            tx.success();
        }
    }

    @Test(expected = InvalidConfigException.class)
    public void invalidDistributionShouldThrowException() {
        NodeCreator nodeCreator = SocialNetworkNodeCreator.getInstance();
        RelationshipCreator relationshipCreator = SocialNetworkRelationshipCreator.getInstance();
        SimpleDegreeDistribution distribution = new SimpleDegreeDistribution(Arrays.asList(3, 2, 2, 2));
        SimpleGraphRelationshipGenerator relationshipGenerator = new SimpleGraphRelationshipGenerator(distribution);

        GeneratorConfiguration config = new BasicGeneratorConfiguration(4, relationshipGenerator, nodeCreator, relationshipCreator);

        new Neo4jGraphGenerator(getDatabase()).generateGraph(config);
    }
}
