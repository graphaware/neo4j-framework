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

package com.graphaware.runtime.module;

import com.graphaware.runtime.config.NullTimerDrivenModuleConfiguration;
import com.graphaware.runtime.config.TimerDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.TimerDrivenModuleContext;

/**
 * Base class for {@link TimerDrivenModule} implementations.
 *
 * @param <C> the type of context that the module persists in Neo4j between runs.
 */
public abstract class BaseTimerDrivenModule<C extends TimerDrivenModuleContext> extends BaseRuntimeModule implements TimerDrivenModule<C> {

    /**
     * Construct a new module.
     *
     * @param moduleId ID of this module. Must not be <code>null</code>.
     */
    protected BaseTimerDrivenModule(String moduleId) {
        super(moduleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimerDrivenModuleConfiguration getConfiguration() {
        return NullTimerDrivenModuleConfiguration.getInstance();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that the implementation in this base class doesn't do anything and can be safely overridden without calling super.
     * </p>
     */
    @Override
    public void shutdown() {
        //to be overridden
    }
}
