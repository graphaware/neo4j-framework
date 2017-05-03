/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.test.unit;

import com.graphaware.common.policy.inclusion.InclusionPolicies;
import com.graphaware.common.policy.inclusion.PropertyInclusionPolicy;
import com.graphaware.common.util.PropertyContainerUtils;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.Iterators;
import org.neo4j.logging.Log;
import org.neo4j.shell.ShellSettings;
import org.neo4j.test.TestGraphDatabaseFactory;
import com.graphaware.common.log.LoggerFactory;

import java.util.*;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static com.graphaware.common.util.PropertyContainerUtils.*;
import static org.junit.Assert.fail;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.helpers.collection.Iterables.count;
import static org.neo4j.kernel.configuration.Settings.*;

/**
 * A set of assertion methods useful for writing tests for Neo4j. Uses the {@link org.junit.Assert} class from JUnit
 * to throw {@link AssertionError}s.
 * <p/>
 * Note: This class is well-tested functionally, but it is not designed for production use, mainly because it hasn't been
 * optimised for performance. The performance will be poor on large graphs - the problem it is solving is computationally
 * quite hard!
 */
public final class GraphUnit {

    private static final Log LOG = LoggerFactory.getLogger(GraphUnit.class);

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
        assertSameGraph(database, sameGraphCypher, InclusionPolicies.all());
    }

    /**
     * Assert that the graph in the given database is exactly the same as the graph that would be created by the given
     * Cypher query. The only thing that can be different in those two graphs are IDs of nodes and relationships
     * and nodes/relationships/properties explicitly excluded from comparisons by provided inclusionPolicies.
     * Properties values are converted to {@link String} before comparison, which means 123L (long) is equal to 123 (int),
     * but also "123" (String) is considered equal to 123 (int).
     *
     * @param database          first graph, typically the one that has been created by some code that is being tested by this
     *                          method.
     * @param sameGraphCypher   second graph expressed as a Cypher create statement, which communicates the desired state
     *                          of the database (first parameter) iff the code that created it is correct.
     * @param inclusionPolicies {@link InclusionPolicies} deciding whether to include nodes/relationships/properties in the comparisons.
     * @throws AssertionError in case the graphs are not the same.
     */
    public static void assertSameGraph(GraphDatabaseService database, String sameGraphCypher, InclusionPolicies inclusionPolicies) {
        if (database == null) {
            throw new IllegalArgumentException("Database must not be null");
        }

        if (sameGraphCypher == null || sameGraphCypher.trim().isEmpty()) {
            assertEmpty(database, inclusionPolicies);
            return;
        }

        GraphDatabaseService otherDatabase = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig("online_backup_enabled", FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .newGraphDatabase();

        registerShutdownHook(otherDatabase);

        otherDatabase.execute(sameGraphCypher);

        try {
            assertSameGraph(database, otherDatabase, inclusionPolicies);
        } finally {
            otherDatabase.shutdown();
        }
    }

    /**
     * Compare that the graph in the given database is exactly the same as the graph that would be created by the given
     * Cypher query. It is transaction-safe method, it can be used in stored procedures, without making the TopLevelTransaction failed.
     * The only thing that can be different in those two graphs are IDs of nodes and relationships.
     * Properties values are converted to {@link String} before comparison, which means 123L (long) is equal to 123 (int),
     * but also "123" (String) is considered equal to 123 (int).
     *
     * @param database        first graph, typically the one that has been created by some code that is being tested by this
     *                        method.
     * @param sameGraphCypher second graph expressed as a Cypher create statement, which communicates the desired state
     *                        of the database (first parameter) iff the code that created it is correct.
     * @return boolean value true in case the graphs are the same, false otherwise
     */
    public boolean areSameGraph(GraphDatabaseService database, String sameGraphCypher) {
        return areSameGraph(database, sameGraphCypher, InclusionPolicies.all());
    }

    /**
     * Compare that the graph in the given database is exactly the same as the graph that would be created by the given
     * Cypher query. It is transaction-safe method, it can be used in stored procedures, without making the TopLevelTransaction failed.
     * The only thing that can be different in those two graphs are IDs of nodes and relationships
     * and nodes/relationships/properties explicitly excluded from comparisons by provided inclusionPolicies.
     * Properties values are converted to {@link String} before comparison, which means 123L (long) is equal to 123 (int),
     * but also "123" (String) is considered equal to 123 (int).
     *
     * @param database        first graph, typically the one that has been created by some code that is being tested by this
     *                        method.
     * @param sameGraphCypher second graph expressed as a Cypher create statement, which communicates the desired state
     *                        of the database (first parameter) iff the code that created it is correct.
     * @param inclusionPolicies {@link InclusionPolicies} deciding whether to include nodes/relationships/properties in the comparisons.
     * @return boolean value true in case the graphs are the same, false otherwise
     */
    public static boolean areSameGraph(GraphDatabaseService database, String sameGraphCypher, InclusionPolicies inclusionPolicies) {
        if (database == null) {
            throw new IllegalArgumentException("Database must not be null");
        }

        if (sameGraphCypher == null || sameGraphCypher.trim().isEmpty()) {
            return isEmpty(database, inclusionPolicies);
        }

        GraphDatabaseService otherDatabase = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig("online_backup_enabled", FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .newGraphDatabase();

        registerShutdownHook(otherDatabase);

        otherDatabase.execute(sameGraphCypher);

        try {
            return areSameGraph(database, otherDatabase, inclusionPolicies);
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
     * into account.  Properties are included for comparison based on the InclusionPolicies.
     * Properties values are converted to {@link String} before comparison, which means 123L (long) is
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
        assertSubgraph(database, subgraphCypher, InclusionPolicies.all());
    }

    /**
     * Assert that the graph that would be created by the given Cypher query is a subgraph of the graph in the given
     * database. This means that every node and every relationship in the (Cypher) subgraph included as specified by
     * the inclusionPolicies must be present in the (database) graph.
     * <p/>
     * Nodes are considered equal if they have the exact same labels and properties. Relationships
     * are considered equal if they have the same type and properties. IDs of nodes and relationships are not taken
     * into account. Properties are included for comparison based on the inclusionPolicies.
     * Properties values are converted to {@link String} before comparison, which means 123L (long) is
     * equal to 123 (int), but also "123" (String) is considered equal to 123 (int).
     * <p/>
     * This method is useful for testing that some portion of a graph has been created correctly without the need to
     * express the entire graph structure in Cypher.
     *
     * @param database          first graph, typically the one that has been created by some code that is being tested by
     *                          this method.
     * @param subgraphCypher    second graph expressed as a Cypher create statement, which communicates the desired state
     *                          of the database (first parameter) iff the code that created it is correct.
     * @param inclusionPolicies {@link InclusionPolicies} deciding whether to include nodes/relationships/properties or not.
     * @throws AssertionError in case the "cypher" graph is not a subgraph of the "database" graph.
     */
    public static void assertSubgraph(GraphDatabaseService database, String subgraphCypher, InclusionPolicies inclusionPolicies) {
        if (database == null) {
            throw new IllegalArgumentException("Database must not be null");
        }

        if (subgraphCypher == null || subgraphCypher.trim().isEmpty()) {
            throw new IllegalArgumentException("Cypher statement must not be null or empty");
        }

        GraphDatabaseService otherDatabase = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig("online_backup_enabled", FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .newGraphDatabase();

        registerShutdownHook(otherDatabase);

        otherDatabase.execute(subgraphCypher);

        try {
            assertSubgraph(database, otherDatabase, inclusionPolicies);
        } finally {
            otherDatabase.shutdown();
        }
    }

    /**
     * Check that the graph that would be created by the given Cypher query is a subgraph of the graph in the given
     * database. This means that every node and every relationship in the (Cypher) subgraph must be present in the
     * (database) graph. It is transaction-safe method, it can be used in stored procedures, without making the
     * TopLevelTransaction failed.
     * <p/>
     * Nodes are considered equal if they have the exact same labels and properties. Relationships
     * are considered equal if they have the same type and properties. IDs of nodes and relationships are not taken
     * into account.  Properties are included for comparison based on the InclusionPolicies.
     * Properties values are converted to {@link String} before comparison, which means 123L (long) is
     * equal to 123 (int), but also "123" (String) is considered equal to 123 (int).
     * <p/>
     * This method is useful for testing that some portion of a graph has been created correctly without the need to
     * express the entire graph structure in Cypher.
     *
     * @param database       first graph, typically the one that has been created by some code that is being tested by
     *                       this method.
     * @param subgraphCypher second graph expressed as a Cypher create statement, which communicates the desired state
     *                       of the database (first parameter) iff the code that created it is correct.
     * @return boolean value true in case the "cypher" graph is a subgraph of the "database" graph, false otherwise.
     */
    public static boolean isSubgraph(GraphDatabaseService database, String subgraphCypher) {
        return isSubgraph(database, subgraphCypher, InclusionPolicies.all());
    }

    /**
     * Check that the graph that would be created by the given Cypher query is a subgraph of the graph in the given
     * database. This means that every node and every relationship in the (Cypher) subgraph included as specified by
     * the inclusionPolicies must be present in the (database) graph. It is transaction-safe method, it can be used
     * in stored procedures, without making the TopLevelTransaction failed.
     * <p/>
     * Nodes are considered equal if they have the exact same labels and properties. Relationships
     * are considered equal if they have the same type and properties. IDs of nodes and relationships are not taken
     * into account.  Properties are included for comparison based on the InclusionPolicies.
     * Properties values are converted to {@link String} before comparison, which means 123L (long) is
     * equal to 123 (int), but also "123" (String) is considered equal to 123 (int).
     * <p/>
     * This method is useful for testing that some portion of a graph has been created correctly without the need to
     * express the entire graph structure in Cypher.
     *
     * @param database       first graph, typically the one that has been created by some code that is being tested by
     *                       this method.
     * @param subgraphCypher second graph expressed as a Cypher create statement, which communicates the desired state
     *                       of the database (first parameter) iff the code that created it is correct.
     * @param inclusionPolicies {@link InclusionPolicies} deciding whether to include nodes/relationships/properties or not.
     * @return boolean value true in case the "cypher" graph is a subgraph of the "database" graph, false otherwise.
     */
    public static boolean isSubgraph(GraphDatabaseService database, String subgraphCypher, InclusionPolicies inclusionPolicies) {
        if (database == null) {
            throw new IllegalArgumentException("Database must not be null");
        }

        if (subgraphCypher == null || subgraphCypher.trim().isEmpty()) {
            throw new IllegalArgumentException("Cypher statement must not be null or empty");
        }

        GraphDatabaseService otherDatabase = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig("online_backup_enabled", FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .newGraphDatabase();

        registerShutdownHook(otherDatabase);

        otherDatabase.execute(subgraphCypher);

        try {
            return isSubgraph(database, otherDatabase, inclusionPolicies);
        } finally {
            otherDatabase.shutdown();
        }
    }

    /**
     * Assert that the database is empty.
     *
     * @param database to run the assertion against.
     */
    public static void assertEmpty(GraphDatabaseService database) {
        assertEmpty(database, InclusionPolicies.all());
    }

    /**
     * Assert that the database is empty.
     *
     * @param database          to run the assertion against.
     * @param inclusionPolicies {@link InclusionPolicies} deciding whether to include nodes/relationships/properties or not in the assertion.
     */
    public static void assertEmpty(GraphDatabaseService database, InclusionPolicies inclusionPolicies) {
        if (database == null) {
            throw new IllegalArgumentException("Database must not be null");
        }

        try (Transaction tx = database.beginTx()) {
            for (Node node : database.getAllNodes()) {
                if (inclusionPolicies.getNodeInclusionPolicy().include(node)) {
                    fail("The database is not empty, there are nodes");
                }
            }

            for (Relationship relationship : database.getAllRelationships()) {
                if (inclusionPolicies.getRelationshipInclusionPolicy().include(relationship)) {
                    fail("The database is not empty, there are relationships");
                }
            }

            tx.success();
        }
    }

    /**
     * Check that the database is empty.
     *
     * @param database          to run the assertion against.
     * @return boolean true if the database is empty, false otherwise.
     */
    public static boolean isEmpty(GraphDatabaseService database) {
        return isEmpty(database, InclusionPolicies.all());
    }

    /**
     * Check that the database is empty.
     *
     * @param database          to run the assertion against.
     * @param inclusionPolicies {@link InclusionPolicies} deciding whether to include nodes/relationships/properties or not in the assertion.
     * @return boolean true if the database is empty, false otherwise.
     */
    public static boolean isEmpty(GraphDatabaseService database, InclusionPolicies inclusionPolicies) {
        if (database == null) {
            throw new IllegalArgumentException("Database must not be null");
        }
        for (Node node : database.getAllNodes()) {
            if (inclusionPolicies.getNodeInclusionPolicy().include(node)) {
                return false;
            }
        }

        for (Relationship relationship : database.getAllRelationships()) {
            if (inclusionPolicies.getRelationshipInclusionPolicy().include(relationship)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Clear the graph by deleting all nodes and relationships.
     *
     * @param database graph, typically the one that has been created by some code that is being tested.
     */
    public static void clearGraph(GraphDatabaseService database) {
        clearGraph(database, InclusionPolicies.all());
    }

    /**
     * Clear the graph by deleting all nodes and relationships specified by inclusionPolicies
     *
     * @param database          graph, typically the one that has been created by some code that is being tested.
     * @param inclusionPolicies {@link InclusionPolicies} deciding whether to include nodes/relationships or not.
     *                          Note that {@link PropertyInclusionPolicy}s are ignored when clearing the graph.
     */
    public static void clearGraph(GraphDatabaseService database, InclusionPolicies inclusionPolicies) {
        if (database == null) {
            throw new IllegalArgumentException("Database must not be null");
        }

        for (Relationship rel : database.getAllRelationships()) {
            if (isRelationshipIncluded(rel, inclusionPolicies)) {
                rel.delete();
            }
        }

        for (Node node : database.getAllNodes()) {
            if (isNodeIncluded(node, inclusionPolicies)) {
                node.delete();
            }
        }
    }

    /**
     * Prints the contents of the graph.
     *
     * @param database to print.
     */
    public static void printGraph(GraphDatabaseService database) {
        printGraph(database, InclusionPolicies.all());
    }

    /**
     * Prints the contents of the graph.
     *
     * @param database          to print.
     * @param inclusionPolicies {@link InclusionPolicies} deciding whether to include nodes/relationships or not.
     *                          Note that {@link PropertyInclusionPolicy}s are ignored when printing the graph.
     */
    public static void printGraph(GraphDatabaseService database, InclusionPolicies inclusionPolicies) {
        if (database == null) {
            throw new IllegalArgumentException("Database must not be null");
        }

        try (Transaction tx = database.beginTx()) {
            System.out.println("Nodes:");
            for (Node node : database.getAllNodes()) {
                if (isNodeIncluded(node, inclusionPolicies)) {
                    System.out.println(PropertyContainerUtils.nodeToString(node));
                }
            }

            System.out.println("Relationships:");
            for (Relationship rel : database.getAllRelationships()) {
                if (isRelationshipIncluded(rel, inclusionPolicies)) {
                    System.out.println(PropertyContainerUtils.relationshipToString(rel));
                }
            }
            tx.success();
        }
    }

    /**
     * Compare two {@link org.neo4j.graphdb.Node} to verify they contain the same labels and properties.
     *
     * @param node1 first node to compare.
     * @param node2 second node to compare.
     * @return boolean are the nodes the same.
     */
    public static boolean areSame(Node node1, Node node2) {
        return areSame(node1, node2, InclusionPolicies.all());
    }

    /**
     * Compare two {@link org.neo4j.graphdb.Relationship} to verify they contain the same labels and properties.
     *
     * @param relationship1 first relationship to compare.
     * @param relationship2 second relationship to compare.
     * @return boolean are the relationships the same.
     */
    public static boolean areSame(Relationship relationship1, Relationship relationship2) {
        return areSame(relationship1, relationship2, InclusionPolicies.all());
    }

    /**
     * Compare two {@link org.neo4j.graphdb.Node} to verify they contain the same labels and properties.
     *
     * @param node1             first node to compare.
     * @param node2             second node to compare.
     * @param inclusionPolicies {@link InclusionPolicies} deciding whether to include nodes/relationships or not.
     *                          Note that {@link PropertyInclusionPolicy}s are ignored when printing the graph.
     * @return boolean are the nodes the same.
     */
    public static boolean areSame(Node node1, Node node2, InclusionPolicies inclusionPolicies) {
        return haveSameLabels(node1, node2) && haveSameProperties(node1, node2, inclusionPolicies);
    }

    /**
     * Compare two {@link org.neo4j.graphdb.Relationship} to verify they contain the same labels and properties.
     *
     * @param relationship1     first relationship to compare.
     * @param relationship2     second relationship to compare.
     * @param inclusionPolicies {@link InclusionPolicies} deciding whether to include nodes/relationships or not.
     *                          Note that {@link PropertyInclusionPolicy}s are ignored when printing the graph.
     * @return boolean are the relationships the same.
     */
    public static boolean areSame(Relationship relationship1, Relationship relationship2, InclusionPolicies inclusionPolicies) {
        return haveSameType(relationship1, relationship2) && haveSameProperties(relationship1, relationship2, inclusionPolicies);
    }

    private static void assertSameGraph(GraphDatabaseService database, GraphDatabaseService otherDatabase, InclusionPolicies InclusionPolicies) {
        try (Transaction tx = database.beginTx()) {
            try (Transaction tx2 = otherDatabase.beginTx()) {
                doAssertSubgraph(database, otherDatabase, InclusionPolicies, "existing database");
                doAssertSubgraph(otherDatabase, database, InclusionPolicies, "Cypher-created database");
                tx2.failure();
            }
            tx.failure();
        }
    }

    private static boolean areSameGraph(GraphDatabaseService database, GraphDatabaseService otherDatabase, InclusionPolicies InclusionPolicies) {
        try (Transaction tx = database.beginTx()) {
            try (Transaction tx2 = otherDatabase.beginTx()) {
                try {
                    doAssertSubgraph(database, otherDatabase, InclusionPolicies, "existing database");
                    doAssertSubgraph(otherDatabase, database, InclusionPolicies, "Cypher-created database");
                } catch (AssertionError error) {
                    return false;
                }finally {
                    tx2.success();
                    tx.success();
                }
            }
        }
        return true;
    }

    private static boolean isSubgraph(GraphDatabaseService database, GraphDatabaseService otherDatabase, InclusionPolicies InclusionPolicies) {
        try (Transaction tx = database.beginTx()) {
            try (Transaction tx2 = otherDatabase.beginTx()) {
                try {
                    doAssertSubgraph(database, otherDatabase, InclusionPolicies, "existing database");
                } catch (AssertionError error) {
                    return false;
                } finally {
                    tx2.success();
                    tx.success();
                }
            }
        }
        return true;
    }


    private static void assertSubgraph(GraphDatabaseService database, GraphDatabaseService otherDatabase, InclusionPolicies InclusionPolicies) {
        try (Transaction tx = database.beginTx()) {
            try (Transaction tx2 = otherDatabase.beginTx()) {
                doAssertSubgraph(database, otherDatabase, InclusionPolicies, "existing database");
                tx2.failure();
            }
            tx.failure();
        }
    }

    private static void doAssertSubgraph(GraphDatabaseService database, GraphDatabaseService otherDatabase, InclusionPolicies inclusionPolicies, String firstDatabaseName) {
        Map<Long, Long[]> sameNodesMap = buildSameNodesMap(database, otherDatabase, inclusionPolicies, firstDatabaseName);
        Set<Map<Long, Long>> nodeMappings = buildNodeMappingPermutations(sameNodesMap, otherDatabase);

        if (nodeMappings.size() == 1) {
            assertRelationshipsMappingExistsForSingleNodeMapping(database, otherDatabase, nodeMappings.iterator().next(), inclusionPolicies, firstDatabaseName);
            return;
        }

        for (Map<Long, Long> nodeMapping : nodeMappings) {
            if (relationshipsMappingExists(database, otherDatabase, nodeMapping, inclusionPolicies)) {
                return;
            }
        }

        fail("There is no corresponding relationship mapping for any of the possible node mappings");
    }

    private static Map<Long, Long[]> buildSameNodesMap(GraphDatabaseService database, GraphDatabaseService otherDatabase, InclusionPolicies inclusionPolicies, String firstDatabaseName) {
        Map<Long, Long[]> sameNodesMap = new HashMap<>();  //map of nodeID and IDs of nodes that match

        for (Node node : otherDatabase.getAllNodes()) {
            if (!isNodeIncluded(node, inclusionPolicies)) {
                continue;
            }
            Iterable<Node> sameNodes = findSameNodes(database, node, inclusionPolicies);    //List of all nodes that match this

            //fail fast
            if (!sameNodes.iterator().hasNext()) {
                fail("There is no corresponding node to " + nodeToString(node) + " in " + firstDatabaseName);
            }

            Set<Long> sameNodeIds = new HashSet<>();
            for (Node sameNode : sameNodes) {
                sameNodeIds.add(sameNode.getId());
            }
            sameNodesMap.put(node.getId(), sameNodeIds.toArray(new Long[sameNodeIds.size()]));
        }

        return sameNodesMap;
    }

    private static Set<Map<Long, Long>> buildNodeMappingPermutations(Map<Long, Long[]> sameNodesMap, GraphDatabaseService otherDatabase) {
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

    private static boolean relationshipsMappingExists(GraphDatabaseService database, GraphDatabaseService otherDatabase, Map<Long, Long> mapping, InclusionPolicies inclusionPolicies) {
        LOG.debug("Attempting a node mapping...");

        Set<Long> usedRelationships = new HashSet<>();
        for (Relationship relationship : otherDatabase.getAllRelationships()) {
            if (!relationshipMappingExists(database, relationship, mapping, usedRelationships, inclusionPolicies)) {
                LOG.debug("Failure... No corresponding relationship found to: " + relationshipToString(relationship));
                return false;
            }
        }

        LOG.debug("Success...");
        return true;
    }

    private static void assertRelationshipsMappingExistsForSingleNodeMapping(GraphDatabaseService database, GraphDatabaseService otherDatabase, Map<Long, Long> mapping, InclusionPolicies inclusionPolicies, String firstDatabaseName) {
        Set<Long> usedRelationships = new HashSet<>();
        for (Relationship relationship : otherDatabase.getAllRelationships()) {
            if (!relationshipMappingExists(database, relationship, mapping, usedRelationships, inclusionPolicies)) {
                fail("No corresponding relationship found to " + relationshipToString(relationship) + " in " + firstDatabaseName);
            }
        }
    }

    private static boolean relationshipMappingExists(GraphDatabaseService database, Relationship relationship, Map<Long, Long> nodeMapping, Set<Long> usedRelationships, InclusionPolicies inclusionPolicies) {
        if (!isRelationshipIncluded(relationship, inclusionPolicies)) {
            return true;
        }

        for (Relationship candidate : database.getNodeById(nodeMapping.get(relationship.getStartNode().getId())).getRelationships(OUTGOING)) {
            if (nodeMapping.get(relationship.getEndNode().getId()).equals(candidate.getEndNode().getId())) {
                if (areSame(candidate, relationship, inclusionPolicies) && !usedRelationships.contains(candidate.getId())) {
                    usedRelationships.add(candidate.getId());
                    return true;
                }
            }
        }

        return false;
    }

    private static Iterable<Node> findSameNodes(GraphDatabaseService database, Node node, InclusionPolicies inclusionPolicies) {
        Iterator<Label> labels = node.getLabels().iterator();
        if (labels.hasNext()) {
            return findSameNodesByLabel(database, node, labels.next(), inclusionPolicies);
        }

        return findSameNodesWithoutLabel(database, node, inclusionPolicies);
    }

    private static Iterable<Node> findSameNodesByLabel(GraphDatabaseService database, Node node, Label label, InclusionPolicies inclusionPolicies) {
        Set<Node> result = new HashSet<>();

        for (Node candidate : Iterators.asResourceIterable(database.findNodes(label))) {
            if (isNodeIncluded(candidate, inclusionPolicies)) {
                if (areSame(node, candidate, inclusionPolicies)) {
                    result.add(candidate);
                }
            }
        }

        return result;
    }

    private static Iterable<Node> findSameNodesWithoutLabel(GraphDatabaseService database, Node node, InclusionPolicies inclusionPolicies) {
        Set<Node> result = new HashSet<>();

        for (Node candidate : database.getAllNodes()) {
            if (isNodeIncluded(candidate, inclusionPolicies)) {
                if (areSame(node, candidate, inclusionPolicies)) {
                    result.add(candidate);
                }
            }
        }

        return result;
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

    private static boolean haveSameProperties(PropertyContainer pc1, PropertyContainer pc2, InclusionPolicies inclusionPolicies) {
        int pc1KeyCount = 0, pc2KeyCount = 0;
        for (String key : pc1.getPropertyKeys()) {
            if (isPropertyIncluded(pc1, key, inclusionPolicies)) {
                pc1KeyCount++;
                if (!pc2.hasProperty(key)) {
                    return false;
                }
                if (!valueToString(pc1.getProperty(key)).equals(valueToString(pc2.getProperty(key)))) {
                    return false;
                }
            }
        }
        for (String key : pc2.getPropertyKeys()) {
            if (isPropertyIncluded(pc2, key, inclusionPolicies)) {
                pc2KeyCount++;
            }
        }
        return pc1KeyCount == pc2KeyCount;
    }

    private static boolean isNodeIncluded(Node node, InclusionPolicies inclusionPolicies) {
        return inclusionPolicies.getNodeInclusionPolicy().include(node);
    }

    private static boolean isRelationshipIncluded(Relationship rel, InclusionPolicies inclusionPolicies) {
        return inclusionPolicies.getRelationshipInclusionPolicy().include(rel);
    }

    private static boolean isPropertyIncluded(PropertyContainer propertyContainer, String propertyKey, InclusionPolicies inclusionPolicies) {
        if (propertyContainer instanceof Node) {
            return inclusionPolicies.getNodePropertyInclusionPolicy().include(propertyKey, (Node) propertyContainer);
        }

        if (propertyContainer instanceof Relationship) {
            return inclusionPolicies.getRelationshipPropertyInclusionPolicy().include(propertyKey, (Relationship) propertyContainer);
        }

        throw new IllegalStateException("Property container is not a Node or Relationship!");
    }
}
