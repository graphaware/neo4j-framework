package com.graphaware.relcount.perf;

import com.graphaware.framework.GraphAwareFramework;
import com.graphaware.relcount.module.RelationshipCountModule;
import com.graphaware.tx.executor.NullItem;
import com.graphaware.tx.executor.batch.NoInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.util.Random;

import static org.neo4j.graphdb.DynamicRelationshipType.withName;

@Ignore
public class SpaceComparison {

    protected static final Random RANDOM = new Random(System.currentTimeMillis());

    @Test
    public void createTwoDatabases() {
        GraphDatabaseService one = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/space/one");
        populateDatabase(one);
        one.shutdown();

        GraphDatabaseService two = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/space/two");
        GraphAwareFramework framework = new GraphAwareFramework(two);
        framework.registerModule(new RelationshipCountModule());
        framework.start();
        populateDatabase(two);
        two.shutdown();
    }

    private void populateDatabase(GraphDatabaseService database) {

        new NoInputBatchTransactionExecutor(database, 1000, 1000, new UnitOfWork<NullItem>() {
            @Override
            public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                database.createNode();
            }
        }).execute();

        new NoInputBatchTransactionExecutor(database, 1000, 1000000, new UnitOfWork<NullItem>() {
            @Override
            public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                final Node node1 = database.getNodeById(RANDOM.nextInt(1000) + 1);
                final Node node2 = database.getNodeById(RANDOM.nextInt(1000) + 1);

                Relationship rel = node1.createRelationshipTo(node2, withName("TEST" + ((1000 * (batchNumber - 1) + stepNumber) % 2)));
                rel.setProperty("rating", RANDOM.nextInt(5) + 1);
                rel.setProperty("timestamp", RANDOM.nextLong());
            }
        }).execute();
    }
}
