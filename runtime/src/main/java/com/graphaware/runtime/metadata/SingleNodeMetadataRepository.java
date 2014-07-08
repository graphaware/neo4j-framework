package com.graphaware.runtime.metadata;

import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.strategy.InclusionStrategy;
import com.graphaware.common.util.PropertyContainerUtils;
import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.module.RuntimeModule;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.LabelEntry;
import org.neo4j.graphdb.event.TransactionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.graphaware.runtime.config.RuntimeConfiguration.GA_METADATA;

/**
 * {@link ModuleMetadataRepository} that stores all {@link ModuleMetadata} on a single Neo4j {@link Node}. Each module's
 * metadata is stored serialized into a byte array as the value of a property, keyed by a key built by
 * {@link RuntimeConfiguration#createPrefix(String)} + {@link com.graphaware.runtime.module.RuntimeModule#getId()}, where
 * the argument passed to {@link RuntimeConfiguration#createPrefix(String)} is provided by the constructor of this class.
 */
public abstract class SingleNodeMetadataRepository implements ModuleMetadataRepository {

    private static final Logger LOG = LoggerFactory.getLogger(SingleNodeMetadataRepository.class);

    private final String propertyPrefix;


    /**
     * Create a new repository.
     *
     * @param configuration  runtime configuration.
     * @param propertyPrefix String with which the property keys of properties written by this repository will be prefixed.
     */
    protected SingleNodeMetadataRepository(RuntimeConfiguration configuration, String propertyPrefix) {
        this.propertyPrefix = configuration.createPrefix(propertyPrefix);
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
    public <M extends ModuleMetadata> M getModuleMetadata(RuntimeModule module) {
        return getModuleMetadata(module.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends ModuleMetadata> M getModuleMetadata(String moduleId) {
        final String key = moduleKey(moduleId);
        Map<String, Object> internalProperties = getInternalProperties(getOrMetadataNode());

        try {
            byte[] serializedMetadata = (byte[]) internalProperties.get(key);

            if (serializedMetadata == null) {
                return null;
            }

            return Serializer.fromByteArray(serializedMetadata);
        } catch (Exception e) {
            removeModuleMetadata(moduleId);
            LOG.error("Could not deserialize metadata for module ID " + moduleId);
            throw new CorruptMetadataException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends ModuleMetadata> void persistModuleMetadata(RuntimeModule module, M metadata) {
        persistModuleMetadata(module.getId(), metadata);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends ModuleMetadata> void persistModuleMetadata(String moduleId, M metadata) {
        getOrMetadataNode().setProperty(moduleKey(moduleId), Serializer.toByteArray(metadata));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAllModuleIds() {
        Set<String> result = new HashSet<>();
        for (String key : getInternalProperties(getOrMetadataNode()).keySet()) {
            result.add(key.replace(propertyPrefix, ""));
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
     * Get properties starting with {@link #propertyPrefix} from a node.
     *
     * @param node to get properties from.
     * @return map of properties (key-value).
     */
    private Map<String, Object> getInternalProperties(Node node) {
        return PropertyContainerUtils.propertiesToMap(node, new InclusionStrategy<String>() {
            @Override
            public boolean include(String s) {
                return s.startsWith(propertyPrefix);
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
        return propertyPrefix + moduleId;
    }
}
