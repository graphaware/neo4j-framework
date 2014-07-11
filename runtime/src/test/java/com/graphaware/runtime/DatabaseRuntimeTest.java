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

package com.graphaware.runtime;

import com.graphaware.common.util.IterableUtils;
import com.graphaware.common.util.PropertyContainerUtils;
import com.graphaware.runtime.config.NullTxDrivenModuleConfiguration;
import com.graphaware.runtime.config.TxDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.junit.Test;
import org.neo4j.graphdb.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link ProductionRuntime}.
 */
public abstract class DatabaseRuntimeTest extends GraphAwareRuntimeTest {

    protected GraphDatabaseService database;
    protected ModuleMetadataRepository txRepo;

    protected abstract Node getMetadataNode();

    protected GraphAwareRuntime createRuntime() {
        return GraphAwareRuntimeFactory.createRuntime(database);
    }

    @Override
    protected TxDrivenModule mockTxModule() {
        return mockTxModule(MOCK);
    }

    @Override
    protected TxDrivenModule mockTxModule(String id) {
        return mockTxModule(id, NullTxDrivenModuleConfiguration.getInstance());
    }

    @Override
    protected TxDrivenModule mockTxModule(String id, TxDrivenModuleConfiguration configuration) {
        TxDrivenModule mockModule = mock(TxDrivenModule.class);
        when(mockModule.getId()).thenReturn(id);
        when(mockModule.getConfiguration()).thenReturn(configuration);
        when(mockModule.beforeCommit(any(ImprovedTransactionData.class))).thenReturn("TEST_" + id);
        return mockModule;
    }

    protected Transaction getTransaction() {
        return database.beginTx();
    }

    @Override
    protected void verifyInitialization(TxDrivenModule module) {
        verify(module).initialize(database);
    }

    @Override
    protected void verifyReinitialization(TxDrivenModule module) {
        verify(module).reinitialize(database);
    }

    @Override
    protected void verifyStart(TxDrivenModule module) {
        verify(module).start(database);
    }

    @Override
    protected Node createNode(Label... labels) {
        return database.createNode(labels);
    }

    protected void shutdown() {
        database.shutdown();
    }

    @Override
    protected ModuleMetadataRepository getTxRepo() {
        return txRepo;
    }

    @Override
    protected long countNodes() {
        try (Transaction tx = database.beginTx()) {
            for (Node node : database.getAllNodes()) {
                System.out.println(PropertyContainerUtils.nodeToString(node));
            }
            tx.success();
        }

        long count;
        try (Transaction tx = database.beginTx()) {
            count = IterableUtils.count(database.getAllNodes());
            tx.success();
        }
        return count;
    }

    @Test
    public void shouldCreateRuntimeMetadataNodeAfterFirstStartup() {
        assertNull(getMetadataNode());

        GraphAwareRuntime runtime = createRuntime();

        assertNull(getMetadataNode());

        runtime.start();

        assertNotNull(getMetadataNode());
    }
}
