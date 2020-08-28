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

package com.graphaware.runtime.config;

import com.graphaware.runtime.write.WritingConfig;
import org.neo4j.kernel.configuration.Config;

/**
 * {@link com.graphaware.runtime.GraphAwareRuntime} configuration.
 */
public interface RuntimeConfiguration {

    /**
     * Prefix for GraphAware internal nodes, relationships, and properties. This is fixed as there is little chance
     * that users would have a reason to change it.
     */
    String GA_PREFIX = "_GA_";

    /**
     * Retrieves the {@link WritingConfig} used for configuring a {@link com.graphaware.writer.neo4j.Neo4jWriter}.
     *
     * @return The {@link WritingConfig}, may not be null.
     */
    WritingConfig getWritingConfig();

    /**
     * @return Neo4j kernel config.
     */
    Config kernelConfig();
}
