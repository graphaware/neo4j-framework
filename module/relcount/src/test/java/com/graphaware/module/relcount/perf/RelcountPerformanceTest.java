package com.graphaware.module.relcount.perf;

import com.graphaware.runtime.performance.PerformanceTest;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.util.Random;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;


public abstract class RelcountPerformanceTest implements PerformanceTest {

    protected static final Random RANDOM = new Random(System.currentTimeMillis());

    protected static final String FW = "fw";
    protected static final String PROPS = "props";

    protected Node randomNode(GraphDatabaseService database, int noNodes) {
        return database.getNodeById(RANDOM.nextInt(noNodes) + 1);
    }

    protected Direction randomDirection() {
        return RANDOM.nextBoolean() ? INCOMING : OUTGOING;
    }

    protected DynamicRelationshipType randomType() {
        return withName("TEST" + RANDOM.nextInt(2));
    }
}
