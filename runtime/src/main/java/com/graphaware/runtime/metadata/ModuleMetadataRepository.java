package com.graphaware.runtime.metadata;

import com.graphaware.runtime.module.RuntimeModule;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.TransactionData;

import java.util.Collection;
import java.util.Map;

/**
 *
 */
public interface ModuleMetadataRepository {

    void check(TransactionData transactionData);

    Collection<String> getAllModuleIds();

    String getModuleMetadata(RuntimeModule module);

    void persistModuleMetadata(RuntimeModule module, String metadata);

    void removeModuleMetadata(String moduleId);
}
