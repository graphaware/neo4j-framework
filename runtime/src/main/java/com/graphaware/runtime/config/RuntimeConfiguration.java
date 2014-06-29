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

package com.graphaware.runtime.config;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;

/**
 * {@link com.graphaware.runtime.GraphAwareRuntime} configuration.
 */
public interface RuntimeConfiguration {

    /**
     * Prefix for GraphAware internal nodes, relationships, and properties. This is fixed as there is little chance
     * that users would have a reason to change it.
     */
    public static final String GA_PREFIX = "_GA_";

    /**
     * Label of the runtime metadata node.
     */
    public static final Label GA_METADATA = DynamicLabel.label(GA_PREFIX + "METADATA");

    /**
     * Prefix for property keys of properties storing {@link com.graphaware.runtime.metadata.TxDrivenModuleMetadata}.
     */
    public static final String TX_MODULES_PROPERTY_PREFIX = "TX_MODULE";

    /**
     * Prefix for property keys of properties storing {@link com.graphaware.runtime.metadata.TimerDrivenModuleMetadata}.
     */
    public static final String TIMER_MODULES_PROPERTY_PREFIX = "TIMER_MODULE";

    /**
     * Initial timer delay for timer driven module work after the runtime starts. This is temporary and will go away
     * when adaptive timing is properly implemented.
     */
    public static final long INITIAL_TIMER_DELAY = 1000;

    /**
     * Timer delay for timer driven module work intervals. This is temporary and will go away
     * when adaptive timing is properly implemented.
     */
    public static final long TIMER_DELAY = 200;

    /**
     * Create prefix a component should use for internal data it reads/writes (nodes, relationships, properties).
     *
     * @param id of the component/module.
     * @return prefix of the form {@link #GA_PREFIX} + id + "_"
     */
    String createPrefix(String id);
}
