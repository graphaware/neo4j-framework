package com.graphaware.runtime.metadata;

import com.graphaware.runtime.module.RuntimeModule;
import org.neo4j.graphdb.event.TransactionData;

import java.util.Set;

/**
 *
 */
public interface ModuleMetadataRepository {

    void check(TransactionData transactionData);

    <M extends ModuleMetadata> M getModuleMetadata(RuntimeModule<M> module);

    <M extends ModuleMetadata> void persistModuleMetadata(RuntimeModule<M> module, M metadata);

    Set<String> getAllModuleIds();

    void removeModuleMetadata(String moduleId);
}
