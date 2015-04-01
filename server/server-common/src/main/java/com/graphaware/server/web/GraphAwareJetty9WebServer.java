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

import com.graphaware.server.tx.LongRunningTransactionFilter;
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
import org.neo4j.server.database.InjectableProvider;
import org.neo4j.server.rest.transactional.TransactionFacade;
import org.neo4j.server.web.Jetty9WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Collection;
import java.util.EnumSet;

/**
 * GraphAware extension to {@link org.neo4j.server.web.Jetty9WebServer} that mounts the framework APIs under "/graphaware".
 */
public class GraphAwareJetty9WebServer extends Jetty9WebServer {

    private static final Logger LOG = LoggerFactory.getLogger(GraphAwareJetty9WebServer.class);

    private static final String GA_CONTEXT_PATH_SETTING = "com.graphaware.server.uri";
    private static final String GA_CONTEXT_PATH_DEFAULT = "graphaware";

    private final WebAppInitializer initializer;
    private final Config config;
    private LongRunningTransactionFilter txFilter;

    public GraphAwareJetty9WebServer(Logging logging, WebAppInitializer initializer, Config config) {
        super(logging);
        this.initializer = initializer;
        this.config = config;
    }

    @Override
    protected void startJetty() {
        HandlerList handlerList = findHandlerList();

        SessionManager sessionManager = findSessionManager(handlerList);

        addHandlers(handlerList, sessionManager);

        super.startJetty();
    }

    protected void addHandlers(HandlerList handlerList, SessionManager sessionManager) {
        ServletContextHandler graphAwareHandler = createGraphAwareHandler(sessionManager);

        prependHandler(handlerList, graphAwareHandler);
    }

    protected ServletContextHandler createGraphAwareHandler(SessionManager sessionManager) {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(getContextPath(config));
        context.getSessionHandler().setSessionManager(sessionManager);
        context.addLifeCycleListener(new JettyStartingListener(context.getServletContext()));
        context.addFilter(new FilterHolder(txFilter), "/*", EnumSet.allOf(DispatcherType.class));
        return context;
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

    private String getContextPath(Config config) {
        if (config.getParams().containsKey(GA_CONTEXT_PATH_SETTING)) {
            String path = config.getParams().get(GA_CONTEXT_PATH_SETTING);
            if (StringUtils.isNotBlank(path)) {
                LOG.info("Mounting GraphAware Framework under /" + path);
                return "/" + path;
            } else {
                LOG.warn("Invalid URI for GraphAware Framework, will use default...");
            }
        }

        LOG.info("Mounting GraphAware Framework under /" + GA_CONTEXT_PATH_DEFAULT);
        return "/" + GA_CONTEXT_PATH_DEFAULT;
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

        private final ServletContext sc;

        public JettyStartingListener(ServletContext sc) {
            this.sc = sc;
        }

        @Override
        public void lifeCycleStarting(LifeCycle event) {
            try {
                onJettyStartup(sc);
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void onJettyStartup(ServletContext sc) throws ServletException {
        initializer.onStartup(sc);
    }
}
