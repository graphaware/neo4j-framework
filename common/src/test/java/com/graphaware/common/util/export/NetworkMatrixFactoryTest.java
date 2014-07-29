package com.graphaware.common.util.export;

import com.graphaware.common.util.testing.PageRank;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.Pair;
import org.neo4j.test.TestGraphDatabaseFactory;


import java.util.LinkedList;
import java.util.List;


public class NetworkMatrixFactoryTest {

    @Test
    public void test()
    {

        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        List<Pair<String, String>> folks = new LinkedList<Pair<String, String>>() {
            {
                // J --- R
                //  \   /
                //    C
                //    |
                //    A
                //

                add(Pair.of("John", "Carmack"));
                add(Pair.of("John", "Romero"));
                add(Pair.of("Romero", "Carmack"));
                add(Pair.of("Adrian", "Carmack"));
            }
        };

        try (org.neo4j.graphdb.Transaction transaction = database.beginTx()) {

            Label personLabel = DynamicLabel.label("Person");
            DynamicRelationshipType relationshipType = DynamicRelationshipType.withName("BOSS_OF");

            for (Pair<String, String> pairOfPeople : folks) {
                Node person = findOrCreateNode(personLabel, pairOfPeople.first(), database);
                Node colleague = findOrCreateNode(personLabel, pairOfPeople.other(), database);
                person.createRelationshipTo(colleague, relationshipType);
            }

            NetworkMatrixFactory networkMatrixFactory = new NetworkMatrixFactory(database);
            PageRank pageRank = new PageRank();

            NetworkMatrix adjacencyMatrix  = networkMatrixFactory.getAdjacencyMatrix();
            NetworkMatrix transitionMatrix = networkMatrixFactory.getTransitionMatrix();

            System.out.println(adjacencyMatrix.getMatrix().toString());
            System.out.println(transitionMatrix.getMatrix().toString());


            // ------- calculate the page rank ----------
            System.out.println(pageRank.getPageRankVector(transitionMatrix, 0.85));
            System.out.println("The highest PageRank in the network is: " + pageRank.getPageRank(transitionMatrix, 0.85).get(0).getProperty("name"));

            transaction.success();

        }
    }

    private static Node findOrCreateNode(Label label, String name, GraphDatabaseService database) {
        ResourceIterable<Node> existingNodes = database.findNodesByLabelAndProperty(label, "name", name);
        if (existingNodes.iterator().hasNext()) {
            return existingNodes.iterator().next();
        }
        Node newNode = database.createNode(label);
        newNode.setProperty("name", name);
        return newNode;
    }
}