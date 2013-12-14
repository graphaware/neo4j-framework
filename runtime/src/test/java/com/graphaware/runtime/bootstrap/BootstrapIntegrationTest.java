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

package com.graphaware.runtime.bootstrap;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.runtime.bootstrap.RuntimeKernelExtension.RUNTIME_ENABLED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Integration test for runtime and module bootstrapping.
 */
public class BootstrapIntegrationTest {

    @Before
    public void setUp() {
        TestRuntimeModule.reset();
    }

    @Test
    public void moduleShouldNotBeInitializedWhenNoConfigProvided() throws InterruptedException {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        assertFalse(TestRuntimeModule.isInitialized());

        database.shutdown();

        assertFalse(TestRuntimeModule.isInitialized());
    }

    @Test
    public void moduleShouldNotBeInitializedWhenRuntimeIsDisabled() throws InterruptedException {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(RUNTIME_ENABLED, "false")
                .setConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .newGraphDatabase();

        assertFalse(TestRuntimeModule.isInitialized());

        database.shutdown();

        assertFalse(TestRuntimeModule.isInitialized());
    }

    @Test(expected = IllegalArgumentException.class)
    public void misconfiguredRuntimeShouldFailStartup() throws InterruptedException {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(RUNTIME_ENABLED, "whatever")
                .setConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .newGraphDatabase();

        assertFalse(TestRuntimeModule.isInitialized());

        database.shutdown();

        assertFalse(TestRuntimeModule.isInitialized());
    }

    @Test
    public void moduleShouldNotBeInitializedWhenRuntimeIsDisabled3() throws InterruptedException {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(RUNTIME_ENABLED, null)
                .setConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .newGraphDatabase();

        assertFalse(TestRuntimeModule.isInitialized());

        database.shutdown();

        assertFalse(TestRuntimeModule.isInitialized());
    }

    @Test
    public void moduleShouldBeInitializedWhenRuntimeIsEnabled() throws InterruptedException {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(RUNTIME_ENABLED, "true")
                .setConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .newGraphDatabase();

        assertTrue(TestRuntimeModule.isInitialized());

        database.shutdown();

        assertFalse(TestRuntimeModule.isInitialized());
    }

    @Test
    public void moduleShouldBeInitializedWhenModuleIsDisabled() throws InterruptedException {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(RUNTIME_ENABLED, "true")
                .setConfig(TestModuleBootstrapper.MODULE_ENABLED, null)
                .newGraphDatabase();

        assertFalse(TestRuntimeModule.isInitialized());

        database.shutdown();

        assertFalse(TestRuntimeModule.isInitialized());
    }

    @Test
    public void moduleShouldBeInitializedWhenAnotherModuleIsMisConfigured() throws InterruptedException {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(RUNTIME_ENABLED, "true")
                .setConfig("com.graphaware.module.wrong.enabled", "com.not.existent.Bootstrapper")
                .setConfig(TestModuleBootstrapper.MODULE_ENABLED, TestModuleBootstrapper.MODULE_ENABLED.getDefaultValue())
                .newGraphDatabase();

        assertTrue(TestRuntimeModule.isInitialized());

        database.shutdown();

        assertFalse(TestRuntimeModule.isInitialized());
    }
}
