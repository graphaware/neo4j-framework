/*
 * Copyright (c) 2013 GraphAware
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

package com.graphaware.relcount.bootstrap;

import com.graphaware.framework.GraphAwareFramework;
import com.graphaware.framework.GraphAwareModuleBootstrapper;
import com.graphaware.kernel.GraphAwareExtensionFactory;
import com.graphaware.relcount.module.RelationshipCountModule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.lifecycle.Lifecycle;

/**
 * {@link GraphAwareModuleBootstrapper} for {@link com.graphaware.relcount.module.RelationshipCountModule}.
 */
public class RelcountModuleBootstrapper implements GraphAwareModuleBootstrapper {

    /**
     * {@inheritDoc}
     */
    @Override
    public void bootstrap(GraphAwareFramework framework, Config config) {
        framework.registerModule(new RelationshipCountModule());
    }
}
