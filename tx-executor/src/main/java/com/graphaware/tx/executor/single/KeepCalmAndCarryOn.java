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
 * {@link ExceptionHandlingStrategy} that merely logs the exception. It is good for batch imports, for instance, when we
 * don't want a single exception messing up the whole import. Instead, we'd like the current transaction to roll-back,
 * start a new one, and carry on.
 * <p/>
 * Note that this is a singleton.
 */
public final class KeepCalmAndCarryOn implements ExceptionHandlingStrategy {
    private static final Log LOG = LoggerFactory.getLogger(KeepCalmAndCarryOn.class);

    private static final KeepCalmAndCarryOn INSTANCE = new KeepCalmAndCarryOn();

    /**
     * Get an instance of this strategy.
     *
     * @return singleton instance.
     */
    public static KeepCalmAndCarryOn getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleException(RuntimeException e) {
        LOG.warn("An exception occurred while executing transaction", e);
    }

    /**
     * Private constructor to enforce singleton.
     */
    private KeepCalmAndCarryOn() {
    }
}
