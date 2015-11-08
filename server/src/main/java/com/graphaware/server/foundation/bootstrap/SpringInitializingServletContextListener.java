package com.graphaware.server.foundation.bootstrap;

import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

/**
 * A ServletContextListener listener that initializes Spring when the server starts.
 */
public class SpringInitializingServletContextListener implements ServletContextListener {

    private final WebApplicationInitializer initializer;

    private final ServletContext sc;

    public SpringInitializingServletContextListener(WebApplicationInitializer initializer, ServletContext sc) {
        this.initializer = initializer;
        this.sc = sc;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            initializer.onStartup(sc);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}