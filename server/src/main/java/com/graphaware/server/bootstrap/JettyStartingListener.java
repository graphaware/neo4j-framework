package com.graphaware.server.bootstrap;

import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * A Jetty listener that initializes Spring when the server starts.
 */
class JettyStartingListener extends AbstractLifeCycle.AbstractLifeCycleListener {

    private final WebApplicationInitializer initializer;

    private final ServletContext sc;

    public JettyStartingListener(WebApplicationInitializer initializer, ServletContext sc) {
        this.initializer = initializer;
        this.sc = sc;
    }

    @Override
    public void lifeCycleStarting(LifeCycle event) {
        try {
            initializer.onStartup(sc);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }
}