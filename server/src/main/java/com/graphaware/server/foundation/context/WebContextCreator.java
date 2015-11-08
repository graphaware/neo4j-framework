package com.graphaware.server.foundation.context;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.neo4j.kernel.configuration.Config;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public interface WebContextCreator {

    WebApplicationContext createWebContext(ApplicationContext rootContext, ServletContextHandler handler, Config config);
}
