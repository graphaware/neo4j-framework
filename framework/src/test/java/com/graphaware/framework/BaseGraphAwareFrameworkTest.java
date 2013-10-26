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

package com.graphaware.framework;

import com.graphaware.framework.GraphAwareModule;
import com.graphaware.framework.config.BaseFrameworkConfiguration;
import com.graphaware.framework.config.BaseFrameworkConfigured;
import com.graphaware.framework.config.FrameworkConfiguration;
import com.graphaware.framework.config.FrameworkConfigured;
import com.graphaware.framework.strategy.InclusionStrategiesImpl;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.event.improved.strategy.InclusionStrategies;
import org.neo4j.graphdb.GraphDatabaseService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base-class for framework tests.
 */
public abstract class BaseGraphAwareFrameworkTest {

    protected static final String TEST_CONFIG = "test config";
    protected static final String MOCK = "MOCK";

    protected GraphAwareModule createMockModule() {
        GraphAwareModule mockModule = mock(GraphAwareModule.class);
        when(mockModule.getId()).thenReturn(MOCK);
        when(mockModule.asString()).thenReturn(TEST_CONFIG);
        return mockModule;
    }

    protected interface FrameworkConfiguredModule extends GraphAwareModule, FrameworkConfigured {

    }

    protected class RealFrameworkConfiguredModule extends BaseFrameworkConfigured implements FrameworkConfiguredModule {

        //make public
        @Override
        protected FrameworkConfiguration getConfig() {
            return super.getConfig();
        }

        @Override
        public String getId() {
            return "TEST";
        }

        @Override
        public String asString() {
            return "someConfig";
        }

        @Override
        public void initialize(GraphDatabaseService database) {
            //do nothing
        }

        @Override
        public void reinitialize(GraphDatabaseService database) {
            //do nothing
        }

        @Override
        public void initialize(TransactionSimulatingBatchInserter batchInserter) {
            //do nothing
        }

        @Override
        public void reinitialize(TransactionSimulatingBatchInserter batchInserter) {
            //do nothing
        }

        @Override
        public void shutdown() {
            //do nothing
        }

        @Override
        public void beforeCommit(ImprovedTransactionData transactionData) {
            //do nothing
        }

        @Override
        public InclusionStrategies getInclusionStrategies() {
            return InclusionStrategiesImpl.all();
        }
    }

    protected class CustomConfig extends BaseFrameworkConfiguration implements FrameworkConfiguration {

      //todo pointless
    }
}
