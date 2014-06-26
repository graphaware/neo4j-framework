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

package com.graphaware.runtime.module;

import com.graphaware.runtime.config.TxDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.ModuleMetadata;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * A {@link com.graphaware.runtime.TimerAndTxDrivenRuntime} module performing some useful work based on about-to-be-committed transaction data.
 */
public interface RuntimeModule<M extends ModuleMetadata> {

    /**
     * Get a human-readable (ideally short) ID of this module. This ID must be unique across all {@link RuntimeModule}s
     * used in a single {@link com.graphaware.runtime.GraphAwareRuntime} instance.
     *
     * @return short ID of this module.
     */
    String getId();

    /**
     * Perform cleanup if needed before database shutdown.
     */
    void shutdown();

    Class<M> getMetadataClass();
}
