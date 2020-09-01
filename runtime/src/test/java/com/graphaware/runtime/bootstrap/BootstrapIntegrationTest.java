/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime.bootstrap;

import com.graphaware.runtime.RuntimeRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.configuration.SettingImpl;
import org.neo4j.configuration.SettingValueParsers;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilder;
import org.neo4j.harness.Neo4jBuilders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.graphaware.runtime.bootstrap.RuntimeKernelExtension.RUNTIME_ENABLED;
import static com.graphaware.runtime.bootstrap.TestModule.TEST_RUNTIME_MODULES;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for runtime and module bootstrapping.
 */
public class BootstrapIntegrationTest {

    @BeforeEach
    public void setUp() {
        TEST_RUNTIME_MODULES.clear();
        RuntimeRegistry.clear();
    }

    @Test
    public void moduleShouldNotBeInitializedWhenNoConfigProvided() throws InterruptedException {
        Neo4j database = builder().build();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());

        database.close();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());
    }

    @Test
    public void moduleShouldNotBeInitializedWhenRuntimeIsDisabled() throws InterruptedException {
        Neo4j database = builder()
                .withConfig(RUNTIME_ENABLED, false)
                .withConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.defaultValue())
                .build();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());

        database.close();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());
    }

    @Test
    public void misconfiguredRuntimeShouldFailStartup() throws InterruptedException {
        assertThrows(RuntimeException.class, () -> { //todo more concrete exception?
            Neo4j database = builder()
                    .withConfig(SettingImpl.newBuilder("com.graphaware.runtime.enabled", SettingValueParsers.STRING, "whatever").build(), "whatever")
                    .withConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.defaultValue())
                    .build();
        });

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());
    }

    @Test
    public void moduleShouldNotBeInitializedWhenRuntimeIsDisabled3() throws InterruptedException {
        Neo4j database = builder()
                .withConfig(RUNTIME_ENABLED, null)
                .withConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.defaultValue())
                .build();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());

        database.close();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());
    }

    @Test
    public void moduleShouldBeInitializedWhenRuntimeIsEnabled() throws InterruptedException {
        Neo4j database = builder()
                .withConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.defaultValue())
                .withConfig(TestModuleBootstrapper.MODULE_CONFIG, TestModuleBootstrapper.MODULE_CONFIG.defaultValue())
                .build();

        try (Transaction tx = database.defaultDatabaseService().beginTx()) {
            tx.createNode(); //tx just to kick off Runtime init
            tx.commit();
        }

        assertEquals(1, TEST_RUNTIME_MODULES.size());
        assertEquals("configValue", TEST_RUNTIME_MODULES.get(0).getConfig().get("configKey"));

        database.close();
    }

    @Test
    public void moduleShouldBeInitializedWhenRuntimeIsEnabledWithoutAnyTransactions() throws InterruptedException {
        Neo4j database = builder()
                .withConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.defaultValue())
                .withConfig(TestModuleBootstrapper.MODULE_CONFIG, TestModuleBootstrapper.MODULE_CONFIG.defaultValue())
                .build();

        Thread.sleep(1000);

        assertEquals(1, TEST_RUNTIME_MODULES.size());
        assertEquals("configValue", TEST_RUNTIME_MODULES.get(0).getConfig().get("configKey"));

        database.close();
    }

    @Test
    public void moduleShouldBeInitializedWhenModuleIsDisabled() throws InterruptedException {
        Neo4j database = builder()
                .withConfig(TestModuleBootstrapper.MODULE_ENABLED, null)
                .build();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());

        database.close();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());
    }

    @Test
    public void moduleShouldBeInitializedWhenAnotherModuleIsMisConfigured() throws InterruptedException {
        Neo4j database = builder()
                .withConfig(SettingImpl.newBuilder("com.graphaware.module.wrong1.enabled", SettingValueParsers.STRING, "com.not.existent.Bootstrapper").build(), "com.not.existent.Bootstrapper")
                .withConfig(SettingImpl.newBuilder("com.graphaware.module.wrong2.2", SettingValueParsers.STRING, "com.not.existent.Bootstrapper").build(), "com.not.existent.Bootstrapper")
                .withConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.defaultValue())
                .withConfig(TestModuleBootstrapper.MODULE_CONFIG, TestModuleBootstrapper.MODULE_CONFIG.defaultValue())
                .build();

        try (Transaction tx = database.defaultDatabaseService().beginTx()) {
            tx.createNode();
            tx.commit();
        }

        assertEquals(1, TEST_RUNTIME_MODULES.size());
        assertEquals("configValue", TEST_RUNTIME_MODULES.get(0).getConfig().get("configKey"));

        database.close();
    }

    @Test
    public void modulesShouldBeDelegatedToInCorrectOrder() throws InterruptedException {
        Neo4j database = builder()
                .withConfig(SettingImpl.newBuilder("com.graphaware.module.test1.1", SettingValueParsers.STRING, TestModuleBootstrapper.MODULE_ENABLED.defaultValue()).build(), TestModuleBootstrapper.MODULE_ENABLED.defaultValue())
                .withConfig(SettingImpl.newBuilder("com.graphaware.module.test3.3", SettingValueParsers.STRING, TestModuleBootstrapper.MODULE_ENABLED.defaultValue()).build(), TestModuleBootstrapper.MODULE_ENABLED.defaultValue())
                .withConfig(SettingImpl.newBuilder("com.graphaware.module.test2.2", SettingValueParsers.STRING, TestModuleBootstrapper.MODULE_ENABLED.defaultValue()).build(), TestModuleBootstrapper.MODULE_ENABLED.defaultValue())
                .build();

        try (Transaction tx = database.defaultDatabaseService().beginTx()) {
            tx.createNode();
            tx.commit();
        }

        assertEquals(3, TEST_RUNTIME_MODULES.size());
        assertEquals("test1", TEST_RUNTIME_MODULES.get(0).getId());
        assertEquals("test2", TEST_RUNTIME_MODULES.get(1).getId());
        assertEquals("test3", TEST_RUNTIME_MODULES.get(2).getId());

        database.close();
    }

    @Test
    public void modulesShouldBeDelegatedToInRandomOrderWhenOrderClashes() throws InterruptedException {
        Neo4j database = builder()
                .withConfig(SettingImpl.newBuilder("com.graphaware.module.test1.1", SettingValueParsers.STRING, TestModuleBootstrapper.MODULE_ENABLED.defaultValue()).build(), TestModuleBootstrapper.MODULE_ENABLED.defaultValue())
                .withConfig(SettingImpl.newBuilder("com.graphaware.module.test3.1", SettingValueParsers.STRING, TestModuleBootstrapper.MODULE_ENABLED.defaultValue()).build(), TestModuleBootstrapper.MODULE_ENABLED.defaultValue())
                .withConfig(SettingImpl.newBuilder("com.graphaware.module.test2.1", SettingValueParsers.STRING, TestModuleBootstrapper.MODULE_ENABLED.defaultValue()).build(), TestModuleBootstrapper.MODULE_ENABLED.defaultValue())
                .build();

        try (Transaction tx = database.defaultDatabaseService().beginTx()) {
            tx.createNode();
            tx.commit();
        }

        assertEquals(3, TEST_RUNTIME_MODULES.size());
        Set<String> remaining = new HashSet<>(Arrays.asList("test1", "test2", "test3"));
        assertTrue(remaining.remove(TEST_RUNTIME_MODULES.get(0).getId()));
        assertTrue(remaining.remove(TEST_RUNTIME_MODULES.get(1).getId()));
        assertTrue(remaining.remove(TEST_RUNTIME_MODULES.get(2).getId()));
        assertTrue(remaining.isEmpty());

        database.close();
    }

    private Neo4jBuilder builder() {
        return Neo4jBuilders.newInProcessBuilder().withConfig(RUNTIME_ENABLED, true);
    }
}
