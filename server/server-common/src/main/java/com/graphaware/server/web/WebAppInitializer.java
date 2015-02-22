/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.server.web;

import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.RuntimeRegistry;
import org.neo4j.server.database.Database;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

/**
 * Servlet 3.0+ web application initializer, no need for XML.
 */
public class WebAppInitializer extends AbstractDispatcherServletInitializer {

    private Database database;

    public WebAppInitializer(Database database) {
        this.database = database;
    }

    @Override
    protected WebApplicationContext createServletApplicationContext() {
        GenericApplicationContext parent = new GenericApplicationContext();
        parent.getBeanFactory().registerSingleton("database", database.getGraph());

        GraphAwareRuntime runtime = RuntimeRegistry.getRuntime(database.getGraph());
        if (runtime != null) {
            runtime.waitUntilStarted();
            parent.getBeanFactory().registerSingleton("databaseWriter", runtime.getDatabaseWriter());
        }

        parent.refresh();

        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.setParent(parent);
        appContext.scan("com.**.graphaware.**", "org.**.graphaware.**", "net.**.graphaware.**");

        return appContext;
    }

    @Override
    protected String getServletName() {
        return "graphaware";
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected WebApplicationContext createRootApplicationContext() {
        return null;
    }
}
