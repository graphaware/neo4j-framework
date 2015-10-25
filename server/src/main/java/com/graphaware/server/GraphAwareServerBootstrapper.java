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

//    @GET
//    @Produces( MediaType.TEXT_PLAIN )
//    @Path( "/hello" )
//    public Response hello()
//    {
//        // Do stuff with the database
//        return Response.status(Response.Status.OK).entity(
//                ("OK").getBytes(Charset.forName("UTF-8"))).build();
//    }

    @Override
    public Collection<Injectable<?>> start(NeoServer neoServer) {
        Jetty9WebServer oldServer = (Jetty9WebServer) ((AbstractNeoServer) neoServer).getWebServer();

        try {
            Field logProvider1 = AbstractNeoServer.class.getDeclaredField("logProvider");
            logProvider1.setAccessible(true);
            LogProvider logProvider = (LogProvider) logProvider1.get(neoServer);
            GraphAwareJetty9WebServer newWebServer = new GraphAwareJetty9WebServer(logProvider, neoServer.getDatabase(), neoServer.getConfig());

            Field field =  AbstractNeoServer.class.getDeclaredField("webServer");
            field.setAccessible(true);
            field.set(neoServer, newWebServer);

            copyField(Jetty9WebServer.class, oldServer, newWebServer, "staticContent");
            copyField(Jetty9WebServer.class, oldServer, newWebServer, "jaxRSPackages");
            copyField(Jetty9WebServer.class, oldServer, newWebServer, "jaxRSClasses");
            copyField(Jetty9WebServer.class, oldServer, newWebServer, "filters");

            Method method = AbstractNeoServer.class.getDeclaredMethod("configureWebServer");
            method.setAccessible(true);
            method.invoke(neoServer);

        } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return Collections.EMPTY_SET;
    }

    private static <T,U> void copyField(Class<T> cls, T from, T to, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = Jetty9WebServer.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(to, field.get(from));
    }

    @Override
    public void stop() {

    }

    @Override
    public Collection<Injectable<?>> start(GraphDatabaseService graphDatabaseService, Configuration configuration) {
        return Collections.EMPTY_SET;
    }
}
