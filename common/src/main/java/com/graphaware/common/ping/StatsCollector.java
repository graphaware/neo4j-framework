/*
 * Copyright (c) 2015 GraphAware
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

package com.graphaware.common.ping;

/**
 * A simple component collecting anonymous statistics about the framework usage.
 */
public interface StatsCollector {

    public static final String VERSION = "2.2.1.30";

    /**
     * Report framework start (server mode).
     *
     * @param edition of the framework.
     */
    void frameworkStart(String edition);

    /**
     * Report runtime start (server or embedded mode).
     */
    void runtimeStart();

    /**
     * Report a module start.
     *
     * @param moduleClassName fully qualified name of the module.
     */
    void moduleStart(String moduleClassName);
}
