package com.graphaware.runtime.manager;

import com.graphaware.common.serialize.Serializer;
import com.graphaware.runtime.metadata.ModuleMetadata;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.module.RuntimeModule;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.event.TransactionData;

import java.util.*;

/**
 *
 */
public abstract class BaseModuleManager<T extends RuntimeModule> implements ModuleManager<T> {

    private static final Logger LOG = Logger.getLogger(BaseModuleManager.class);

    public static final String FORCE_INITIALIZATION = "FORCE_INIT:";
    public static final String CONFIG = "CONFIG:";

    public static final String RUNTIME = "RUNTIME";

    protected final List<T> modules = new LinkedList<>();
    protected final ModuleMetadataRepository metadataRepository;

    protected BaseModuleManager(ModuleMetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @Override
    public void throwExceptionIfIllegal(TransactionData transactionData) {
        metadataRepository.check(transactionData);
    }


    /**
     * Check that the given module isn't already registered with the runtime.
     *
     * @param module to check.
     * @throws IllegalStateException in case the module is already registered.
     */
    private void checkNotAlreadyRegistered(T module) {
        if (modules.contains(module)) {
            throw new IllegalStateException("Module " + module.getId() + " cannot be registered more than once!");
        }

        for (T existing : modules) {
            if (existing.getId().equals(module.getId())) {
                throw new IllegalStateException("Module " + module.getId() + " cannot be registered more than once!");
            }
        }
    }

    @Override
    public void registerModule(T module) {
        checkNotAlreadyRegistered(module);
        modules.add(module);
    }

    @Override
    public Set<String> initializeModules() {
        final Set<String> moduleIds = new HashSet<>();

        for (final T module : modules) {
            moduleIds.add(module.getId());
            initializeModule2(module);
        }

        return moduleIds;
    }

    protected abstract void initializeModule2(T module);

    @Override
    public void performCleanup(Set<String> usedModules) {
        Set<String> unusedModules = metadataRepository.getAllModuleIds();
        unusedModules.removeAll(usedModules);
        removeUnusedModules(unusedModules);
    }

    @Override
    public void stopModules() {
        for (T module : modules) {
            module.shutdown();
        }
    }

    /**
     * Remove unused modules.
     *
     * @param unusedModules to remove from the root node's properties.
     */
    protected void removeUnusedModules(final Collection<String> unusedModules) {
        for (String moduleId : unusedModules) {
            LOG.info("Removing unused module " + moduleId + ".");
            metadataRepository.removeModuleMetadata(moduleId);
        }
    }
}
