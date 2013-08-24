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

package com.graphaware.framework.config;

/**
 * Interface for components that have a configuration that can be represented as a String, or that themselves are such
 * configuration. When the configuration changes, its String representation must also change.
 */
public interface ConfigurationAsString {

    /**
     * Return string representation of the configuration. Does not have to be human-readable, but must change when the
     * semantics of the configuration changes.
     *
     * @return configuration as String.
     */
    String asString();
}
