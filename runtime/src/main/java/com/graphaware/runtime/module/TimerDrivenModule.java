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

import com.graphaware.runtime.config.TimerDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.TimerDrivenModuleContext;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Specialisation of {@link RuntimeModule} that can be driven by a timing strategy as opposed to a response to transaction
 * events.
 *
 * @param <C> the type of context that the module persists in Neo4j between runs.
 */
public interface TimerDrivenModule<C extends TimerDrivenModuleContext> extends RuntimeModule {

    /**
     * Create the initial context for this module, when no previously produced context is available.
     *
     * @param database against which the module is running.
     * @return initial context.
     */
    C createInitialContext(GraphDatabaseService database);

    /**
     * Perform the work which is the reason for this module's existence. Implementations can (and should) assume a running
     * transaction.
     *
     * @param lastContext context produced by the last run of this method.
     * @param database    against which the module is running.
     * @return context that will be presented next time the module is run.
     */
    C doSomeWork(C lastContext, GraphDatabaseService database);

    /**
     * Return the configuration of this module. Each module must encapsulate its entire configuration in an instance of
     * a {@link com.graphaware.runtime.config.TimerDrivenModuleConfiguration} implementation. Use {@link com.graphaware.runtime.config.NullTimerDrivenModuleConfiguration}
     * if this module needs no configuration.
     *
     * @return module configuration.
     */
    TimerDrivenModuleConfiguration getConfiguration();
}
