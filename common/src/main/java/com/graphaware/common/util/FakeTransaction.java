package com.graphaware.common.util;

import org.neo4j.graphdb.Lock;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;

/**
 * A fake {@link Transaction}, for framework use only.
 */
public final class FakeTransaction implements Transaction {

    @Override
    public void failure() {
        //intentionally do nothing, this is a fake tx
    }

    @Override
    public void success() {
        //intentionally do nothing, this is a fake tx
    }

    @Override
    public void finish() {
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
