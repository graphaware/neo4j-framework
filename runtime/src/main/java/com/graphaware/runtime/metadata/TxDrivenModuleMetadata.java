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
 * {@link ModuleMetadata} for {@link com.graphaware.runtime.module.TxDrivenModule}s.
 */
public interface TxDrivenModuleMetadata extends ModuleMetadata {

    /**
     * Get the configuration of the module. It is part of the {@link TxDrivenModuleMetadata} in order to detect
     * configuration changes after a restart.
     *
     * @return configuration.
     */
    TxDrivenModuleConfiguration getConfig();

    /**
     * Does the module need initialization?
     *
     * @return true iff the module needs initialization. This is usually the case when something out-of-sync has been
     *         detected during the last run of the database.
     */
    boolean needsInitialization();

    /**
     * Get the time in milliseconds since 1/1/1970 of the first occurrence of a problem that caused the {@link #needsInitialization()}
     * method to return <code>true</code>.
     *
     * @return timestamp of the first problem occurrence, -1 if {@link #needsInitialization()} returns <code>false</code>.
     */
    long problemTimestamp();

    /**
     * Create a new instance of this class with {@link #needsInitialization()} returning <code>true</code>. This must cause
     * {@link #problemTimestamp()} to return time representing the instant when this method was called. Same instance
     * of the implementation of this class should be returned when {@link #needsInitialization()} already returns
     * <code>true</code>.
     *
     * @return new instance of this class with {@link #needsInitialization()} returning <code>true</code>, same instance
     *         if that's already the case.
     */
    TxDrivenModuleMetadata markedNeedingInitialization();
}
