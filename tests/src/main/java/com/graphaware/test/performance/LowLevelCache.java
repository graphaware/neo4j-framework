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

import java.util.HashMap;
import java.util.Map;

/**
 * {@link CacheConfiguration} representing low-level cache available, but not high-level cache.
 */
public class LowLevelCache implements CacheConfiguration {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needsWarmup() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> addToConfig(Map<String, String> existingConfig) {
        Map<String, String> result = new HashMap<>(existingConfig);

        //low level cache
        result.put("dbms.pagecache.memory", "500M");

        //high level cache
        result.put("cache_type", "none");

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "lowcache";
    }
}
