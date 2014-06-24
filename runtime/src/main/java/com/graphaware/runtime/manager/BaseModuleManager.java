package com.graphaware.runtime.manager;

import com.graphaware.common.serialize.Serializer;
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


            String moduleMetadata = metadataRepository.getModuleMetadata(module);

            if (moduleMetadata == null) {
                LOG.info("Module " + module.getId() + " seems to have been registered for the first time, will initialize...");
                initializeModule(module);
                continue;
            }

            if (moduleMetadata.startsWith(CONFIG)) {
                if (!moduleMetadata.equals(Serializer.toString(module.getConfiguration(), CONFIG))) {
                    LOG.info("Module " + module.getId() + " seems to have changed configuration since last run, will re-initialize...");
                    reinitializeModule(module);
                } else {
                    LOG.info("Module " + module.getId() + " has not changed configuration since last run, already initialized.");
                }
                continue;
            }

            if (moduleMetadata.startsWith(FORCE_INITIALIZATION)) {
                LOG.info("Module " + module.getId() + " has been marked for re-initialization on "
                        + new Date(Long.valueOf(moduleMetadata.replace(FORCE_INITIALIZATION, ""))).toString() + ". Will re-initialize...");
                reinitializeModule(module);
                continue;

            }

            LOG.fatal("Corrupted module info: " + moduleMetadata + " is not a valid value!");
            throw new IllegalStateException("Corrupted module info: " + moduleMetadata + " is not a valid value");
        }

        return moduleIds;
    }

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
     * Initialize a module and capture that fact on the as a root node's property.
     *
     * @param module to initialize.
     */
    private void initializeModule(final T module) {
        doInitialize(module);
        recordInitialization(module);
    }

    /**
     * Initialize module.
     *
     * @param module to initialize.
     */
    protected abstract void doInitialize(T module);

    /**
     * Re-initialize a module and capture that fact on the as a root node's property.
     *
     * @param module to initialize.
     */
    private void reinitializeModule(final T module) {
        doReinitialize(module);
        recordInitialization(module);
    }

    /**
     * Re-initialize a module.
     *
     * @param module to initialize.
     */
    protected abstract void doReinitialize(T module);

    /**
     * Capture the fact the a module has been (re-)initialized as a root node's property.
     *
     * @param module that has been initialized.
     */
    private void recordInitialization(final T module) {
        metadataRepository.persistModuleMetadata(module, Serializer.toString(module.getConfiguration(), CONFIG));
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

    /**
     * Force a module to be (re-)initialized next time the database (and runtime) are started.
     *
     * @param module to be (re-)initialized next time.
     */
    protected void forceInitialization(final T module) {
        if (!metadataRepository.getModuleMetadata(module).startsWith(FORCE_INITIALIZATION)) {
            metadataRepository.persistModuleMetadata(module, FORCE_INITIALIZATION + System.currentTimeMillis());
        }
    }
}
