package com.graphaware.module.relcount;

import com.graphaware.common.change.Change;
import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.module.relcount.cache.NodeBasedDegreeCache;
import com.graphaware.module.relcount.count.CachedRelationshipCounter;
import com.graphaware.module.relcount.count.FallbackRelationshipCounter;
import com.graphaware.module.relcount.count.NaiveRelationshipCounter;
import com.graphaware.module.relcount.count.RelationshipCounter;
import com.graphaware.runtime.GraphAwareRuntimeModule;
import com.graphaware.runtime.config.BaseRuntimeConfigured;
import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfigured;
import com.graphaware.runtime.config.RuntimeModuleConfiguration;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import com.graphaware.tx.event.batch.propertycontainer.inserter.BatchInserterNode;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.event.improved.propertycontainer.filtered.FilteredNode;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.TransactionCallback;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Collection;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * {@link com.graphaware.runtime.GraphAwareRuntimeModule} providing caching capabilities for full relationship counting.
 * "Full" means it cares about {@link org.neo4j.graphdb.RelationshipType}s, {@link org.neo4j.graphdb.Direction}s,
 * and properties.
 * <p/>
 * Once registered with {@link com.graphaware.runtime.ProductionGraphAwareRuntime}, relationship
 * counts will be cached on nodes properties. {@link com.graphaware.module.relcount.count.CachedRelationshipCounter} or {@link com.graphaware.module.relcount.count.FallbackRelationshipCounter} can then be used to
 * count relationships by querying these cached counts.
 */
public class RelationshipCountRuntimeModule extends BaseRuntimeConfigured implements GraphAwareRuntimeModule, RuntimeConfigured {

    /**
     * Default ID of this module used to identify metadata written by this module.
     */
    public static final String FULL_RELCOUNT_DEFAULT_ID = "FRC";

    private final String id;
    private final RelationshipCountConfiguration relationshipCountConfiguration;
    private final NodeBasedDegreeCache relationshipCountCache;

    /**
     * Create a module with default ID and configuration. Use this constructor when you wish to register a single
     * instance of the module with {@link com.graphaware.runtime.ProductionGraphAwareRuntime} and you are happy with
     * the default configuration (see {@link RelationshipCountConfigurationImpl#defaultConfiguration()}).
     */
    public RelationshipCountRuntimeModule() {
        this(FULL_RELCOUNT_DEFAULT_ID, RelationshipCountConfigurationImpl.defaultConfiguration());
    }

    /**
     * Create a module with default ID and custom configuration. Use this constructor when you wish to register a single
     * instance of the module with {@link com.graphaware.runtime.ProductionGraphAwareRuntime} and you want to provide
     * custom {@link RelationshipCountConfiguration}. This could be the case, for instance, when you would like to exclude
     * certain {@link org.neo4j.graphdb.Relationship}s from being counted at all ({@link com.graphaware.common.strategy.RelationshipInclusionStrategy}),
     * certain properties from being considered at all ({@link com.graphaware.common.strategy.RelationshipPropertyInclusionStrategy}),
     * weigh each relationship differently ({@link com.graphaware.module.relcount.count.WeighingStrategy},
     * or use a custom threshold for compaction.
     */
    public RelationshipCountRuntimeModule(RelationshipCountConfiguration relationshipCountConfiguration) {
        this(FULL_RELCOUNT_DEFAULT_ID, relationshipCountConfiguration);
    }

    /**
     * Create a module with a custom ID and configuration. Use this constructor when you wish to register a multiple
     * instances of the module with {@link com.graphaware.runtime.ProductionGraphAwareRuntime} and you want to provide
     * custom {@link RelationshipCountConfiguration} for each one of them. This could be the case, for instance, when you
     * would like to keep two different kinds of relationships, weighted and unweighted.
     */
    public RelationshipCountRuntimeModule(String id, RelationshipCountConfiguration relationshipCountConfiguration) {
        this.id = id;
        this.relationshipCountConfiguration = relationshipCountConfiguration;
        this.relationshipCountCache = new NodeBasedDegreeCache(id, relationshipCountConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        //do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RuntimeModuleConfiguration getConfiguration() {
        return relationshipCountConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(GraphDatabaseService database) {
        buildCachedCounts(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reinitialize(GraphDatabaseService database) {
        clearCachedCounts(database);
        initialize(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(TransactionSimulatingBatchInserter batchInserter) {
        buildCachedCounts(batchInserter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reinitialize(TransactionSimulatingBatchInserter batchInserter) {
        clearCachedCounts(batchInserter);
        initialize(batchInserter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeCommit(ImprovedTransactionData transactionData) {
        relationshipCountCache.startCaching();

        try {
            handleCreatedRelationships(transactionData);
            handleDeletedRelationships(transactionData);
            handleChangedRelationships(transactionData);
        } finally {
            relationshipCountCache.endCaching();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configurationChanged(RuntimeConfiguration configuration) {
        super.configurationChanged(configuration);
        relationshipCountCache.configurationChanged(configuration);
    }

    private void handleCreatedRelationships(ImprovedTransactionData data) {
        Collection<Relationship> allCreatedRelationships = data.getAllCreatedRelationships();

        for (Relationship createdRelationship : allCreatedRelationships) {
            relationshipCountCache.handleCreatedRelationship(createdRelationship, createdRelationship.getStartNode(), INCOMING);
            relationshipCountCache.handleCreatedRelationship(createdRelationship, createdRelationship.getEndNode(), OUTGOING);
        }
    }

    private void handleDeletedRelationships(ImprovedTransactionData data) {
        Collection<Relationship> allDeletedRelationships = data.getAllDeletedRelationships();

        for (Relationship deletedRelationship : allDeletedRelationships) {
            Node startNode = deletedRelationship.getStartNode();
            if (!data.hasBeenDeleted(startNode)) {
                relationshipCountCache.handleDeletedRelationship(deletedRelationship, startNode, INCOMING);
            }

            Node endNode = deletedRelationship.getEndNode();
            if (!data.hasBeenDeleted(endNode)) {
                relationshipCountCache.handleDeletedRelationship(deletedRelationship, endNode, Direction.OUTGOING);
            }
        }
    }

    private void handleChangedRelationships(ImprovedTransactionData data) {
        Collection<Change<Relationship>> allChangedRelationships = data.getAllChangedRelationships();

        for (Change<Relationship> changedRelationship : allChangedRelationships) {
            Relationship current = changedRelationship.getCurrent();
            Relationship previous = changedRelationship.getPrevious();

            relationshipCountCache.handleDeletedRelationship(previous, previous.getStartNode(), Direction.INCOMING);
            relationshipCountCache.handleDeletedRelationship(previous, previous.getEndNode(), Direction.OUTGOING);
            relationshipCountCache.handleCreatedRelationship(current, current.getStartNode(), Direction.INCOMING);
            relationshipCountCache.handleCreatedRelationship(current, current.getEndNode(), Direction.OUTGOING);
        }
    }

    /**
     * Clear all cached counts. NOTE: This is a potentially very expensive operation as it traverses the
     * entire graph! Use with care.
     *
     * @param database to perform the operation on.
     */
    private void clearCachedCounts(GraphDatabaseService database) {
        new IterableInputBatchTransactionExecutor<>(
                database,
                500,
                new SimpleTransactionExecutor(database).executeInTransaction(new TransactionCallback<Iterable<Node>>() {
                    @Override
                    public Iterable<Node> doInTransaction(GraphDatabaseService database) {
                        return at(database).getAllNodes();
                    }
                }),
                new UnitOfWork<Node>() {
                    @Override
                    public void execute(GraphDatabaseService database, Node node, int batchNumber, int stepNumber) {
                        for (String key : node.getPropertyKeys()) {
                            if (key.startsWith(getConfig().createPrefix(id))) {
                                node.removeProperty(key);
                            }
                        }
                    }
                }
        ).execute();
    }

    /**
     * Clear all cached counts. NOTE: This is a potentially very expensive operation as it traverses the
     * entire graph! Use with care.
     *
     * @param batchInserter to perform the operation on.
     */
    private void clearCachedCounts(TransactionSimulatingBatchInserter batchInserter) {
        for (long nodeId : batchInserter.getAllNodes()) {
            for (String key : batchInserter.getNodeProperties(nodeId).keySet()) {
                if (key.startsWith(getConfig().createPrefix(id))) {
                    batchInserter.removeNodeProperty(nodeId, key);
                }
            }
        }
    }

    /**
     * Clear and rebuild all cached counts. NOTE: This is a potentially very expensive operation as it traverses the
     * entire graph! Use with care.
     *
     * @param database to perform the operation on.
     */
    private void buildCachedCounts(GraphDatabaseService database) {
        new IterableInputBatchTransactionExecutor<>(
                database,
                100,
                new SimpleTransactionExecutor(database).executeInTransaction(new TransactionCallback<Iterable<Node>>() {
                    @Override
                    public Iterable<Node> doInTransaction(GraphDatabaseService database) {
                        return at(database).getAllNodes();
                    }
                }),
                new UnitOfWork<Node>() {
                    @Override
                    public void execute(GraphDatabaseService database, Node node, int batchNumber, int stepNumber) {
                        Node filteredNode = new FilteredNode(node, getConfiguration().getInclusionStrategies());

                        buildCachedCounts(filteredNode);

                    }
                }).execute();
    }

    /**
     * Clear and rebuild all cached counts. NOTE: This is a potentially very expensive operation as it traverses the
     * entire graph! Use with care.
     *
     * @param batchInserter to perform the operation on.
     */
    private void buildCachedCounts(TransactionSimulatingBatchInserter batchInserter) {
        for (long nodeId : batchInserter.getAllNodes()) {
            Node filteredNode = new FilteredNode(new BatchInserterNode(nodeId, batchInserter), getConfiguration().getInclusionStrategies());

            buildCachedCounts(filteredNode);
        }
    }

    /**
     * Build cached counts for a node.
     *
     * @param filteredNode filtered node.
     */
    private void buildCachedCounts(Node filteredNode) {
        relationshipCountCache.startCaching();

        for (Relationship relationship : filteredNode.getRelationships()) {
            relationshipCountCache.handleCreatedRelationship(relationship, filteredNode, Direction.OUTGOING);

            if (relationship.getStartNode().getId() == relationship.getEndNode().getId()) {
                relationshipCountCache.handleCreatedRelationship(relationship, filteredNode, Direction.INCOMING);
            }
        }

        relationshipCountCache.endCaching();
    }
}
