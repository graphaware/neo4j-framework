package com.graphaware.runtime.manager;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.graphaware.runtime.metadata.*;

import com.graphaware.runtime.module.RuntimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base-class for {@link ModuleManager} implementations.
 */
public abstract class BaseModuleManager<M extends ModuleMetadata, T extends RuntimeModule> implements ModuleManager<T> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseModuleManager.class);

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
    public final void registerModule(T module) {
        modules.add(module);
    }

    /**
     * Check that the given module isn't already registered with the manager.
     *
     * @param module to check.
     * @throws IllegalStateException in case the module is already registered.
     */
    public void checkNotAlreadyRegistered(RuntimeModule module) {
        if (modules.contains(module)) {
            LOG.error("Module " + module.getId() + " cannot be registered more than once!");
            throw new IllegalStateException("Module " + module.getId() + " cannot be registered more than once!");
        }

        for (T existing : modules) {
            if (existing.getId().equals(module.getId())) {
                LOG.error("Module " + module.getId() + " cannot be registered more than once!");
                throw new IllegalStateException("Module " + module.getId() + " cannot be registered more than once!");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Set<String> loadMetadata() {
        final Set<String> moduleIds = new HashSet<>();

        for (final T module : modules) {
            moduleIds.add(module.getId());
            LOG.info("Loading metadata for module " + module.getId());
            loadMetadata(module);
        }

        return moduleIds;
    }

    /**
     * Load module metadata from wherever they are stored in between database restarts and do whatever is necessary
     * to do with this metadata before the module can be used.
     *
     * @param module to load metadata for.
     */
    private void loadMetadata(T module) {
        M moduleMetadata = null;
        try {
            moduleMetadata = metadataRepository.getModuleMetadata(module);
            if (moduleMetadata == null) {
                LOG.info("Module " + module.getId() + " seems to have been registered for the first time.");
                handleNoMetadata(module);
            } else {
                LOG.info("Module " + module.getId() + " seems to have been registered before, metadata loaded successfully.");
            }
        } catch (CorruptMetadataException e) {
            LOG.info("Module " + module.getId() + " seems to have corrupted metadata.");
            handleCorruptMetadata(module);
        }

        if (moduleMetadata == null) {
            LOG.info("Creating fresh metadata for module " + module.getId() + ".");
            moduleMetadata = createFreshMetadata(module);
        }

        moduleMetadata = acknowledgeMetadata(module, moduleMetadata);
        persistMetadata(module, moduleMetadata);
    }

    /**
     * Handle the fact that metadata for a module has been corrupted. This could be because somebody has manually messed
     * with the storage of module metadata, or because the implementation of the module metadata has changed and the class
     * expected is different from the class previously serialized.
     * <p/>
     * After this method has returned, it is guaranteed that {@link #createFreshMetadata(com.graphaware.runtime.module.RuntimeModule)}
     * will be called. This means that if the module doesn't heavily rely on its metadata, there is no need to override
     * this method.
     *
     * @param module that had corrupted metadata.
     */
    protected void handleCorruptMetadata(T module) {
        //for sub-classes
    }

    /**
     * Handle the fact that there was no metadata for a module. This could be because it has been registered for the
     * first time, or because somebody as manually deleted the metadata from wherever {@link ModuleMetadataRepository}
     * stores them.
     * <p/>
     * After this method has returned, it is guaranteed that {@link #createFreshMetadata(com.graphaware.runtime.module.RuntimeModule)}
     * will be called. This means that there is no need to override this method unless there is some work that needs to
     * be done in addition to creating new metadata.
     *
     * @param module that had no metadata.
     */
    protected void handleNoMetadata(T module) {
        //for sub-classes
    }

    /**
     * Create new metadata for a module.
     *
     * @param module for which to create metadata.
     * @return fresh metadata.
     */
    protected abstract M createFreshMetadata(T module);

    /**
     * Acknowledge module metadata after it has been created afresh or successfully loaded from a {@link ModuleMetadataRepository}.
     * The implementation can, for example, choose to let the module know about its own metadata. This method also gives
     * the subclasses the opportunity to modify the metadata before it is persisted to the database.
     *
     * @param module   for which to acknowledge metadata.
     * @param metadata to acknowledge.
     * @return the acknowledged metadata (possibly modified).
     */
    protected abstract M acknowledgeMetadata(T module, M metadata);

    /**
     * Persist module metadata.
     *
     * @param module   module.
     * @param metadata metadata.
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
