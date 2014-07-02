package com.graphaware.neo4j.example.pagerank;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class RandomRelationshipChooser implements RelationshipChooser {

	@Override
	public Relationship chooseRelationship(Node node) {
		double max = 0.0;
        Relationship edgeChoice = null;
        // XXX: This will probably perform pretty poorly on popular nodes - is there an O(1) solution available?
        for (Relationship relationship : node.getRelationships()) {
            double rnd = Math.random();
            if (rnd > max) {
                max = rnd;
                edgeChoice = relationship;
            }
        }

 		return edgeChoice;
	}

}
