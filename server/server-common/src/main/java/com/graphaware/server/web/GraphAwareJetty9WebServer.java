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
import com.graphaware.server.tx.LongRunningTransactionFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ArrayUtil;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.logging.Logging;
import org.neo4j.server.database.Database;
import org.neo4j.server.database.InjectableProvider;
import org.neo4j.server.rest.transactional.TransactionFacade;
import org.neo4j.server.web.Jetty9WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumSet;

/**
 * GraphAware extension to {@link org.neo4j.server.web.Jetty9WebServer} that mounts the framework APIs under "/graphaware".
 */
public class GraphAwareJetty9WebServer extends Jetty9WebServer {

    private static final Logger LOG = LoggerFactory.getLogger(GraphAwareJetty9WebServer.class);

    private static final String GA_API_CONTEXT_PATH_SETTING = "com.graphaware.server.api.uri";
    private static final String GA_API_CONTEXT_PATH_DEFAULT = "graphaware";
    private static final String GA_API_PACKAGE_SCAN_SETTING = "com.graphaware.server.api.scan";
    private static final String[] GA_API_PACKAGE_SCAN_DEFAULT = new String[]{"com.**.graphaware.**", "org.**.graphaware.**", "net.**.graphaware.**"};

    private final Config config;
    private LongRunningTransactionFilter txFilter;
    private Database database;

    public GraphAwareJetty9WebServer(Logging logging, Database database, Config config) {
        super(logging);
        this.database = database;
        this.config = config;
    }

    @Override
    protected void startJetty() {
        ApplicationContext rootContext = createRootApplicationContext();

        HandlerList handlerList = findHandlerList();

        SessionManager sessionManager = findSessionManager(handlerList);

        addHandlers(handlerList, sessionManager, rootContext);

        addFilters();

        super.startJetty();
    }

    protected ApplicationContext createRootApplicationContext() {
        GenericApplicationContext parent = new GenericApplicationContext();
        parent.getBeanFactory().registerSingleton("database", database.getGraph());

        GraphAwareRuntime runtime = RuntimeRegistry.getRuntime(database.getGraph());
        if (runtime != null) {
            runtime.waitUntilStarted();
            parent.getBeanFactory().registerSingleton("databaseWriter", runtime.getDatabaseWriter());
        }

        parent.refresh();

        return parent;
    }

    protected void addHandlers(HandlerList handlerList, SessionManager sessionManager, ApplicationContext rootContext) {
        ServletContextHandler graphAwareHandler = createGraphAwareHandler(sessionManager, rootContext);

        prependHandler(handlerList, graphAwareHandler);
    }

    protected void addFilters() {

    }

    protected void addFilterToAllHandlers(FilterHolder filterHolder) {
        addFilter(findHandlerList(), filterHolder);
    }

    protected ServletContextHandler createGraphAwareHandler(SessionManager sessionManager, ApplicationContext rootContext) {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(getContextPath(config));
        context.getSessionHandler().setSessionManager(sessionManager);
        context.addLifeCycleListener(new JettyStartingListener(new WebAppInitializer(rootContext, getPackagesToScan(config)), context.getServletContext()));
        context.addFilter(new FilterHolder(txFilter), "/*", EnumSet.allOf(DispatcherType.class));

        addDefaultFilters(context);

        return context;
    }

    protected final void addDefaultFilters(ServletContextHandler context) {
        //dirty dirty stuff
        try {
            Method m = Jetty9WebServer.class.getDeclaredMethod("addFiltersTo", ServletContextHandler.class);
            m.setAccessible(true);
            m.invoke(this, context);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void prependHandler(HandlerList handlerList, ServletContextHandler handler) {
        handlerList.setHandlers(ArrayUtil.prependToArray(handler, handlerList.getHandlers(), Handler.class));
    }

    private HandlerList findHandlerList() {
        //If http logging is turned on, the jetty handler is a RequestLogHandler with a different type hierarchy than
        //the HandlerList returned when http logging is off
        if (getJetty().getHandler().getClass().equals(RequestLogHandler.class)) {
            return  (HandlerList) ((RequestLogHandler) getJetty().getHandler()).getHandler();
        }
        return  (HandlerList) getJetty().getHandler();
    }

    private SessionManager findSessionManager(HandlerList handlerList) {
        for (Handler h :  handlerList.getHandlers()) {
            if (h instanceof ServletContextHandler) {
                return  ((ServletContextHandler) h).getSessionHandler().getSessionManager();
            }
        }

        throw new IllegalStateException("Could not find SessionManager");
    }

    private void addFilter(HandlerList handlerList, FilterHolder filterHolder) {
        for (Handler h :  handlerList.getHandlers()) {
            if (h instanceof ServletContextHandler) {
                ((ServletContextHandler) h).addFilter(filterHolder, "/*", EnumSet.allOf(DispatcherType.class));
            }
        }
    }

    private String getContextPath(Config config) {
        if (config.getParams().containsKey(GA_API_CONTEXT_PATH_SETTING)) {
            String path = config.getParams().get(GA_API_CONTEXT_PATH_SETTING);
            if (StringUtils.isNotBlank(path)) {
                LOG.info("Mounting GraphAware Framework under /" + path);
                return "/" + path;
            } else {
                LOG.warn("Invalid URI for GraphAware Framework, will use default...");
            }
        }

        LOG.info("Mounting GraphAware Framework under /" + GA_API_CONTEXT_PATH_DEFAULT);
        return "/" + GA_API_CONTEXT_PATH_DEFAULT;
    }

    private String[] getPackagesToScan(Config config) {
        if (config.getParams().containsKey(GA_API_PACKAGE_SCAN_SETTING)) {
            String packageExpression = config.getParams().get(GA_API_PACKAGE_SCAN_SETTING);
            if (StringUtils.isNotBlank(packageExpression)) {
                LOG.info("Will try to scan the following packages: " + packageExpression);
                return packageExpression.split(",");
            } else {
                LOG.warn("Invalid expression for packages to scan, will use default...");
            }
        }

        LOG.info("Will try to scan the following packages: " + ArrayUtils.toString(GA_API_PACKAGE_SCAN_DEFAULT));
        return GA_API_PACKAGE_SCAN_DEFAULT;
    }

    @Override
    public void setDefaultInjectables(Collection<InjectableProvider<?>> defaultInjectables) {
        for (InjectableProvider<?> injecteble : defaultInjectables) {
            if (TransactionFacade.class.isAssignableFrom(injecteble.t)) {
                txFilter = new LongRunningTransactionFilter((TransactionFacade) injecteble.getValue(null));
            }
        }

        super.setDefaultInjectables(defaultInjectables);
    }

    public class JettyStartingListener extends AbstractLifeCycle.AbstractLifeCycleListener {

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

    public Config getConfig() {
        return config;
    }
}
