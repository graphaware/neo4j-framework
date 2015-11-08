package com.graphaware.server.foundation.context;

import org.neo4j.server.NeoServer;
import org.springframework.context.support.AbstractApplicationContext;

public interface RootContextCreator {

    AbstractApplicationContext createContext(NeoServer neoServer);
}
