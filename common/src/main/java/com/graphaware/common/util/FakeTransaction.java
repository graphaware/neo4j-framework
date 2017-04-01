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

package com.graphaware.common.util;

import org.neo4j.graphdb.Lock;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;

/**
 * A fake {@link Transaction}, for framework use only.
 */
public final class FakeTransaction implements Transaction {

    @Override
    public void terminate() {
        //intentionally do nothing, this is a fake tx
    }

    @Override
    public void failure() {
        //intentionally do nothing, this is a fake tx
    }

    @Override
    public void success() {
        //intentionally do nothing, this is a fake tx
    }

    @Override
    public void close() {
        //intentionally do nothing, this is a fake tx
    }

    @Override
    public Lock acquireWriteLock(PropertyContainer entity) {
        throw new UnsupportedOperationException("Fake tx!");
    }

    @Override
    public Lock acquireReadLock(PropertyContainer entity) {
        throw new UnsupportedOperationException("Fake tx!");
    }
}
