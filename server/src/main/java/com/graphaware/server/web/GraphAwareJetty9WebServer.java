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

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.database.Database;
import org.neo4j.server.database.DatabaseProvider;
import org.neo4j.server.database.GraphDatabaseServiceProvider;
import org.neo4j.server.database.InjectableProvider;
import org.neo4j.server.web.Jetty9WebServer;
import org.springframework.web.SpringServletContainerInitializer;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 */
public class GraphAwareJetty9WebServer extends Jetty9WebServer {

    private GraphDatabaseService database;

    @Override
    protected void startJetty() {
        additionalSetup();

        super.startJetty();
    }

    @Override
    public void setDefaultInjectables(Collection<InjectableProvider<?>> defaultInjectables) {
        for (InjectableProvider<?> provider : defaultInjectables) {
            if (GraphDatabaseService.class.isAssignableFrom(provider.t)) {
                database = (GraphDatabaseService) provider.getValue(null);
            }
        }
        super.setDefaultInjectables(defaultInjectables);
    }

    public class JettyStartingListener extends AbstractLifeCycle.AbstractLifeCycleListener {

        private final ServletContext sc;

        public JettyStartingListener(ServletContext sc) {
            this.sc = sc;
        }

        @Override
        public void lifeCycleStarting(LifeCycle event) {
            new WebAppInitializer(database).onStartup(sc);
        }

    }

    protected void additionalSetup() {


        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/graphaware");
        getJetty().setHandler(context);
        context.addLifeCycleListener(new JettyStartingListener(context.getServletContext()));


//        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
//        contextHandler.setContextPath("/");

//        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
//        appContext.register(AppConfig.class);
//        appContext.refresh();

//        contextHandler.addEventListener(new ContextLoaderListener(appContext));

        // ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", new DispatcherServlet(appContext));
        // dispatcher.setLoadOnStartup(1);
        // dispatcher.addMapping("/");

//        contextHandler.addEventListener(new ContextLoaderListener());
//        contextHandler.setInitParameter("contextConfigLocation", "classpath*:**/testContext.xml");

        // server.setHandler(contextHandler);

        //contextHandler.addServlet(new ServletHolder(new BatchReceiver()), "/receiver/*");
        //contextHandler.addServlet(new ServletHolder(new BatchSender()), "/sender/*");
//        getJetty().getHandlers();
    }
}
