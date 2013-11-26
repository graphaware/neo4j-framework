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

package com.graphaware.kernel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.test.TestGraphDatabaseBuilder;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Integration test for framework and module bootstrapping.
 */
public class BootstrapIntegrationTest {

    @Before
    public void setUp() {
        TestModule.reset();
    }

    @Test
    public void moduleShouldNotBeInitializedWhenNoConfigProvided() throws InterruptedException {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        assertFalse(TestModule.isInitialized());

        database.shutdown();

        assertFalse(TestModule.isInitialized());
    }

    @Test
    public void moduleShouldNotBeInitializedWhenFrameworkIsDisabled() throws InterruptedException {
        Map<String, String> config = MapUtil.stringMap(
                "com.graphaware.framework.enabled", "false",
                "com.graphaware.module.test.enabled", "com.graphaware.kernel.TestModuleBootstrapper"
        );

        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder().setConfig(config).newGraphDatabase();

        assertFalse(TestModule.isInitialized());

        database.shutdown();

        assertFalse(TestModule.isInitialized());
    }

    @Test
    public void moduleShouldNotBeInitializedWhenFrameworkIsDisabled2() throws InterruptedException {
        Map<String, String> config = MapUtil.stringMap(
                "com.graphaware.framework.enabled", "whatever",
                "com.graphaware.module.test.enabled", "com.graphaware.kernel.TestModuleBootstrapper"
        );

        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder().setConfig(config).newGraphDatabase();

        assertFalse(TestModule.isInitialized());

        database.shutdown();

        assertFalse(TestModule.isInitialized());
    }

    @Test
    public void moduleShouldBeInitializedWhenFrameworkIsEnabled() throws InterruptedException {
        Map<String, String> config = MapUtil.stringMap(
                "com.graphaware.framework.enabled", "true",
                "com.graphaware.module.test.enabled", "com.graphaware.kernel.TestModuleBootstrapper"
        );

        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder().setConfig(config).newGraphDatabase();

        assertTrue(TestModule.isInitialized());

        database.shutdown();

        assertFalse(TestModule.isInitialized());
    }

    @Test
    public void moduleShouldBeInitializedWhenFrameworkIsEnabled2() throws InterruptedException {
        Map<String, String> config = MapUtil.stringMap(
                "com.graphaware.framework.enabled", "1",
                "com.graphaware.module.test.enabled", "com.graphaware.kernel.TestModuleBootstrapper"
        );

        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder().setConfig(config).newGraphDatabase();

        assertTrue(TestModule.isInitialized());

        database.shutdown();

        assertFalse(TestModule.isInitialized());
    }

    @Test
    public void moduleShouldBeInitializedWhenAnotherModuleIsMisConfigured() throws InterruptedException {
        Map<String, String> config = MapUtil.stringMap(
                "com.graphaware.framework.enabled", "1",
                "com.graphaware.module.wrong.enabled", "com.not.existent.Bootstrapper",
                "com.graphaware.module.test.enabled", "com.graphaware.kernel.TestModuleBootstrapper"
        );

        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder().setConfig(config).newGraphDatabase();

        assertTrue(TestModule.isInitialized());

        database.shutdown();

        assertFalse(TestModule.isInitialized());
    }
}
