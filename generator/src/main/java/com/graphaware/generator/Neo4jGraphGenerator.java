package com.graphaware.generator;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.generator.config.GeneratorConfiguration;
import com.graphaware.generator.relationship.RelationshipGenerator;
import com.graphaware.tx.executor.NullItem;
import com.graphaware.tx.executor.batch.BatchTransactionExecutor;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.NoInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.util.List;

/**
 * {@link GraphGenerator} for Neo4j.
 */
public class Neo4jGraphGenerator implements GraphGenerator {

    private final GraphDatabaseService database;

    public Neo4jGraphGenerator(GraphDatabaseService database) {
        this.database = database;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateGraph(GeneratorConfiguration configuration) {
        generateNodes(configuration);
        generateRelationships(configuration);
    }

    private void generateNodes(final GeneratorConfiguration config) {
        int numberOfNodes = config.getNumberOfNodes();

        BatchTransactionExecutor executor = new NoInputBatchTransactionExecutor(database, config.getBatchSize(), numberOfNodes, new UnitOfWork<NullItem>() {
            @Override
            public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                config.getNodeCreator().createNode(database);
            }
        });

        executor.execute();
    }

    private void generateRelationships(final GeneratorConfiguration config) {
        RelationshipGenerator<?> relationshipGenerator = config.getRelationshipGenerator();
        List<SameTypePair<Integer>> relationships = relationshipGenerator.generateEdges();

        BatchTransactionExecutor executor = new IterableInputBatchTransactionExecutor<>(database, config.getBatchSize(), relationships, new UnitOfWork<SameTypePair<Integer>>() {
            @Override
            public void execute(GraphDatabaseService database, SameTypePair<Integer> input, int batchNumber, int stepNumber) {
                Node first = database.getNodeById(input.first());
                Node second = database.getNodeById(input.second());
                config.getRelationshipCreator().createRelationship(first, second);
            }
        });

        executor.execute();
    }
}
