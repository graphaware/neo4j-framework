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

import com.graphaware.runtime.metadata.ModuleMetadata;

/**
 * A module performing some useful work on the graph in the background, being delegated to by {@link com.graphaware.runtime.GraphAwareRuntime}.
 *
 * @param <M> type of metadata this module needs.
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

    /**
     * Get the concrete class used to hold metadata for this module. This is needed so that the framework knows, which
     * class has been serialized / needs to be deserialized.
     *
     * @return class holding module metadata.
     */
    Class<M> getMetadataClass();
}
