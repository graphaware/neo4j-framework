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

package com.graphaware.common.log;

import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.impl.logging.LogService;
import org.neo4j.kernel.impl.spi.KernelContext;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;
import org.neo4j.logging.FormattedLogProvider;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;

/**
 * To be used by the Framework and all modules for logging. Call {@link #getLogger(Class)} to obtain Neo4j's own
 * {@link Log} object for logging. In case Neo4j isn't running when logging, e.g. in unit tests, logging will be done
 * to System.out.
 */
public final class LoggerFactory extends KernelExtensionFactory<LoggerFactory.Dependencies> {

    private static LogProvider LOG_PROVIDER = FormattedLogProvider.toOutputStream(System.out);

    public static Log getLogger(Class<?> clazz) {
        return LOG_PROVIDER.getLog(clazz);
    }

    public interface Dependencies {
        LogService logger();
    }

    public LoggerFactory() {
        super("graphaware-logging");
    }

    @Override
    public Lifecycle newInstance(@SuppressWarnings("unused") KernelContext context, final Dependencies dependencies) throws Throwable {
        return new LifecycleAdapter() {
            @Override
            public void init() throws Throwable {
                LoggerFactory.LOG_PROVIDER = dependencies.logger().getUserLogProvider();
            }
        };
    }
}
