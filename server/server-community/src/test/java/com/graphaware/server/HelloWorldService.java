package com.graphaware.server;

import com.graphaware.common.util.IterableUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class HelloWorldService implements GreetingService {

    @Autowired
    private GraphDatabaseService database;

    @Override
    public String greet() {
        long count;

        try (Transaction tx = database.beginTx()) {
            count = IterableUtils.count(GlobalGraphOperations.at(database).getAllNodes());
            tx.success();
        }

        return "Hello World! There are " + count + " nodes in the database.";
    }
}
