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

package com.graphaware.test.performance;

import java.util.Map;

/**
 * Cache configuration.
 */
public interface CacheConfiguration {

    /**
     * Does cache need to be warmed up?
     *
     * @return true iff cache needs to be warmed up with this configuration.
     */
    boolean needsWarmup();

    /**
     * Add the cache configuration to the database configuration.
     *
     * @param existingConfig existing config.
     * @return new config.
     */
    Map<String, String> addToConfig(Map<String, String> existingConfig);
}
