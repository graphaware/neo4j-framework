package com.graphaware.runtime.manager;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.graphaware.runtime.metadata.*;
import org.apache.log4j.Logger;

import com.graphaware.runtime.module.RuntimeModule;

/**
 * Base-class for {@link ModuleManager} implementations.
 */
public abstract class BaseModuleManager<M extends ModuleMetadata, T extends RuntimeModule<? extends M>> implements ModuleManager<T> {

    private static final Logger LOG = Logger.getLogger(BaseModuleManager.class);

    protected final List<T> modules = new LinkedList<>();
    protected final ModuleMetadataRepository metadataRepository;

    /**
     * Construct a new manager.
     *
     * @param metadataRepository repository for storing module metadata.
     */
    protected BaseModuleManager(ModuleMetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerModule(T module) {
        checkNotAlreadyRegistered(module);
        modules.add(module);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> loadMetadata() {
        final Set<String> moduleIds = new HashSet<>();

        for (final T module : modules) {
            moduleIds.add(module.getId());
            LOG.info("Loading metadata for module " + module.getId());
            loadMetadata(module);
        }

        return moduleIds;
    }

    /**
     * Initialize module. This means doing any work necessary for a module that has been registered for the first time
     * on an existing database, or that has been previously registered with different configuration.
     * <p/>
     * For example, a module that performs some in-graph caching needs to write information into the graph so that when
     * the method returns, the graph is in the same state as it would be if the module has been running all the time
     * since the graph was empty.
     * <p/>
     * Note that for many modules, it might not be necessary to do anything.
     *
     * @param module to initialize.
     */
    private void loadMetadata(T module) {
        M moduleMetadata = null;
        try {
            moduleMetadata = metadataRepository.getModuleMetadata(module);
            if (moduleMetadata == null) {
                LOG.info("Module " + module.getId() + " seems to have been registered for the first time.");
                handleNoMetadata(module);
            }
        } catch (CorruptMetadataException e) {
            LOG.info("Module " + module.getId() + " seems to have corrupted metadata.");
            handleCorruptMetadata(module);
        }

        if (moduleMetadata == null) {
            moduleMetadata = createFreshMetadata(module);
        }

        moduleMetadata = acknowledgeMetadata(module, moduleMetadata);
        persistMetadata(module, moduleMetadata);
    }

    protected abstract void handleCorruptMetadata(T module);

    protected abstract void handleNoMetadata(T module);

    protected abstract M createFreshMetadata(T module);

    protected abstract M acknowledgeMetadata(T module, M metadata);

    /**
     * Capture the fact the a module has been (re-)initialized as a root node's property.
     *
     * @param module that has been initialized.
     */
    private void persistMetadata(final T module, M metadata) {
        metadataRepository.persistModuleMetadata(module, metadata);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanupMetadata(Set<String> usedModules) {
        Set<String> unusedModules = metadataRepository.getAllModuleIds();
        unusedModules.removeAll(usedModules);
        for (String moduleId : unusedModules) {
            LOG.info("Removing unused module " + moduleId + ".");
            metadataRepository.removeModuleMetadata(moduleId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdownModules() {
        for (T module : modules) {
            LOG.info("Shutting down module " + module.getId());
            module.shutdown();
        }
    }
}
