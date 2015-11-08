package com.graphaware.server.foundation.context;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.neo4j.kernel.configuration.Config;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public abstract class BaseWebContextCreator implements WebContextCreator {

    @Override
    public WebApplicationContext createWebContext(ApplicationContext rootContext, ServletContextHandler handler, Config config) {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setParent(rootContext);
        context.setServletContext(handler.getServletContext());

        registerConfigClasses(context, config);

        context.refresh();

        return context;
    }

    protected abstract void registerConfigClasses(AnnotationConfigWebApplicationContext context, Config config);
}
