package com.graphaware.runtime.metadata;

import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.strategy.InclusionStrategy;
import com.graphaware.common.util.PropertyContainerUtils;
import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.module.RuntimeModule;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.LabelEntry;
import org.neo4j.graphdb.event.TransactionData;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.graphaware.runtime.config.RuntimeConfiguration.GA_METADATA;

/**
 * {@link ModuleMetadataRepository} that stores all {@link ModuleMetadata} on a single Neo4j {@link Node}. Each module's
 * metadata is stored serialized into a byte array as the value of a property, keyed by a key built by
 * {@link RuntimeConfiguration#createPrefix(String)} + {@link com.graphaware.runtime.module.RuntimeModule#getId()}, where
 * the argument passed to {@link RuntimeConfiguration#createPrefix(String)} is {@link #RUNTIME}.
 */
public abstract class SingleNodeMetadataRepository implements ModuleMetadataRepository {

    private static final Logger LOG = Logger.getLogger(SingleNodeMetadataRepository.class);

    public static final String RUNTIME = "RUNTIME";

    private final RuntimeConfiguration configuration;

    /**
     * Create a new repository.
     *
     * @param configuration runtime configuration.
     */
    protected SingleNodeMetadataRepository(RuntimeConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void throwExceptionIfIllegal(TransactionData transactionData) {
        for (LabelEntry entry : transactionData.removedLabels()) {
            if (entry.label().equals(GA_METADATA)) {
                throw new IllegalStateException("Attempted to delete GraphAware Runtime root node!");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends ModuleMetadata, T extends RuntimeModule> M getModuleMetadata(T module) {
        Map<String, Object> internalProperties = getInternalProperties(getOrMetadataNode());
        final String key = moduleKey(module);

        try {
            byte[] serializedMetadata = (byte[]) internalProperties.get(key);

            if (serializedMetadata == null) {
                return null;
            }

            return Serializer.fromByteArray(serializedMetadata);
        } catch (Exception e) {
            removeModuleMetadata(module.getId());
            LOG.error("Could not deserialize metadata for module ID " + module.getId());
            throw new CorruptMetadataException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends ModuleMetadata, T extends RuntimeModule> void persistModuleMetadata(T module, M metadata) {
        getOrMetadataNode().setProperty(moduleKey(module), Serializer.toByteArray(metadata));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAllModuleIds() {
        String prefix = configuration.createPrefix(RUNTIME);
        Set<String> result = new HashSet<>();
        for (String key : getInternalProperties(getOrMetadataNode()).keySet()) {
            result.add(key.replace(prefix, ""));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeModuleMetadata(String moduleId) {
        getOrMetadataNode().removeProperty(moduleKey(moduleId));
    }

    /**
     * Get the node against which to store all metadata, create one if one doesn't exist.
     *
     * @return node on which to store metadata.
     */
    protected abstract Node getOrMetadataNode();

    /**
     * Get properties starting with {@link com.graphaware.runtime.config.RuntimeConfiguration#GA_PREFIX} + {@link #RUNTIME} from a node.
     *
     * @param node to get properties from.
     * @return map of properties (key-value).
     */
    private Map<String, Object> getInternalProperties(Node node) {
        return PropertyContainerUtils.propertiesToMap(node, new InclusionStrategy<String>() {
            @Override
            public boolean include(String s) {
                return s.startsWith(configuration.createPrefix(RUNTIME));
            }
        });
    }

    /**
     * Build a module key to use as a property key on the metadata node.
     *
     * @param module to build a key for.
     * @return module key.
     */
    protected final String moduleKey(RuntimeModule module) {
        return moduleKey(module.getId());
    }

    /**
     * Build a module key to use as a property on the metadata node for storing metadata.
     *
     * @param moduleId to build a key for.
     * @return module key.
     */
    protected final String moduleKey(String moduleId) {
        return configuration.createPrefix(RUNTIME) + moduleId;
    }
}
