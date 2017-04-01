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

import com.graphaware.server.tx.LongRunningTransactionFilter;
import org.apache.commons.configuration.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;
import org.neo4j.server.AbstractNeoServer;
import org.neo4j.server.NeoServer;
import org.neo4j.server.plugins.Injectable;
import org.neo4j.server.plugins.SPIPluginLifecycle;
import org.neo4j.server.rest.transactional.TransactionFacade;
import org.neo4j.server.web.Jetty9WebServer;
import org.neo4j.server.web.WebServer;
import com.graphaware.common.log.LoggerFactory;

import javax.ws.rs.Path;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

/**
 * A component that bootstraps the GraphAware Framework.
 * <p/>
 * It is a bit hacky and we know this - if anybody has a better idea how to do this, please issue a PR!
 * Once this works again: http://www.markhneedham.com/blog/2013/07/08/neo4j-unmanaged-extension-creating-gzipped-streamed-responses-with-jetty/
 * we may be able to bootstrap again without needing to register an "unmanaged extension".
 */
@SuppressWarnings("deprecation")
@Path("/")
public class GraphAwareServerBootstrapper implements SPIPluginLifecycle {

    private static final Log LOG = LoggerFactory.getLogger(GraphAwareServerBootstrapper.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Injectable<?>> start(NeoServer neoServer) {
        LOG.info("started");

        addFilters(neoServer);

        return Collections.emptyList();
    }

    private void addFilters(NeoServer neoServer) {
        addFilters(neoServer, getWebServer(neoServer));
    }

    protected void addFilters(NeoServer neoServer, Jetty9WebServer webServer) {
        webServer.addFilter(createBootstrappingFilter(neoServer, webServer), "/*");
        webServer.addFilter(createTransactionFilter(neoServer), "/*");
    }

    private Jetty9WebServer getWebServer(NeoServer neoServer) {
        if (neoServer instanceof AbstractNeoServer) {
            WebServer webServer = ((AbstractNeoServer) neoServer).getWebServer();
            if (webServer instanceof Jetty9WebServer) {
                return (Jetty9WebServer) webServer;
            }
            throw new IllegalStateException("Server is not Jetty9WebServer");
        }
        throw new IllegalStateException("Server is not an AbstractNeoServer");
    }

    protected GraphAwareBootstrappingFilter createBootstrappingFilter(NeoServer neoServer, Jetty9WebServer webServer) {
        return new GraphAwareBootstrappingFilter(neoServer, webServer);
    }

    private LongRunningTransactionFilter createTransactionFilter(NeoServer neoServer) {
        return new LongRunningTransactionFilter(getTransactionFacade(neoServer));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        LOG.info("stopped");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Injectable<?>> start(GraphDatabaseService graphDatabaseService, Configuration configuration) {
        throw new UnsupportedOperationException("Attempted to start the GraphAware Framework in an unsupported way");
    }

    private TransactionFacade getTransactionFacade(NeoServer neoServer) {
        try {
            Field tfField = AbstractNeoServer.class.getDeclaredField("transactionFacade");
            tfField.setAccessible(true);
            return (TransactionFacade) tfField.get(neoServer);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
