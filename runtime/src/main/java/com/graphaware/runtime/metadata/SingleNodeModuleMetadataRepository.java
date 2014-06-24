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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.graphaware.runtime.config.RuntimeConfiguration.GA_ROOT;
import static com.graphaware.runtime.manager.BaseModuleManager.RUNTIME;

/**
 *
 */
public abstract class SingleNodeModuleMetadataRepository implements ModuleMetadataRepository {

    private static final Logger LOG = Logger.getLogger(SingleNodeModuleMetadataRepository.class);

    private final RuntimeConfiguration configuration;

    protected SingleNodeModuleMetadataRepository(RuntimeConfiguration configuration) {
        this.configuration = configuration;
    }

    protected abstract Node getOrCreateRoot();

    @Override
    public void check(TransactionData transactionData) {
        for (LabelEntry entry : transactionData.removedLabels()) {
            if (entry.label().equals(GA_ROOT)) {
                throw new IllegalStateException("Attempted to delete GraphAware Runtime root node!");
            }
        }
    }

    @Override
    public Set<String> getAllModuleIds() {
        String prefix = configuration.createPrefix(RUNTIME);
        Set<String> result = new HashSet<>();
        for (String key : getInternalProperties(getOrCreateRoot()).keySet()) {
            result.add(key.replace(prefix, ""));
        }
        return result;
    }

    @Override
    public void removeModuleMetadata(String moduleId) {
        getOrCreateRoot().removeProperty(moduleKey(moduleId));
    }

    @Override
    public String getModuleMetadata(RuntimeModule module) {

        Map<String, Object> internalProperties = getInternalProperties(getOrCreateRoot());

        final String key = moduleKey(module);

        Serializer.register(module.getConfiguration().getClass());

        return (String) internalProperties.get(key);
    }

    @Override
    public void persistModuleMetadata(RuntimeModule module, String metadata) {
        getOrCreateRoot().setProperty(moduleKey(module), metadata);
    }

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
     * Build a module key to use as a property on the root node for storing metadata.
     *
     * @param module to build a key for.
     * @return module key.
     */
    protected final String moduleKey(RuntimeModule module) {
        return moduleKey(module.getId());
    }

    /**
     * Build a module key to use as a property on the root node for storing metadata.
     *
     * @param moduleId to build a key for.
     * @return module key.
     */
    protected final String moduleKey(String moduleId) {
        return configuration.createPrefix(RUNTIME) + moduleId;
    }
}
