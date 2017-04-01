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

package com.graphaware.runtime.metadata;

import com.graphaware.runtime.config.TxDrivenModuleConfiguration;

/**
 * Default production implementation of {@link TxDrivenModuleMetadata}.
 */
public class DefaultTxDrivenModuleMetadata implements TxDrivenModuleMetadata {

    private final TxDrivenModuleConfiguration configuration;
    private final boolean needsInitialization;
    private final long problemTimestamp;

    /**
     * Construct new metadata. {@link #needsInitialization} will return <code>false</code>.
     *
     * @param configuration module configuration held by the metadata.
     */
    public DefaultTxDrivenModuleMetadata(TxDrivenModuleConfiguration configuration) {
        this(configuration, false, -1);
    }

    private DefaultTxDrivenModuleMetadata(TxDrivenModuleConfiguration configuration, boolean needsInitialization, long problemTimestamp) {
        this.configuration = configuration;
        this.needsInitialization = needsInitialization;
        this.problemTimestamp = problemTimestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxDrivenModuleConfiguration getConfig() {
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    public boolean needsInitialization() {
        return needsInitialization;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long problemTimestamp() {
        return problemTimestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultTxDrivenModuleMetadata markedNeedingInitialization() {
        if (needsInitialization) {
            return this;
        }

        return new DefaultTxDrivenModuleMetadata(configuration, true, System.currentTimeMillis());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultTxDrivenModuleMetadata that = (DefaultTxDrivenModuleMetadata) o;

        if (!configuration.equals(that.configuration)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return configuration.hashCode();
    }
}
