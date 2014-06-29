package com.graphaware.runtime.metadata;

import com.graphaware.runtime.module.RuntimeModule;
import org.neo4j.graphdb.event.TransactionData;

import java.util.Set;

/**
 *
 */
public interface ModuleMetadataRepository {

    void check(TransactionData transactionData);

    <M extends ModuleMetadata, T extends RuntimeModule<? extends M>> M getModuleMetadata(T module);

    <M extends ModuleMetadata, T extends RuntimeModule<? extends M>> void persistModuleMetadata(T module, M metadata);

    Set<String> getAllModuleIds();

    void removeModuleMetadata(String moduleId);
}
