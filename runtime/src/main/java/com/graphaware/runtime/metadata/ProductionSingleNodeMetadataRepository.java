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
 * {@link SingleNodeMetadataRepository} backed by a {@link GraphDatabaseService}.
 */
public class ProductionSingleNodeMetadataRepository extends SingleNodeMetadataRepository {

    private static final Logger LOG = Logger.getLogger(ProductionSingleNodeMetadataRepository.class);

    private final GraphDatabaseService database;

    /**
     * Create a new repository.
     *
     * @param database      to back the repository.
     * @param configuration of the runtime.
     */
    public ProductionSingleNodeMetadataRepository(GraphDatabaseService database, RuntimeConfiguration configuration) {
        super(configuration);
        this.database = database;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node getOrMetadataNode() {
        Iterator<Node> nodes;

        if (database instanceof GraphDatabaseAPI) {
            nodes = at(database).getAllNodesWithLabel(GA_METADATA).iterator();
        } else {
            //this is for Batch Graph Database
            nodes = new MetadataNodeIterator(database);
        }

        if (!nodes.hasNext()) {
            LOG.info("GraphAware Runtime has never been run before on this database. Creating runtime metadata node...");
            return database.createNode(GA_METADATA);
        }

        Node result = nodes.next();

        if (nodes.hasNext()) {
            LOG.fatal("There is more than 1 runtime metadata node! Cannot start GraphAware Runtime.");
            throw new IllegalStateException("There is more than 1 runtime metadata node! Cannot start GraphAware Runtime.");
        }

        return result;
    }

    private static class MetadataNodeIterator extends PrefetchingIterator<Node> {

        private final Iterator<Node> nodes;

        private MetadataNodeIterator(GraphDatabaseService database) {
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
