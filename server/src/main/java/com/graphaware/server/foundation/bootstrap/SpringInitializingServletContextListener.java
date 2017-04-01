/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

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