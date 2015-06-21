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

package com.graphaware.server.check;

import com.graphaware.server.web.GraphAwareJetty9WebServer;
import org.apache.commons.configuration.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.AbstractNeoServer;
import org.neo4j.server.NeoServer;
import org.neo4j.server.plugins.Injectable;
import org.neo4j.server.plugins.SPIPluginLifecycle;
import org.neo4j.server.web.Jetty9WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;

@Path("/")
public class GraphAwareChecker implements SPIPluginLifecycle {

    private static final Logger LOG = LoggerFactory.getLogger(GraphAwareChecker.class);

    @GET
    @Produces( MediaType.TEXT_PLAIN )
    @Path( "/" )
    public Response hello()
    {
        // Do stuff with the database
        return Response.status(Response.Status.OK).entity(
                ("OK").getBytes(Charset.forName("UTF-8"))).build();
    }

    @Override
    public Collection<Injectable<?>> start(NeoServer neoServer) {
        Jetty9WebServer webServer = (Jetty9WebServer) ((AbstractNeoServer) neoServer).getWebServer();

        if (!(webServer instanceof GraphAwareJetty9WebServer)) {
            LOG.error("GraphAware hasn't been properly bootstrapped!");
            throw new IllegalStateException("GraphAware hasn't been properly bootstrapped!");
        }

        return Collections.EMPTY_SET;
    }

    @Override
    public void stop() {

    }

    @Override
    public Collection<Injectable<?>> start(GraphDatabaseService graphDatabaseService, Configuration configuration) {
        return Collections.EMPTY_SET;
    }
}
