package com.graphaware.runtime.metadata;

import com.graphaware.runtime.config.RuntimeConfiguration;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.PrefetchingIterator;
import org.neo4j.kernel.GraphDatabaseAPI;

import java.util.Iterator;

import static com.graphaware.runtime.config.RuntimeConfiguration.GA_METADATA;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 *
 */
public class ProductionSingleNodeModuleMetadataRepository extends SingleNodeModuleMetadataRepository {

    private static final Logger LOG = Logger.getLogger(ProductionSingleNodeModuleMetadataRepository.class);

    private final GraphDatabaseService database;

    public ProductionSingleNodeModuleMetadataRepository(GraphDatabaseService database, RuntimeConfiguration configuration) {
        super(configuration);
        this.database = database;
    }

    @Override
    protected Node getOrCreateRoot() {
        return getOrCreateRoot(database);
    }

    public static Node getOrCreateRoot(GraphDatabaseService database) {
        Iterator<Node> roots;

        if (database instanceof GraphDatabaseAPI) {
            roots = at(database).getAllNodesWithLabel(GA_METADATA).iterator();
        } else {
            //this is for Batch Graph Database
            roots = new RootNodeIterator(database);
        }

        if (!roots.hasNext()) {
            LOG.info("GraphAware Runtime has never been run before on this database. Creating runtime root node...");
            return database.createNode(GA_METADATA);
        }

        Node result = roots.next();

        if (roots.hasNext()) {
            LOG.fatal("There is more than 1 runtime root node! Cannot start GraphAware Runtime.");
            throw new IllegalStateException("There is more than 1 runtime root node! Cannot start GraphAware Runtime.");
        }

        return result;
    }

    private static class RootNodeIterator extends PrefetchingIterator<Node> {

        private final Iterator<Node> nodes;

        private RootNodeIterator(GraphDatabaseService database) {
            //this is deliberately using the deprecated API
            //noinspection deprecation
            nodes = database.getAllNodes().iterator();
        }

        @Override
        protected Node fetchNextOrNull() {
            while (nodes.hasNext()) {
                Node next = nodes.next();
                if (next.hasLabel(GA_METADATA)) {
                    return next;
                }
            }

            return null;
        }
    }
}
