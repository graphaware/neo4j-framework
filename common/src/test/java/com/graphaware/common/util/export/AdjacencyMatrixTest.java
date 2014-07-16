package com.graphaware.common.util.export;

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
        AdjacencyMatrix mtx = new AdjacencyMatrix(database);

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


            System.out.println(mtx.get().toString());
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