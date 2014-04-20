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

package com.graphaware.runtime.bootstrap;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.lifecycle.Lifecycle;

/**
 * {@link KernelExtensionFactory} that initializes the {@link RuntimeKernelExtension}.
 */
public class RuntimeKernelExtensionFactory extends KernelExtensionFactory<RuntimeKernelExtensionFactory.Dependencies> {

    public interface Dependencies {
        Config getConfig();

        GraphDatabaseService getDatabase();
    }

    public static final String KEY = "GraphAware Runtime";

    public RuntimeKernelExtensionFactory() {
        super(KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Lifecycle newKernelExtension(Dependencies dependencies) throws Throwable {
        return new RuntimeKernelExtension(dependencies.getConfig(), dependencies.getDatabase());
    }
}
