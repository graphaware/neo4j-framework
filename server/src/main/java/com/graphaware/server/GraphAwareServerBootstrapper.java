/*
 * Copyright (c) 2015 GraphAware
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

package com.graphaware.server;

import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.RuntimeRegistry;
import com.graphaware.server.tx.LongRunningTransactionFilter;
import com.graphaware.server.web.GraphAwareJetty9WebServer;
import com.graphaware.server.web.WebAppInitializer;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ArrayUtil;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.logging.LogProvider;
import org.neo4j.server.AbstractNeoServer;
import org.neo4j.server.NeoServer;
import org.neo4j.server.database.Database;
import org.neo4j.server.plugins.Injectable;
import org.neo4j.server.plugins.SPIPluginLifecycle;
import org.neo4j.server.rest.transactional.TransactionFacade;
import org.neo4j.server.web.Jetty9WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.*;
import javax.ws.rs.Path;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

//todo analytics
//todo config of endpoint
@Path("/")
public class GraphAwareServerBootstrapper implements SPIPluginLifecycle {

    private static final Logger LOG = LoggerFactory.getLogger(GraphAwareServerBootstrapper.class);

    private static final String GA_API_CONTEXT_PATH_SETTING = "com.graphaware.server.api.uri";
    private static final String GA_API_CONTEXT_PATH_DEFAULT = "graphaware";
    private static final String GA_API_PACKAGE_SCAN_SETTING = "com.graphaware.server.api.scan";
    private static final String[] GA_API_PACKAGE_SCAN_DEFAULT = new String[]{"com.**.graphaware.**", "org.**.graphaware.**", "net.**.graphaware.**"};

    private Config config;
    private Jetty9WebServer webServer;
    private Database database;
    private AbstractApplicationContext rootContext;

    @Override
    public Collection<Injectable<?>> start(NeoServer neoServer) {
        webServer = (Jetty9WebServer) ((AbstractNeoServer) neoServer).getWebServer();
        webServer.addFilter(new Filter() {
            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
                if (rootContext != null) {
                    return;
                }
                HandlerList handlerList = (HandlerList) ((ContextHandler.Context) filterConfig.getServletContext()).getContextHandler().getServer().getHandler();
                startJetty(handlerList);
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
        },"/*");
        config = neoServer.getConfig();
        database = neoServer.getDatabase();

        TransactionFacade transactionFacade = null;
        try {
            Field tfField = AbstractNeoServer.class.getDeclaredField("transactionFacade");
            tfField.setAccessible(true);
            transactionFacade = (TransactionFacade) tfField.get(neoServer);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        webServer.addFilter(new LongRunningTransactionFilter(transactionFacade), "/*");

        return Collections.EMPTY_SET;
    }

    @Override
    public void stop() {
        if (rootContext != null) {
            rootContext.close();
            rootContext = null;
        }
    }

    @Override
    public Collection<Injectable<?>> start(GraphDatabaseService graphDatabaseService, Configuration configuration) {
        return Collections.EMPTY_SET;
    }

    protected void startJetty(HandlerList handlerList) {
        rootContext = createRootApplicationContext();

        SessionManager sessionManager = findSessionManager(handlerList);

        addHandlers(handlerList, sessionManager, rootContext);

        addFilters();
    }

    protected AbstractApplicationContext createRootApplicationContext() {
        GenericApplicationContext parent = new GenericApplicationContext();
        parent.registerShutdownHook();
        parent.getBeanFactory().registerSingleton("database", database.getGraph());

        GraphAwareRuntime runtime = RuntimeRegistry.getRuntime(database.getGraph());
        if (runtime != null) {
            runtime.waitUntilStarted();
            parent.getBeanFactory().registerSingleton("databaseWriter", runtime.getDatabaseWriter());
        }

        parent.refresh();

        return parent;
    }

    protected void addHandlers(HandlerCollection handlerList, SessionManager sessionManager, ApplicationContext rootContext) {
        ServletContextHandler graphAwareHandler = createGraphAwareHandler(sessionManager, rootContext);

        prependHandler(handlerList, graphAwareHandler);
    }

    protected void addFilters() {

    }

//    protected void addFilterToAllHandlers(FilterHolder filterHolder) {
//        addFilter(findHandlerList(), filterHolder);
//    }

    protected ServletContextHandler createGraphAwareHandler(SessionManager sessionManager, ApplicationContext rootContext) {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(getContextPath(config));
        context.getSessionHandler().setSessionManager(sessionManager);
        context.addLifeCycleListener(new JettyStartingListener(new WebAppInitializer(rootContext, getPackagesToScan(config)), context.getServletContext()));

        addDefaultFilters(context);

        return context;
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



    private SessionManager findSessionManager(HandlerCollection handlerList) {
        for (Handler h :  handlerList.getHandlers()) {
            if (h instanceof ServletContextHandler) {
                return  ((ServletContextHandler) h).getSessionHandler().getSessionManager();
            }
        }

        throw new IllegalStateException("Could not find SessionManager");
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
