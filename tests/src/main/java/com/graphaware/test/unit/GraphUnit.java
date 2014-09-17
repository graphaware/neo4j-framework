/*
 * Copyright (c) 2014 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.test.unit;

import com.graphaware.common.strategy.*;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.graphaware.common.util.PropertyContainerUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.helpers.collection.Iterables.count;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * A set of assertion methods useful for writing tests for Neo4j. Uses the {@link org.junit.Assert} class from JUnit
 * to throw {@link AssertionError}s.
 * <p/>
 * Note: This class is well-tested functionally, but it is not designed for production use, mainly because it hasn't been
 * optimised for performance. The performance will be poor on large graphs - the problem it is solving is computationally
 * quite hard!
 */
public final class GraphUnit {

    private static final Logger LOG = LoggerFactory.getLogger(GraphUnit.class);

    /**
     * Private constructor - this class is a utility and should not be instantiated.
     */
    private GraphUnit() {
    }

    /**
     * Assert that the graph in the given database is exactly the same as the graph that would be created by the given
     * Cypher query. The only thing that can be different in those two graphs are IDs of nodes and relationships.
     * Properties values are converted to {@link String} before comparison, which means 123L (long) is equal to 123 (int),
     * but also "123" (String) is considered equal to 123 (int).
     *
     * @param database        first graph, typically the one that has been created by some code that is being tested by this
     *                        method.
     * @param sameGraphCypher second graph expressed as a Cypher create statement, which communicates the desired state
     *                        of the database (first parameter) iff the code that created it is correct.
     * @throws AssertionError in case the graphs are not the same.
     */
    public static void assertSameGraph(GraphDatabaseService database, String sameGraphCypher) {
        GraphDatabaseService otherDatabase = new TestGraphDatabaseFactory().newImpermanentDatabase();

        new ExecutionEngine(otherDatabase).execute(sameGraphCypher);

        try {
            assertSameGraph(database, otherDatabase,null);
        } finally {
            otherDatabase.shutdown();
        }
    }

    public static void assertSameGraph(GraphDatabaseService database, String sameGraphCypher, InclusionStrategies inclusionStrategies) {
        GraphDatabaseService otherDatabase = new TestGraphDatabaseFactory().newImpermanentDatabase();

        new ExecutionEngine(otherDatabase).execute(sameGraphCypher);

        try {
            assertSameGraph(database, otherDatabase,inclusionStrategies);
        } finally {
            otherDatabase.shutdown();
        }
    }

    /**
     * Assert that the graph that would be created by the given Cypher query is a subgraph of the graph in the given
     * database. This means that every node and every relationship in the (Cypher) subgraph must be present in the
     * (database) graph.
     * <p/>
     * Nodes are considered equal if they have the exact same labels and properties. Relationships
     * are considered equal if they have the same type and properties. IDs of nodes and relationships are not taken
     * into account. Properties values are converted to {@link String} before comparison, which means 123L (long) is
     * equal to 123 (int), but also "123" (String) is considered equal to 123 (int).
     * <p/>
     * This method is useful for testing that some portion of a graph has been created correctly without the need to
     * express the entire graph structure in Cypher.
     *
     * @param database       first graph, typically the one that has been created by some code that is being tested by
     *                       this method.
     * @param subgraphCypher second graph expressed as a Cypher create statement, which communicates the desired state
     *                       of the database (first parameter) iff the code that created it is correct.
     * @throws AssertionError in case the "cypher" graph is not a subgraph of the "database" graph.
     */
    public static void assertSubgraph(GraphDatabaseService database, String subgraphCypher) {
        GraphDatabaseService otherDatabase = new TestGraphDatabaseFactory().newImpermanentDatabase();

        new ExecutionEngine(otherDatabase).execute(subgraphCypher);

        try {
            assertSubgraph(database, otherDatabase);
        } finally {
            otherDatabase.shutdown();
        }
    }

    /**
     * Clear the graph by deleting all nodes and relationships
     * @param database graph, typically the one that has been created by some code that is being tested.
     *
     */
    public static void clearGraph(GraphDatabaseService database) {
        clearGraph(database, InclusionStrategies.all());
    }

    /**
     * Clear the graph by deleting all nodes and relationships specified by inclusionStrategies
     * @param database              graph, typically the one that has been created by some code that is being tested.
     * @param inclusionStrategies   {@link com.graphaware.common.strategy.InclusionStrategies} deciding whether to include nodes/relationships or not.
     *                              Note that property inclusion strategies are ignored when clearing the graph.
     */
    public static void clearGraph(GraphDatabaseService database, InclusionStrategies inclusionStrategies) {


        for (Relationship rel : GlobalGraphOperations.at(database).getAllRelationships()) {
            if (isRelationshipIncluded(rel,inclusionStrategies)) {
                rel.delete();
            }
        }

        for (Node node : GlobalGraphOperations.at(database).getAllNodes()) {
            if (isNodeIncluded(node,inclusionStrategies)) {
                node.delete();
            }
        }
    }


    private static void assertSameGraph(GraphDatabaseService database, GraphDatabaseService otherDatabase,InclusionStrategies inclusionStrategies) {
        try (Transaction tx = database.beginTx()) {
            try (Transaction tx2 = otherDatabase.beginTx()) {
                assertSameNumbersOfElements(database, otherDatabase,inclusionStrategies);
                doAssertSubgraph(database, otherDatabase, inclusionStrategies);
                tx2.failure();
            }
            tx.failure();
        }
    }

    private static void assertSubgraph(GraphDatabaseService database, GraphDatabaseService otherDatabase) {
        try (Transaction tx = database.beginTx()) {
            try (Transaction tx2 = otherDatabase.beginTx()) {
                doAssertSubgraph(database, otherDatabase, null);
                tx2.failure();
            }
            tx.failure();
        }
    }

    private static void assertSameNumbersOfElements(GraphDatabaseService database, GraphDatabaseService otherDatabase) {
        assertEquals("There are different numbers of nodes in the two graphs", count(at(otherDatabase).getAllNodes()), count(at(database).getAllNodes()));
        assertEquals("There are different numbers of relationships in the two graphs", count(at(otherDatabase).getAllRelationships()), count(at(database).getAllRelationships()));
    }

    private static void assertSameNumbersOfElements(GraphDatabaseService database, GraphDatabaseService otherDatabase,InclusionStrategies inclusionStrategies) {
        if(inclusionStrategies==null) {
            assertEquals("There are different numbers of nodes in the two graphs", count(at(otherDatabase).getAllNodes()), count(at(database).getAllNodes()));
            assertEquals("There are different numbers of relationships in the two graphs", count(at(otherDatabase).getAllRelationships()), count(at(database).getAllRelationships()));
        }
        else {

            int nodeCount=0, otherNodeCount=0;
            for(Node node : GlobalGraphOperations.at(database).getAllNodes()) {
                if(isNodeIncluded(node,inclusionStrategies)) {
                    nodeCount++;
                }
            }
            for(Node node : GlobalGraphOperations.at(otherDatabase).getAllNodes()) {
                if(isNodeIncluded(node,inclusionStrategies)) {
                    otherNodeCount++;
                }
            }
            assertEquals("There are different numbers of nodes in the two graphs",otherNodeCount,nodeCount);


            int relCount=0,otherRelCount=0;
            for(Relationship rel : GlobalGraphOperations.at(database).getAllRelationships()) {
                if(isRelationshipIncluded(rel,inclusionStrategies)) {
                    relCount++;
                }
            }
            for(Relationship rel : GlobalGraphOperations.at(otherDatabase).getAllRelationships()) {
                if(isRelationshipIncluded(rel,inclusionStrategies)) {
                    otherRelCount++;
                }
            }
            assertEquals("There are different numbers of relationships in the two graphs", otherRelCount, relCount);

        }


    }

    private static void doAssertSubgraph(GraphDatabaseService database, GraphDatabaseService otherDatabase, InclusionStrategies inclusionStrategies) {
        Map<Long, Long[]> sameNodesMap = buildSameNodesMap(database, otherDatabase, inclusionStrategies);
        Set<Map<Long, Long>> nodeMappings = buildNodeMappingPermutations(sameNodesMap, database, otherDatabase);

        if (nodeMappings.size() == 1) {
            assertRelationshipsMappingExistsForSingleNodeMapping(database, otherDatabase, nodeMappings.iterator().next());
            return;
        }

        for (Map<Long, Long> nodeMapping : nodeMappings) {
            if (relationshipsMappingExists(database, otherDatabase, nodeMapping)) {
                return;
            }
        }

        fail("There is no corresponding relationship mapping for any of the possible node mappings");
    }

    private static Map<Long, Long[]> buildSameNodesMap(GraphDatabaseService database, GraphDatabaseService otherDatabase, InclusionStrategies inclusionStrategies) {
        Map<Long, Long[]> sameNodesMap = new HashMap<>();  //map of nodeID and IDs of nodes that match

        for (Node node : at(otherDatabase).getAllNodes()) {
            if(inclusionStrategies!=null) {
                if(!isNodeIncluded(node,inclusionStrategies)) {
                    continue;
                }
            }
            Iterable<Node> sameNodes = findSameNodes(database, node, inclusionStrategies);    //List of all nodes that match this

            //fail fast
            if (!sameNodes.iterator().hasNext()) {
                fail("There is no corresponding node to " + nodeToString(node));
            }

            Set<Long> sameNodeIds = new HashSet<>();
            for (Node sameNode : sameNodes) {
                sameNodeIds.add(sameNode.getId());
            }
            sameNodesMap.put(node.getId(), sameNodeIds.toArray(new Long[sameNodeIds.size()]));
        }

        return sameNodesMap;
    }

    private static Set<Map<Long, Long>> buildNodeMappingPermutations(Map<Long, Long[]> sameNodesMap, GraphDatabaseService database, GraphDatabaseService otherDatabase) {
        Set<Map<Long, Long>> result = new HashSet<>();
        result.add(new HashMap<Long, Long>());

        for (Map.Entry<Long, Long[]> entry : sameNodesMap.entrySet()) {

            Set<Map<Long, Long>> newResult = new HashSet<>();

            for (Long target : entry.getValue()) {
                for (Map<Long, Long> mapping : result) {
                    if (!mapping.values().contains(target)) {
                        Map<Long, Long> newMapping = new HashMap<>(mapping);
                        newMapping.put(entry.getKey(), target);
                        newResult.add(newMapping);
                    }
                }
            }

            if (newResult.isEmpty()) {
                fail("Could not find a node corresponding to: " + nodeToString(otherDatabase.getNodeById(entry.getKey()))
                        + ". There are most likely more nodes with the same characteristics (labels, properties) in your " +
                        "cypher CREATE statement but fewer in the database.");
            }

            result = newResult;
        }

        return result;
    }

    private static boolean relationshipsMappingExists(GraphDatabaseService database, GraphDatabaseService otherDatabase, Map<Long, Long> mapping) {
        LOG.debug("Attempting a node mapping...");

        Set<Long> usedRelationships = new HashSet<>();
        for (Relationship relationship : at(otherDatabase).getAllRelationships()) {
            if (!relationshipMappingExists(database, relationship, mapping, usedRelationships)) {
                LOG.debug("Failure... No corresponding relationship found to: " + relationshipToString(relationship));
                return false;
            }
        }

        LOG.debug("Success...");
        return true;
    }

    private static void assertRelationshipsMappingExistsForSingleNodeMapping(GraphDatabaseService database, GraphDatabaseService otherDatabase, Map<Long, Long> mapping) {
        Set<Long> usedRelationships = new HashSet<>();
        for (Relationship relationship : at(otherDatabase).getAllRelationships()) {
            if (!relationshipMappingExists(database, relationship, mapping, usedRelationships)) {
                fail("No corresponding relationship found to: " + relationshipToString(relationship));
            }
        }
    }

    private static boolean relationshipMappingExists(GraphDatabaseService database, Relationship relationship, Map<Long, Long> nodeMapping, Set<Long> usedRelationships) {
        for (Relationship candidate : database.getNodeById(nodeMapping.get(relationship.getStartNode().getId())).getRelationships(OUTGOING)) {
            if (nodeMapping.get(relationship.getEndNode().getId()).equals(candidate.getEndNode().getId())) {
                if (areSame(candidate, relationship) && !usedRelationships.contains(candidate.getId())) {
                    usedRelationships.add(candidate.getId());
                    return true;
                }
            }
        }

        return false;
    }

    private static Iterable<Node> findSameNodes(GraphDatabaseService database, Node node, InclusionStrategies inclusionStrategies) {
        Iterator<Label> labels = node.getLabels().iterator();
        if (labels.hasNext()) {
            return findSameNodesByLabel(database, node, labels.next(), inclusionStrategies);
        }

        return findSameNodesWithoutLabel(database, node, inclusionStrategies);
    }

    private static Iterable<Node> findSameNodesByLabel(GraphDatabaseService database, Node node, Label label, InclusionStrategies inclusionStrategies) {
        Set<Node> result = new HashSet<>();

        for (Node candidate : GlobalGraphOperations.at(database).getAllNodesWithLabel(label)) {
            if(isNodeIncluded(candidate,inclusionStrategies)) {
                if (areSame(node, candidate)) {
                    result.add(candidate);
                }
            }
        }

        return result;
    }

    private static Iterable<Node> findSameNodesWithoutLabel(GraphDatabaseService database, Node node, InclusionStrategies inclusionStrategies) {
        Set<Node> result = new HashSet<>();

        for (Node candidate : GlobalGraphOperations.at(database).getAllNodes()) {
            if(isNodeIncluded(candidate,inclusionStrategies)) {
                if (areSame(node, candidate)) {
                    result.add(candidate);
                }
            }
        }

        return result;
    }

    private static boolean areSame(Node node1, Node node2) {
        if (!haveSameLabels(node1, node2)) {
            return false;
        }

        if (!haveSameProperties(node1, node2)) {
            return false;
        }

        return true;
    }

    private static boolean areSame(Relationship relationship1, Relationship relationship2) {
        if (!haveSameType(relationship1, relationship2)) {
            return false;
        }

        if (!haveSameProperties(relationship1, relationship2)) {
            return false;
        }

        return true;
    }

    private static boolean haveSameLabels(Node node1, Node node2) {
        if (count(node1.getLabels()) != count(node2.getLabels())) {
            return false;
        }

        for (Label label : node1.getLabels()) {
            if (!node2.hasLabel(label)) {
                return false;
            }
        }

        return true;
    }

    private static boolean haveSameType(Relationship relationship1, Relationship relationship2) {
        return relationship1.isType(relationship2.getType());
    }

    private static boolean haveSameProperties(PropertyContainer pc1, PropertyContainer pc2) {
        if (count(pc1.getPropertyKeys()) != count(pc2.getPropertyKeys())) {
            return false;
        }

        for (String key : pc1.getPropertyKeys()) {
            if (!pc2.hasProperty(key)) {
                return false;
            }

            if (!valueToString(pc1.getProperty(key)).equals(valueToString(pc2.getProperty(key)))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isNodeIncluded(Node node, InclusionStrategies inclusionStrategies) {
        if(inclusionStrategies!=null) {
            NodeInclusionStrategy nodeInclusionStrategy = inclusionStrategies.getNodeInclusionStrategy() == null ?
                    IncludeAllNodes.getInstance() : inclusionStrategies.getNodeInclusionStrategy();
            return nodeInclusionStrategy.include(node);
        }
        return true;
    }

    private static boolean isRelationshipIncluded(Relationship rel, InclusionStrategies inclusionStrategies) {
        if(inclusionStrategies!=null) {
            RelationshipInclusionStrategy relInclusionStrategy = inclusionStrategies.getRelationshipInclusionStrategy() == null ?
                    IncludeAllRelationships.getInstance() : inclusionStrategies.getRelationshipInclusionStrategy();
            return relInclusionStrategy.include(rel);
        }
        return true;
    }
}
