package com.graphaware.example.pagerank;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public interface RelationshipChooser {

	/**
	 * @param node The node from which to choose a relationship
	 * @return A {@link Relationship} from the node or <code>null</code> if there aren't any to follow
	 */
	Relationship chooseRelationship(Node node);

}
