/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.graphaware.generator;

import com.graphaware.generator.config.BasicGeneratorConfiguration;
import com.graphaware.generator.config.GeneratorConfiguration;
import com.graphaware.generator.distribution.DegreeDistribution;
import com.graphaware.generator.distribution.InvalidDistributionException;
import com.graphaware.generator.distribution.SimpleDegreeDistribution;
import com.graphaware.generator.node.NodeCreator;
import com.graphaware.generator.node.SocialNetworkNodeCreator;
import com.graphaware.generator.relationship.RelationshipCreator;
import com.graphaware.generator.relationship.RelationshipGenerator;
import com.graphaware.generator.relationship.SimpleGraphRelationshipGenerator;
import com.graphaware.generator.relationship.SocialNetworkRelationshipCreator;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Arrays;

import static com.graphaware.common.util.IterableUtils.count;
import static org.junit.Assert.assertEquals;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * Smoke test for {@link Neo4jGraphGenerator}.
 */
public class Neo4jGraphGeneratorTest extends DatabaseIntegrationTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected GraphDatabaseService createDatabase() {
        return new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    @Test
    public void validDistributionShouldGenerateGraph() {
        NodeCreator nodeCreator = SocialNetworkNodeCreator.getInstance();
        RelationshipCreator relationshipCreator = SocialNetworkRelationshipCreator.getInstance();
        RelationshipGenerator relationshipGenerator = new SimpleGraphRelationshipGenerator();
        DegreeDistribution distribution = new SimpleDegreeDistribution(Arrays.asList(2, 2, 2, 2));

        GeneratorConfiguration config = new BasicGeneratorConfiguration(nodeCreator, relationshipCreator, relationshipGenerator, distribution);

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

    @Test(expected = InvalidDistributionException.class)
    public void invalidDistributionShouldThrowException() {
        NodeCreator nodeCreator = SocialNetworkNodeCreator.getInstance();
        RelationshipCreator relationshipCreator = SocialNetworkRelationshipCreator.getInstance();
        RelationshipGenerator relationshipGenerator = new SimpleGraphRelationshipGenerator();
        DegreeDistribution distribution = new SimpleDegreeDistribution(Arrays.asList(3, 2, 2, 2));

        GeneratorConfiguration config = new BasicGeneratorConfiguration(nodeCreator, relationshipCreator, relationshipGenerator, distribution);

        new Neo4jGraphGenerator(getDatabase()).generateGraph(config);
    }
}
