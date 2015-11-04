package com.graphaware.server.bootstrap;

import com.graphaware.common.ping.GoogleAnalyticsStatsCollector;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.RuntimeRegistry;
import com.graphaware.server.web.WebAppInitializer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ArrayUtil;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.server.configuration.ServerSettings;
import org.neo4j.server.configuration.ThirdPartyJaxRsPackage;
import org.neo4j.server.database.Database;
import org.neo4j.server.web.Jetty9WebServer;
import org.neo4j.server.web.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import javax.servlet.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * {@link Filter} that only exists to bootstrap the Framework.
 */
public class GraphAwareBootstrappingFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(GraphAwareBootstrappingFilter.class);

    private static final String GA_PACKAGE = "com.graphaware.server";
    private static final String GA_API_PACKAGE_SCAN_SETTING = "com.graphaware.server.api.scan";
    private static final String[] GA_API_PACKAGE_SCAN_DEFAULT = new String[]{"com.**.graphaware.**", "org.**.graphaware.**", "net.**.graphaware.**"};

    private final Database database;
    private final Config config;
    private final Jetty9WebServer webServer;

    private AbstractApplicationContext rootContext;

    public GraphAwareBootstrappingFilter(Database database, Config config, Jetty9WebServer webServer) {
        this.database = database;
        this.config = config;
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

    protected void bootstrapGraphAware(FilterConfig filterConfig) {
        HandlerList handlerList = findHandlerList(filterConfig);

        SessionManager sessionManager = findSessionManager(handlerList);

        rootContext = createRootApplicationContext();

        addHandlers(handlerList, sessionManager, rootContext);
    }

    private HandlerList findHandlerList(FilterConfig filterConfig) {
        return (HandlerList) ((ContextHandler.Context) filterConfig.getServletContext()).getContextHandler().getServer().getHandler();
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
        for (Handler h : handlerList.getHandlers()) {
            if (h instanceof ServletContextHandler) {
                return ((ServletContextHandler) h).getSessionHandler().getSessionManager();
            }
        }

        throw new IllegalStateException("Could not find SessionManager");
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

    public Config getConfig() {
        return config;
    }
}
