package com.graphaware.neo4j.it;

import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

class NoWaitStrategy extends AbstractWaitStrategy implements WaitStrategy {

    @Override
    protected void waitUntilReady() {
        // no-op
    }

}
