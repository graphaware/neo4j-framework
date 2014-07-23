package com.graphaware.common.util.export;

import com.graphaware.common.util.testing.PageRank;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.Pair;
import org.neo4j.test.TestGraphDatabaseFactory;


import java.util.LinkedList;
import java.util.List;


public class AdjacencyMatrixTest {

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

            AdjacencyMatrix mtx = new AdjacencyMatrix(database);
            PageRank pageRank = new PageRank();


            System.out.println(mtx.getAdjacencyMatrix().toString());
            System.out.println(mtx.getTransitionMatrix().toString());
            transaction.success();

            // ------- calculate the page rank ----------
            System.out.println(pageRank.getPageRank(mtx, 0.85).toString());
            System.out.println("The highest PageRank in the network has: " + mtx.getIndexMap().get(1).getProperty("name").toString());



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