package com.graphaware.server.foundation.bootstrap;

import com.graphaware.common.ping.GoogleAnalyticsStatsCollector;
import com.graphaware.server.foundation.context.*;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ArrayUtil;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.server.NeoServer;
import org.neo4j.server.configuration.ServerSettings;
import org.neo4j.server.configuration.ThirdPartyJaxRsPackage;
import org.neo4j.server.web.Jetty9WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(GraphAwareBootstrappingFilter.class);

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
        bootstrapGraphAware(filterConfig);
        GoogleAnalyticsStatsCollector.getInstance().frameworkStart("all");
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

    private void bootstrapGraphAware(FilterConfig filterConfig) {
        HandlerList handlerList = findHandlerList(filterConfig);

        SessionManager sessionManager = findSessionManager(handlerList);

        rootContext = getRootContextCreator().createContext(neoServer);

        //addSpringToNeoHandlers(handlerList, sessionManager, rootContext);

        addGraphAwareHandlers(handlerList, sessionManager, rootContext);
    }

    private HandlerList findHandlerList(FilterConfig filterConfig) {
        return (HandlerList) ((ContextHandler.Context) filterConfig.getServletContext()).getContextHandler().getServer().getHandler();
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

//    private void addSpringToNeoHandlers(HandlerCollection handlerList, SessionManager sessionManager, ApplicationContext rootContext) {
//        for (Handler neoHandler : handlerList.getHandlers()) {
//            if (!(neoHandler instanceof ServletContextHandler)) {
//                LOG.info(neoHandler + " is not a ServletContextHandler. Not adding Spring.");
//                continue;
//            }
//            LOG.info("Adding Spring to " + neoHandler);
//
//            addSpringToHandler((ServletContextHandler) neoHandler, getNeoContextCreator(), rootContext, neoServer.getConfig());
//        }
//    }

//    protected WebContextCreator getNeoContextCreator() {
//        return new NeoWebContextCreator();
//    }

    protected void addGraphAwareHandlers(HandlerCollection handlerList, SessionManager sessionManager, ApplicationContext rootContext) {
        prependHandler(handlerList, createGraphAwareHandler(sessionManager, rootContext));
    }

    private ServletContextHandler createGraphAwareHandler(SessionManager sessionManager, ApplicationContext rootContext) {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(getContextPath(neoServer.getConfig()));
        context.getSessionHandler().setSessionManager(sessionManager);
        context.setServer(webServer.getJetty());

        addSpringToHandler(context, getGraphAwareContextCreator(), rootContext, neoServer.getConfig());
        addDefaultFilters(context);

        return context;
    }

    protected WebContextCreator getGraphAwareContextCreator() {
        return new GraphAwareWebContextCreator();
    }

    protected final void addSpringToHandler(ServletContextHandler handler, WebContextCreator contextCreator, ApplicationContext rootContext, Config config) {
        WebApplicationContext context = contextCreator.createWebContext(rootContext, handler, config);
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
                    LOG.info("Mounting GraphAware Framework under " + path);
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
