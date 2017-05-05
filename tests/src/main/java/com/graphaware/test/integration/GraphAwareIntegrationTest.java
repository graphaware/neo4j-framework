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

package com.graphaware.test.integration;

import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.procedure.Procedure;

import java.util.Collections;
import java.util.Map;

import static com.graphaware.test.integration.ClassPathProcedureUtils.registerAllProceduresAndFunctions;


/**
 * Base-class for tests of APIs that are written as Spring MVC {@link org.springframework.stereotype.Controller}s
 * and deployed using the GraphAware Framework.
 * <p/>
 * Starts a Neo4j server on the port specified using {@link #neoServerPort()} (or 7575 by default) and deploys all
 * {@link org.springframework.stereotype.Controller} annotated controllers.
 * <p/>
 * Allows implementing tests to call {@link #getDatabase()} and thus gain low-level access to the database
 * even when running within a server. This is useful, for instance, when using
 * {@link com.graphaware.test.unit.GraphUnit} to assert the database state. Before tests are run, the database can be populated
 * by overriding the {@link #populateDatabase(org.neo4j.graphdb.GraphDatabaseService)} method, or by providing a
 * {@link com.graphaware.test.data.DatabasePopulator} in {@link #databasePopulator()}.
 * <p>
 * Also works around the issue that Neo4j only registers procedures that it finds in .jar files (not .class) files, so
 * this class by default registers all {@link Procedure}s that are part of the current project (are on classpath). You
 * can disable this behaviour by overridding {@link #shouldRegisterProceduresAndFunctions()}.
 */
public abstract class GraphAwareIntegrationTest extends ServerIntegrationTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerProceduresAndFunctions(Procedures procedures) throws Exception {
        super.registerProceduresAndFunctions(procedures);

        registerAllProceduresAndFunctions(procedures);
    }

    @Override
    protected Map<String, String> thirdPartyJaxRsPackageMappings() {
        return Collections.singletonMap("com.graphaware.server", "/graphaware");
    }

    public String baseUrl() {
        return baseNeoUrl() + "/graphaware";
    }
}
