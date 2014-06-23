package com.graphaware.runtime.manager;

import com.graphaware.runtime.module.RuntimeModule;
import org.neo4j.graphdb.event.TransactionData;

/**
 *
 */
public interface ModuleManager<T extends RuntimeModule> {

    void check(TransactionData transactionData);

    void registerModule(T module);

    void initializeModules();

    void shutdownModules();
}
