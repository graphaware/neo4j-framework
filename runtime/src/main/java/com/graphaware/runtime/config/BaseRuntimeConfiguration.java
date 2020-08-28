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
 * Base-class for {@link RuntimeConfiguration} implementations.
 */
public abstract class BaseRuntimeConfiguration implements RuntimeConfiguration {

    private final Config config;
    private final WritingConfig writingConfig;

    protected BaseRuntimeConfiguration(Config config, WritingConfig writingConfig) {
        this.config = config;
        this.writingConfig = writingConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WritingConfig getWritingConfig() {
        return writingConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Config kernelConfig() {
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseRuntimeConfiguration that = (BaseRuntimeConfiguration) o;

        if (!writingConfig.equals(that.writingConfig)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = writingConfig.hashCode();
        return result;
    }
}
