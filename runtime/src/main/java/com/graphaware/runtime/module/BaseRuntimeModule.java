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

import static org.springframework.util.Assert.hasLength;

/**
 * Base class for {@link com.graphaware.runtime.module.RuntimeModule} implementations.
 *
 */
public abstract class BaseRuntimeModule implements RuntimeModule {

    private final String moduleId;

    /**
     * Construct a new module.
     *
     * @param moduleId ID of this module. Must not be <code>null</code> or empty.
     */
    protected BaseRuntimeModule(String moduleId) {
        hasLength(moduleId);

        this.moduleId = moduleId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return moduleId;
    }
}
