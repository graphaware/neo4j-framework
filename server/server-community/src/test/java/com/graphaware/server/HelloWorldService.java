package com.graphaware.server;

import com.graphaware.common.util.IterableUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HelloWorldService implements GreetingService {

    @Autowired
    private GraphDatabaseService database;

    @Override
    public String greet() {
        try (Transaction tx = database.beginTx()) {
            return "Hello World! There are " + IterableUtils.count(GlobalGraphOperations.at(database).getAllNodes()) + " nodes in the database.";
        }
    }
}
