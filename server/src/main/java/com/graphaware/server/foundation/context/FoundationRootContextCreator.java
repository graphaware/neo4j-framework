package com.graphaware.server.foundation.context;

import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.RuntimeRegistry;
import org.neo4j.server.NeoServer;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class FoundationRootContextCreator implements RootContextCreator {

    @Override
    public AbstractApplicationContext createContext(NeoServer neoServer) {
        GenericApplicationContext parent = new GenericApplicationContext();
        parent.registerShutdownHook();
        parent.getBeanFactory().registerSingleton("database", neoServer.getDatabase().getGraph());

        GraphAwareRuntime runtime = RuntimeRegistry.getRuntime(neoServer.getDatabase().getGraph());
        if (runtime != null) {
            runtime.waitUntilStarted();
            parent.getBeanFactory().registerSingleton("databaseWriter", runtime.getDatabaseWriter());
        }

        parent.refresh();

        return parent;
    }
}
