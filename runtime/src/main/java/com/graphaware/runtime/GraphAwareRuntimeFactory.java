/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime;

import com.graphaware.runtime.manager.CommunityModuleManager;
import com.graphaware.runtime.manager.ModuleManager;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Factory producing {@link GraphAwareRuntime}. This should be the only way a runtime is created.
 */
public final class GraphAwareRuntimeFactory {

    /**
     * Create a runtime for the given database.
     *
     * @param service       Neo4j database management service.
     * @param database      for which the runtime is being created.
     * @return runtime.
     */
    public static GraphAwareRuntime createRuntime(DatabaseManagementService service, GraphDatabaseService database) {
        ModuleManager moduleManager = new CommunityModuleManager(database);

        return new CommunityRuntime(database, service, moduleManager);
    }

    private GraphAwareRuntimeFactory() {
    }
}
