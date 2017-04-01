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

import com.graphaware.common.ping.GoogleAnalyticsStatsCollector;
import com.graphaware.common.ping.StatsCollector;
import com.graphaware.server.foundation.context.FoundationRootContextCreator;
import com.graphaware.server.foundation.context.GraphAwareWebContextCreator;
import com.graphaware.server.foundation.context.RootContextCreator;
import com.graphaware.server.foundation.context.WebContextCreator;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ArrayUtil;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.logging.Log;
import org.neo4j.server.NeoServer;
import org.neo4j.server.configuration.ServerSettings;
import org.neo4j.server.configuration.ThirdPartyJaxRsPackage;
import org.neo4j.server.web.Jetty9WebServer;
import com.graphaware.common.log.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * {@link Filter} that only exists to bootstrap the GraphAware Framework in Neo4j server.
 */
public class GraphAwareBootstrappingFilter implements Filter {

    private static final Log LOG = LoggerFactory.getLogger(GraphAwareBootstrappingFilter.class);

    private static final String GA_PACKAGE = "com.graphaware.server";

    private final NeoServer neoServer;
    private final Jetty9WebServer webServer;

    private AbstractApplicationContext rootContext;

    public GraphAwareBootstrappingFilter(NeoServer neoServer, Jetty9WebServer webServer) {
        this.neoServer = neoServer;
        this.webServer = webServer;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (rootContext != null) {
            return;
        }

        GoogleAnalyticsStatsCollector statsCollector = new GoogleAnalyticsStatsCollector(neoServer.getDatabase().getGraph());

        bootstrapGraphAware(filterConfig, statsCollector);

        statsCollector.frameworkStart("all");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        if (rootContext != null) {
            rootContext.close();
            rootContext = null;
        }
    }

    private void bootstrapGraphAware(FilterConfig filterConfig, StatsCollector statsCollector) {
        HandlerList handlerList = findHandlerList(filterConfig);

        SessionManager sessionManager = findSessionManager(handlerList);

        rootContext = getRootContextCreator().createContext(neoServer, statsCollector);

        addGraphAwareHandlers(handlerList, sessionManager, rootContext, neoServer.getConfig());
    }

    private HandlerList findHandlerList(FilterConfig filterConfig) {
        Server server = ((ContextHandler.Context) filterConfig.getServletContext()).getContextHandler().getServer();

        if (server.getHandler().getClass().equals(RequestLogHandler.class)) {
            return (HandlerList) ((RequestLogHandler) server.getHandler()).getHandler();
        }

        return (HandlerList) server.getHandler();
    }

    private SessionManager findSessionManager(HandlerCollection handlerList) {
        for (Handler h : handlerList.getHandlers()) {
            if (h instanceof ServletContextHandler) {
                return ((ServletContextHandler) h).getSessionHandler().getSessionManager();
            }
        }

        throw new IllegalStateException("Could not find SessionManager");
    }

    protected RootContextCreator getRootContextCreator() {
        return new FoundationRootContextCreator();
    }

    protected void addGraphAwareHandlers(HandlerCollection handlerList, SessionManager sessionManager, ApplicationContext rootContext, Config config) {
        prependHandler(handlerList, createGraphAwareHandler(sessionManager, rootContext));
    }

    private ServletContextHandler createGraphAwareHandler(SessionManager sessionManager, ApplicationContext rootContext) {
        ServletContextHandler handler = createNewHandler(sessionManager, getContextPath(neoServer.getConfig()));

        addSpringToHandler(handler, getGraphAwareContextCreator(), rootContext, neoServer.getConfig());
        addDefaultFilters(handler);

        return handler;
    }

    protected final ServletContextHandler createNewHandler(SessionManager sessionManager, String contextPath) {
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        configureNewHandler(sessionManager, contextPath, handler);
        return handler;
    }

    protected final void configureNewHandler(SessionManager sessionManager, String contextPath, ServletContextHandler handler) {
        handler.setContextPath(contextPath);
        handler.getSessionHandler().setSessionManager(sessionManager);
        handler.setServer(webServer.getJetty());
    }

    protected WebContextCreator getGraphAwareContextCreator() {
        return new GraphAwareWebContextCreator();
    }

    protected final void addSpringToHandler(ServletContextHandler handler, WebContextCreator contextCreator, ApplicationContext rootContext, Config config) {
        addSpringToHandler(handler, contextCreator.createWebContext(rootContext, handler, config));
    }

    protected final void addSpringToHandler(ServletContextHandler handler, WebApplicationContext context) {
        handler.addEventListener(new SpringInitializingServletContextListener(new WebAppInitializer(context, "graphaware" + handler.getContextPath()), handler.getServletContext()));
    }

    protected final void addDefaultFilters(ServletContextHandler context) {
        //dirty dirty stuff
        try {
            Method m = Jetty9WebServer.class.getDeclaredMethod("addFiltersTo", ServletContextHandler.class);
            m.setAccessible(true);
            m.invoke(webServer, context);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void prependHandler(HandlerCollection handlerList, ServletContextHandler handler) {
        handlerList.setHandlers(ArrayUtil.prependToArray(handler, handlerList.getHandlers(), Handler.class));
    }

    private String getContextPath(Config config) {
        for (ThirdPartyJaxRsPackage rsPackage : config.get(ServerSettings.third_party_packages)) {
            if (rsPackage.getPackageName().equals(getPackage())) {
                String path = rsPackage.getMountPoint();
                if (StringUtils.isNotBlank(path)) {
                    LOG.info("Mounting GraphAware Framework at %s", path);
                    return path;
                } else {
                    throw new IllegalArgumentException("Illegal GraphAware mount point: " + path);
                }
            }
        }

        throw new IllegalStateException("No mount point for GraphAware");
    }

    protected String getPackage() {
        return GA_PACKAGE;
    }
}
