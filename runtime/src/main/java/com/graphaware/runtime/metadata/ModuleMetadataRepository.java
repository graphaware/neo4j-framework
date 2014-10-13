package com.graphaware.runtime.metadata;

import com.graphaware.runtime.module.RuntimeModule;
import org.neo4j.graphdb.event.TransactionData;

import java.util.Set;

/**
 * Component that stores {@link ModuleMetadata} so that it survives database restarts.
 */
public interface ModuleMetadataRepository {

    /**
     * Get the metadata of a module that has previously been presisted.
     *
     * @param module to get metadata for.
     * @param <M>    type of the metadata.
     * @return module metadata, null if no such metadata exists. This happens, for example, when a module has never been
     *         registered and/or run before.
     */
    <M extends ModuleMetadata> M getModuleMetadata(RuntimeModule module);

    /**
     * Get the metadata of a module that has previously been presisted.
     *
     * @param moduleId to get metadata for.
     * @param <M>      type of the metadata.
     * @return module metadata, null if no such metadata exists. This happens, for example, when a module has never been
     *         registered and/or run before.
     */
    <M extends ModuleMetadata> M getModuleMetadata(String moduleId);

    /**
     * Persist metadata of a module.
     *
     * @param module   for which to persist metadata.
     * @param metadata to persist.
     * @param <M>      type of the metadata.
     */
    <M extends ModuleMetadata> void persistModuleMetadata(RuntimeModule module, M metadata);

    /**
     * Persist metadata of a module.
     *
     * @param moduleId for which to persist metadata.
     * @param metadata to persist.
     * @param <M>      type of the metadata.
     */
    <M extends ModuleMetadata> void persistModuleMetadata(String moduleId, M metadata);

    /**
     * Get IDs of all modules, for which metadata has been persisted by this repository.
     *
     * @return IDs of all modules.
     */
    Set<String> getAllModuleIds();

    /**
     * Remove persisted metadata for a module.
     *
     * @param moduleId ID of the module for which to remove previously persisted metadata. Nothing happens if no such
     *                 metadata exists.
     */
    void removeModuleMetadata(String moduleId);
}
