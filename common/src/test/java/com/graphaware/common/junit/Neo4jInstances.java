package com.graphaware.common.junit;

import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Neo4jInstances {

    private static final Neo4jInstances INSTANCES = new Neo4jInstances();

    private Neo4jInstances() {
    }

    public static Neo4jInstances getInstances() {
        return INSTANCES;
    }

    private final BlockingQueue<Neo4j> queue = new LinkedBlockingQueue<>(5);

    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    queue.put(create());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private Neo4j create() {
        System.out.println("Creating Neo4j");
        Neo4j build = Neo4jBuilders.newInProcessBuilder().withDisabledServer().build();
        //build.defaultDatabaseService().executeTransactionally("MATCH (n) RETURN count(n)");
        System.out.println("Created Neo4j");
        return build;
    }

    public Neo4j get() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
