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

import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.runtime.config.*;
import com.graphaware.runtime.metadata.DefaultTxDrivenModuleMetadata;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.metadata.TxDrivenModuleMetadata;
import com.graphaware.runtime.module.BaseTxDrivenModule;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.runtime.strategy.BatchSupportingTxDrivenModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.junit.Test;
import org.mockito.Mockito;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import static com.graphaware.common.util.IterableUtils.count;
import static com.graphaware.runtime.config.RuntimeConfiguration.GA_METADATA;
import static com.graphaware.runtime.config.RuntimeConfiguration.GA_PREFIX;
import static com.graphaware.runtime.manager.BaseModuleManager.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

/**
 * Base-class for runtime tests.
 */
public abstract class GraphAwareRuntimeTest<T extends TxDrivenModule> {

    protected static final String MOCK = "MOCK";

    protected abstract GraphAwareRuntime createRuntime();

    protected abstract Node createRuntimeRoot();

    protected abstract T createMockModule();

    protected abstract T createMockModule(String id);

    protected abstract T createMockModule(String id, TxDrivenModuleConfiguration configuration);

    protected abstract Transaction getTransaction();

    protected abstract void verifyInitialization(T module);

    protected abstract void verifyReinitialization(T module);

    protected abstract Node createNode(Label... labels);

    protected abstract void shutdown();

    protected abstract ModuleMetadataRepository getRepository();

    @Test
    public void moduleRegisteredForTheFirstTimeShouldBeInitialized() {
        final T mockModule = createMockModule();

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start();

        verifyInitialization(mockModule);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = getTransaction()) {
            TxDrivenModuleMetadata moduleMetadata = getRepository().getModuleMetadata(mockModule);
            assertEquals(NullTxDrivenModuleConfiguration.getInstance(), moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.timestamp());
        }
    }

    @Test
    public void moduleAlreadyRegisteredShouldNotBeInitialized() {
        final TxDrivenModule mockModule = createMockModule();

        try (Transaction tx = getTransaction()) {
            getRepository().persistModuleMetadata(mockModule, new DefaultTxDrivenModuleMetadata(NullTxDrivenModuleConfiguration.getInstance()));
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule).getMetadataClass();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = getTransaction()) {
            TxDrivenModuleMetadata moduleMetadata = getRepository().getModuleMetadata(mockModule);
            assertEquals(NullTxDrivenModuleConfiguration.getInstance(), moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.timestamp());
        }
    }

    @Test
    public void changedModuleShouldBeReInitialized() {
        final T mockModule = createMockModule();

        try (Transaction tx = getTransaction()) {
            getRepository().persistModuleMetadata(mockModule, new DefaultTxDrivenModuleMetadata(new MinimalTxDrivenModuleConfiguration(InclusionStrategies.none())));
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start();

        verifyReinitialization(mockModule);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule).getMetadataClass();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = getTransaction()) {
            TxDrivenModuleMetadata moduleMetadata = getRepository().getModuleMetadata(mockModule);
            assertEquals(NullTxDrivenModuleConfiguration.getInstance(), moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.timestamp());
        }
    }

    @Test
    public void forcedModuleShouldBeReInitialized() {
        final T mockModule = createMockModule();

        try (Transaction tx = getTransaction()) {
            getRepository().persistModuleMetadata(mockModule, new DefaultTxDrivenModuleMetadata(NullTxDrivenModuleConfiguration.getInstance()).markedNeedingInitialization());
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start();

        verifyReinitialization(mockModule);
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule).getMetadataClass();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = getTransaction()) {
            TxDrivenModuleMetadata moduleMetadata = getRepository().getModuleMetadata(mockModule);
            assertEquals(NullTxDrivenModuleConfiguration.getInstance(), moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.timestamp());
        }
    }

    @Test
    public void changedModuleShouldNotBeReInitializedWhenInitializationSkipped() {
        final TxDrivenModule mockModule = createMockModule();

        try (Transaction tx = getTransaction()) {
            getRepository().persistModuleMetadata(mockModule, new DefaultTxDrivenModuleMetadata(new MinimalTxDrivenModuleConfiguration(InclusionStrategies.none())));
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start(true);

        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = getTransaction()) {
            TxDrivenModuleMetadata moduleMetadata = getRepository().getModuleMetadata(mockModule);
            assertEquals(new MinimalTxDrivenModuleConfiguration(InclusionStrategies.none()), moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.timestamp());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterTheSameModuleTwice() {
        final TxDrivenModule mockModule = createMockModule();

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);
        runtime.registerModule(mockModule);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToRegisterModuleWithTheSameIdTwice() {
        final TxDrivenModule mockModule1 = createMockModule();
        final TxDrivenModule mockModule2 = createMockModule();
        when(mockModule1.getId()).thenReturn("ID");
        when(mockModule2.getId()).thenReturn("ID");

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
    }

    @Test
    public void unusedModulesShouldBeRemoved() {
        final TxDrivenModule mockModule = createMockModule();
        final TxDrivenModule unusedModule = createMockModule("UNUSED");

        try (Transaction tx = getTransaction()) {
            getRepository().persistModuleMetadata(mockModule, new DefaultTxDrivenModuleMetadata(NullTxDrivenModuleConfiguration.getInstance()));
            getRepository().persistModuleMetadata(unusedModule, new DefaultTxDrivenModuleMetadata(NullTxDrivenModuleConfiguration.getInstance()));
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule).getMetadataClass();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = getTransaction()) {
            assertEquals(1, getRepository().getAllModuleIds().size());
        }
    }

    @Test
    public void usedCorruptModulesShouldBeReInitialized() {
        final T mockModule = createMockModule();

        try (Transaction tx = getTransaction()) {
            Node root = createRuntimeRoot();
            root.setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, "CORRUPT");
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verifyReinitialization(mockModule);
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = getTransaction()) {
            TxDrivenModuleMetadata moduleMetadata = getRepository().getModuleMetadata(mockModule);
            assertEquals(NullTxDrivenModuleConfiguration.getInstance(), moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.timestamp());
        }
    }

    @Test
    public void usedCorruptModulesShouldBeInitialized2() {
        final T mockModule = createMockModule();

        try (Transaction tx = getTransaction()) {
            Node root = createRuntimeRoot();
            root.setProperty(GA_PREFIX + RUNTIME + "_" + MOCK, new byte[]{2, 3, 4});
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule).getMetadataClass();
        verifyReinitialization(mockModule);
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = getTransaction()) {
            TxDrivenModuleMetadata moduleMetadata = getRepository().getModuleMetadata(mockModule);
            assertEquals(NullTxDrivenModuleConfiguration.getInstance(), moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.timestamp());
        }
    }

    @Test
    public void unusedCorruptModulesShouldBeRemoved() {
        final TxDrivenModule mockModule = createMockModule();

        try (Transaction tx = getTransaction()) {
            Node root = createRuntimeRoot();
            getRepository().persistModuleMetadata(mockModule, new DefaultTxDrivenModuleMetadata(NullTxDrivenModuleConfiguration.getInstance()));
            root.setProperty(GA_PREFIX + RUNTIME + "_UNUSED", "CORRUPT");
            root.setProperty(GA_PREFIX + RUNTIME + "_UNUSED2", new byte[]{1, 2, 3});
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start();

        verify(mockModule, atLeastOnce()).getId();
        verify(mockModule, atLeastOnce()).getConfiguration();
        verify(mockModule).getMetadataClass();
        verifyNoMoreInteractions(mockModule);

        try (Transaction tx = getTransaction()) {
            assertEquals(1, getRepository().getAllModuleIds().size());
        }
    }

    @Test
    public void allRegisteredInterestedModulesShouldBeDelegatedTo() {
        T mockModule1 = createMockModule(MOCK + "1");
        T mockModule2 = createMockModule(MOCK + "2");
        T mockModule3 = createMockModule(MOCK + "3", new MinimalTxDrivenModuleConfiguration(InclusionStrategies.none()));

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
        runtime.registerModule(mockModule3);

        runtime.start();

        verifyInitialization(mockModule1);
        verifyInitialization(mockModule2);
        verifyInitialization(mockModule3);
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule3, atLeastOnce()).getConfiguration();
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);

        try (Transaction tx = getTransaction()) {
            createNode();
            tx.success();
        }

        verify(mockModule1).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule2).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule3, atLeastOnce()).getConfiguration();

        //no interaction with module3, it is not interested!
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);
    }

    @Test
    public void allRegisteredInterestedModulesShouldBeDelegatedToWhenRuntimeIsNotExplicitlyStarted() {
        T mockModule1 = createMockModule(MOCK + "1");
        T mockModule2 = createMockModule(MOCK + "2");
        T mockModule3 = createMockModule(MOCK + "3", new MinimalTxDrivenModuleConfiguration(InclusionStrategies.none()));

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);
        runtime.registerModule(mockModule3);

        //no explicit runtime start!
        try (Transaction tx = getTransaction()) {
            createNode();
            tx.success();
        }

        verifyInitialization(mockModule1);
        verifyInitialization(mockModule2);
        verifyInitialization(mockModule3);
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule3, atLeastOnce()).getConfiguration();
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule3, atLeastOnce()).getId();

        verify(mockModule1).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule2).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule3, atLeastOnce()).getConfiguration();

        //no interaction with module3, it is not interested!
        verifyNoMoreInteractions(mockModule1, mockModule2, mockModule3);
    }

    @Test
    public void moduleThrowingInitExceptionShouldBeMarkedForReinitialization() {
        final TxDrivenModule mockModule = createMockModule();
        when(mockModule.getConfiguration()).thenReturn(NullTxDrivenModuleConfiguration.getInstance());
        Mockito.doThrow(new NeedsInitializationException()).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start();

        try (Transaction tx = getTransaction()) {
            TxDrivenModuleMetadata moduleMetadata = getRepository().getModuleMetadata(mockModule);
            assertEquals(NullTxDrivenModuleConfiguration.getInstance(), moduleMetadata.getConfig());
            assertFalse(moduleMetadata.needsInitialization());
            assertEquals(-1, moduleMetadata.timestamp());
        }

        try (Transaction tx = getTransaction()) {
            createNode();
            tx.success();
        }

        try (Transaction tx = getTransaction()) {
            TxDrivenModuleMetadata moduleMetadata = getRepository().getModuleMetadata(mockModule);
            assertEquals(NullTxDrivenModuleConfiguration.getInstance(), moduleMetadata.getConfig());
            assertTrue(moduleMetadata.needsInitialization());
            assertTrue(moduleMetadata.timestamp() > System.currentTimeMillis() - 1000);
        }
    }

    @Test
    public void moduleThrowingInitExceptionShouldBeMarkedForReinitializationOnlyTheFirstTime() throws InterruptedException {
        final TxDrivenModule mockModule = createMockModule();
        when(mockModule.getConfiguration()).thenReturn(NullTxDrivenModuleConfiguration.getInstance());
        doThrow(new NeedsInitializationException()).when(mockModule).beforeCommit(any(ImprovedTransactionData.class));

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        runtime.start();

        try (Transaction tx = getTransaction()) {
            createNode();
            tx.success();
        }

        long firstFailureTimestamp;
        try (Transaction tx = getTransaction()) {
            firstFailureTimestamp = getRepository().getModuleMetadata(mockModule).timestamp();
        }

        Thread.sleep(1);

        try (Transaction tx = getTransaction()) {
            createNode();
            tx.success();
        }

        long secondFailureTimestamp;
        try (Transaction tx = getTransaction()) {
            secondFailureTimestamp = getRepository().getModuleMetadata(mockModule).timestamp();
        }

        assertEquals(firstFailureTimestamp, secondFailureTimestamp);
    }

    @Test(expected = IllegalStateException.class)
    public void modulesCannotBeRegisteredAfterStart() {
        final TxDrivenModule mockModule = createMockModule();

        GraphAwareRuntime runtime = createRuntime();
        runtime.start(true);
        runtime.registerModule(mockModule);
    }

    @Test
    public void multipleCallsToStartFrameworkHaveNoEffect() {
        GraphAwareRuntime runtime = createRuntime();
        runtime.start();
        runtime.start();
        runtime.start();
        runtime.start();
    }

    @Test
    public void runtimeConfiguredModulesShouldBeConfigured() {
        RuntimeConfiguredRuntimeModule mockModule = mock(RuntimeConfiguredRuntimeModule.class);
        when(mockModule.getId()).thenReturn(MOCK);
        when(mockModule.getConfiguration()).thenReturn(NullTxDrivenModuleConfiguration.getInstance());

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        verify(mockModule).configurationChanged(DefaultRuntimeConfiguration.getInstance());
        verify(mockModule, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule);
    }

    @Test
    public void realRuntimeConfiguredModulesShouldBeConfigured() {
        RealRuntimeConfiguredRuntimeModule module = new RealRuntimeConfiguredRuntimeModule();

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(module);

        assertEquals(DefaultRuntimeConfiguration.getInstance(), module.getConfig());
    }

    @Test(expected = IllegalStateException.class)
    public void unConfiguredModuleShouldThrowException() {
        RealRuntimeConfiguredRuntimeModule module = new RealRuntimeConfiguredRuntimeModule();
        module.getConfig();
    }

    @Test
    public void shutdownShouldBeCalledBeforeShutdown() {
        TxDrivenModule mockModule = createMockModule();

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);
        runtime.start();

        shutdown();

        verify(mockModule).shutdown();
    }



    @Test(expected = IllegalStateException.class)
    public void shouldFailWhenThereAreTwoRootNodes() {
        try (Transaction tx = getTransaction()) {
            createNode(GA_METADATA);
            createNode(GA_METADATA);
            tx.success();
        }

        GraphAwareRuntime runtime = createRuntime();
        runtime.start();
    }

    @Test
    public void whenOneModuleThrowsAnExceptionThenOtherModulesShouldStillBeDelegatedTo() {
        T mockModule1 = createMockModule(MOCK + "1");
        doThrow(new RuntimeException()).when(mockModule1).beforeCommit(any(ImprovedTransactionData.class));

        T mockModule2 = createMockModule(MOCK + "2");

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule1);
        runtime.registerModule(mockModule2);

        runtime.start();

        verifyInitialization(mockModule1);
        verifyInitialization(mockModule2);
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verifyNoMoreInteractions(mockModule1, mockModule2);

        try (Transaction tx = getTransaction()) {
            createNode();
            tx.success();
        }

        verify(mockModule1).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule2).beforeCommit(any(ImprovedTransactionData.class));
        verify(mockModule1, atLeastOnce()).getId();
        verify(mockModule2, atLeastOnce()).getId();
        verify(mockModule1, atLeastOnce()).getConfiguration();
        verify(mockModule2, atLeastOnce()).getConfiguration();
        verifyNoMoreInteractions(mockModule1, mockModule2);
    }

    @Test
    public void runtimeShouldBeStartedAutomatically() {
        final T mockModule = createMockModule();

        GraphAwareRuntime runtime = createRuntime();
        runtime.registerModule(mockModule);

        try (Transaction tx = getTransaction()) {
            createNode();
            tx.success();
        }

        verifyInitialization(mockModule);
    }

    protected interface RuntimeConfiguredRuntimeModule extends TxDrivenModule, RuntimeConfigured {

    }

    protected class RealRuntimeConfiguredRuntimeModule extends BaseTxDrivenModule implements RuntimeConfiguredRuntimeModule {

        private RuntimeConfiguration configuration;

        public RealRuntimeConfiguredRuntimeModule() {
            super("TEST");
        }

        @Override
        public void configurationChanged(RuntimeConfiguration configuration) {
            this.configuration = configuration;
        }

        public RuntimeConfiguration getConfig() {
            if (configuration == null) {
                throw new IllegalStateException("Component hasn't been configured. Has it been registered with the " +
                        "GraphAware runtime?");
            }

            return configuration;
        }

        @Override
        public void beforeCommit(ImprovedTransactionData transactionData) {
            //do nothing
        }
    }
}
