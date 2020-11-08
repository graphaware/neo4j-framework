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

package com.graphaware.runtime.module;

import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.config.NullModuleConfiguration;
import com.graphaware.runtime.config.ModuleConfiguration;
import org.neo4j.graphdb.GraphDatabaseService;

import static org.springframework.util.Assert.hasLength;

/**
 * Base class for {@link Module} implementations.
 *
 * @param <T> The type of a state object that the module can use to
 *            pass information from the {@link #beforeCommit(com.graphaware.tx.event.improved.api.ImprovedTransactionData)}
 *            method to the {@link #afterCommit(Object)} method.
 */
public abstract class BaseModule<T> implements Module<T> {

    private final String moduleId;

    /**
     * Construct a new module.
     *
     * @param moduleId ID of this module. Must not be <code>null</code> or empty.
     */
    protected BaseModule(String moduleId) {
        hasLength(moduleId);

        this.moduleId = moduleId;
    }

    @Override
    public String getId() {
        return moduleId;
    }

    @Override
    public ModuleConfiguration getConfiguration() {
        return NullModuleConfiguration.getInstance();
    }

    @Override
    public void start(GraphAwareRuntime runtime) {
        //to be overridden
    }

    @Override
    public void shutdown() {
        //to be overridden
    }

    @Override
    public void afterCommit(T state) {
        //allow subclasses to override
    }

    @Override
    public void afterRollback(T state) {
        //allow subclasses to override
    }
}
