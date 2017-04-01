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

package com.graphaware.tx.executor.single;

import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

/**
 * {@link ExceptionHandlingStrategy} that logs the exception and re-throws it.
 * <p/>
 * Note that this is a singleton.
 */
public final class RethrowException implements ExceptionHandlingStrategy {
    private static final Log LOG = LoggerFactory.getLogger(RethrowException.class);

    private static final RethrowException INSTANCE = new RethrowException();

    /**
     * Get an instance of this strategy.
     *
     * @return singleton instance.
     */
    public static RethrowException getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleException(RuntimeException e) {
        LOG.warn("An exception occurred while executing transaction", e);
        throw e;
    }

    /**
     * Private constructor to enforce singleton.
     */
    private RethrowException() {
    }
}
