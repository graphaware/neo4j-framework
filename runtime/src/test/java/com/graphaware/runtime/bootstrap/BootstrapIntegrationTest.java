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

package com.graphaware.runtime.bootstrap;

import com.graphaware.runtime.RuntimeRegistry;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.backup.OnlineBackupSettings;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.config.InvalidSettingException;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.shell.ShellSettings;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static com.graphaware.runtime.bootstrap.RuntimeKernelExtension.RUNTIME_ENABLED;
import static com.graphaware.runtime.bootstrap.TestRuntimeModule.TEST_RUNTIME_MODULES;
import static org.junit.Assert.*;
import static org.neo4j.kernel.configuration.Settings.FALSE;

/**
 * Integration test for runtime and module bootstrapping.
 */
public class BootstrapIntegrationTest {

    @Before
    public void setUp() {
        TEST_RUNTIME_MODULES.clear();
        RuntimeRegistry.clear();
    }

    @Test
    public void moduleShouldNotBeInitializedWhenNoConfigProvided() throws InterruptedException {
        GraphDatabaseService database = builder().newGraphDatabase();

        registerShutdownHook(database);

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());

        database.shutdown();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());
    }

    @Test
    public void moduleShouldNotBeInitializedWhenRuntimeIsDisabled() throws InterruptedException {
        GraphDatabaseService database = builder()
                .setConfig(RUNTIME_ENABLED, "false")
                .setConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .newGraphDatabase();

        registerShutdownHook(database);

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());

        database.shutdown();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());
    }

    @Test(expected = InvalidSettingException.class)
    public void misconfiguredRuntimeShouldFailStartup() throws InterruptedException {
        GraphDatabaseService database = builder()
                .setConfig(RUNTIME_ENABLED, "whatever")
                .setConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .newGraphDatabase();

        registerShutdownHook(database);

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());

        database.shutdown();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());
    }

    @Test
    public void moduleShouldNotBeInitializedWhenRuntimeIsDisabled3() throws InterruptedException {
        GraphDatabaseService database = builder()
                .setConfig(RUNTIME_ENABLED, null)
                .setConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .newGraphDatabase();

        registerShutdownHook(database);

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());

        database.shutdown();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());
    }

    @Test
    public void moduleShouldBeInitializedWhenRuntimeIsEnabled() throws InterruptedException {
        GraphDatabaseService database = builder()
                .setConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .setConfig(TestModuleBootstrapper.MODULE_CONFIG, TestModuleBootstrapper.MODULE_CONFIG.getDefaultValue())
                .newGraphDatabase();

        registerShutdownHook(database);

        try (Transaction tx = database.beginTx()) {
            database.createNode(); //tx just to kick off Runtime init
            tx.success();
        }

        assertEquals(1, TEST_RUNTIME_MODULES.size());
        assertTrue(TEST_RUNTIME_MODULES.get(0).isInitialized());
        assertEquals("configValue", TEST_RUNTIME_MODULES.get(0).getConfig().get("configKey"));

        database.shutdown();

        assertFalse(TEST_RUNTIME_MODULES.get(0).isInitialized());
    }

    @Test
    public void moduleShouldBeInitializedWhenRuntimeIsEnabledWithoutAnyTransactions() throws InterruptedException {
        GraphDatabaseService database = builder()
                .setConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .setConfig(TestModuleBootstrapper.MODULE_CONFIG, TestModuleBootstrapper.MODULE_CONFIG.getDefaultValue())
                .newGraphDatabase();

        registerShutdownHook(database);

        Thread.sleep(1000);

        assertEquals(1, TEST_RUNTIME_MODULES.size());
        assertTrue(TEST_RUNTIME_MODULES.get(0).isInitialized());
        assertEquals("configValue", TEST_RUNTIME_MODULES.get(0).getConfig().get("configKey"));

        database.shutdown();

        assertFalse(TEST_RUNTIME_MODULES.get(0).isInitialized());
    }

    @Test
    public void moduleShouldBeInitializedWhenModuleIsDisabled() throws InterruptedException {
        GraphDatabaseService database = builder()
                .setConfig(TestModuleBootstrapper.MODULE_ENABLED, null)
                .newGraphDatabase();

        registerShutdownHook(database);

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());

        database.shutdown();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());
    }

    @Test
    public void moduleShouldBeInitializedWhenAnotherModuleIsMisConfigured() throws InterruptedException {
        GraphDatabaseService database = builder()
                .setConfig("com.graphaware.module.wrong1.enabled", "com.not.existent.Bootstrapper")
                .setConfig("com.graphaware.module.wrong2.2", "com.not.existent.Bootstrapper")
                .setConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .setConfig(TestModuleBootstrapper.MODULE_CONFIG, TestModuleBootstrapper.MODULE_CONFIG.getDefaultValue())
                .newGraphDatabase();

        registerShutdownHook(database);

        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        assertEquals(1, TEST_RUNTIME_MODULES.size());
        assertTrue(TEST_RUNTIME_MODULES.get(0).isInitialized());
        assertEquals("configValue", TEST_RUNTIME_MODULES.get(0).getConfig().get("configKey"));

        database.shutdown();

        assertFalse(TEST_RUNTIME_MODULES.get(0).isInitialized());
    }

    @Test
    public void modulesShouldBeDelegatedToInCorrectOrder() throws InterruptedException {
        GraphDatabaseService database = builder()
                .setConfig("com.graphaware.module.test1.1", TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .setConfig("com.graphaware.module.test3.3", TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .setConfig("com.graphaware.module.test2.2", TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .newGraphDatabase();

        registerShutdownHook(database);

        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        assertEquals(3, TEST_RUNTIME_MODULES.size());
        assertEquals("test1", TEST_RUNTIME_MODULES.get(0).getId());
        assertEquals("test2", TEST_RUNTIME_MODULES.get(1).getId());
        assertEquals("test3", TEST_RUNTIME_MODULES.get(2).getId());
    }

    @Test
    public void modulesShouldBeDelegatedToInRandomOrderWhenOrderClashes() throws InterruptedException {
        GraphDatabaseService database = builder()
                .setConfig("com.graphaware.module.test1.1", TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .setConfig("com.graphaware.module.test3.1", TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .setConfig("com.graphaware.module.test2.1", TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .newGraphDatabase();

        registerShutdownHook(database);

        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        assertEquals(3, TEST_RUNTIME_MODULES.size());
        Set<String> remaining = new HashSet<>(Arrays.asList("test1", "test2", "test3"));
        assertTrue(remaining.remove(TEST_RUNTIME_MODULES.get(0).getId()));
        assertTrue(remaining.remove(TEST_RUNTIME_MODULES.get(1).getId()));
        assertTrue(remaining.remove(TEST_RUNTIME_MODULES.get(2).getId()));
        assertTrue(remaining.isEmpty());
    }

    private GraphDatabaseBuilder builder() {
        return new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .setConfig(RUNTIME_ENABLED, "true");
    }
}
