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

import com.graphaware.server.web.GraphAwareJetty9WebServer;
import org.apache.commons.configuration.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.LogProvider;
import org.neo4j.server.AbstractNeoServer;
import org.neo4j.server.NeoServer;
import org.neo4j.server.plugins.Injectable;
import org.neo4j.server.plugins.SPIPluginLifecycle;
import org.neo4j.server.web.Jetty9WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

@Path("/")
public class GraphAwareServerBootstrapper implements SPIPluginLifecycle {

    private static final Logger LOG = LoggerFactory.getLogger(GraphAwareServerBootstrapper.class);

    @Override
    public Collection<Injectable<?>> start(NeoServer neoServer) {
        Jetty9WebServer oldServer = (Jetty9WebServer) ((AbstractNeoServer) neoServer).getWebServer();

        try {
            Field logProvider = AbstractNeoServer.class.getDeclaredField("logProvider");
            logProvider.setAccessible(true);
            GraphAwareJetty9WebServer newWebServer = new GraphAwareJetty9WebServer((LogProvider) logProvider.get(neoServer), neoServer.getDatabase(), neoServer.getConfig());

            Field field = AbstractNeoServer.class.getDeclaredField("webServer");
            field.setAccessible(true);
            field.set(neoServer, newWebServer);

            copyField(oldServer, newWebServer, "staticContent");
            copyField(oldServer, newWebServer, "jaxRSPackages");
            copyField(oldServer, newWebServer, "jaxRSClasses");
            copyField(oldServer, newWebServer, "filters");

            Method method = AbstractNeoServer.class.getDeclaredMethod("configureWebServer");
            method.setAccessible(true);
            method.invoke(neoServer);

        } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return Collections.emptySet();
    }

    private static <T, F> void copyField(T from, T to, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = from.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(to, getField(from, fieldName));
    }

    private static <T, F> F getField(T object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (F) field.get(object);
    }

    @Override
    public void stop() {

    }

    @Override
    public Collection<Injectable<?>> start(GraphDatabaseService graphDatabaseService, Configuration configuration) {
        return Collections.EMPTY_SET;
    }
}
