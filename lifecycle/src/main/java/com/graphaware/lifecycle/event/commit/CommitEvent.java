package com.graphaware.lifecycle.event.commit;

import com.graphaware.lifecycle.event.LifecycleEvent;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public interface CommitEvent extends LifecycleEvent {

	boolean applicableToNode(Node node);

	boolean applicableToRelationship(Relationship relationship);

	default void validate() {

	}
}
