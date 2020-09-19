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
import com.graphaware.test.integration.GraphAwareNeo4jBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.kernel.extension.ExtensionFactory;

import java.util.*;

import static com.graphaware.runtime.bootstrap.RuntimeKernelExtension.RUNTIME_ENABLED_CONFIG;
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

    @AfterEach
    public void tearDown() {
        GraphAwareNeo4jBuilder.cleanup();
    }

    @Test
    public void moduleShouldNotBeInitializedWhenNoConfigProvided() {
        Neo4j database = builder().build();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());

        database.close();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());
    }

    @Test
    public void moduleShouldNotBeInitializedWhenRuntimeIsDisabled() throws ConfigurationException {
        Neo4j database = builder()
                .withGAConfig(RUNTIME_ENABLED_CONFIG, "false")
                .withGAConfig("com.graphaware.module.test.1", TestModuleBootstrapper.class.getCanonicalName())
                .withGAConfig("com.graphaware.module.test.configKey", "configValue")
                .build();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());

        database.close();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());
    }

    @Test
    public void moduleShouldBeInitializedWhenRuntimeIsEnabled() {
        Neo4j database = builder()
                .withGAConfig(RUNTIME_ENABLED_CONFIG, "true")
                .withGAConfig("com.graphaware.module.test.1", TestModuleBootstrapper.class.getCanonicalName())
                .withGAConfig("com.graphaware.module.test.configKey", "configValue")
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
                .withGAConfig(RUNTIME_ENABLED_CONFIG, "true")
                .withGAConfig("com.graphaware.module.test.1", TestModuleBootstrapper.class.getCanonicalName())
                .withGAConfig("com.graphaware.module.test.configKey", "configValue")
                .build();

        Thread.sleep(1000);

        assertEquals(1, TEST_RUNTIME_MODULES.size());
        assertEquals("configValue", TEST_RUNTIME_MODULES.get(0).getConfig().get("configKey"));

        database.close();
    }

    @Test
    public void moduleShouldBeInitializedWhenModuleIsDisabled() throws InterruptedException {
        Neo4j database = builder()
                .withGAConfig(RUNTIME_ENABLED_CONFIG, "true")
                .build();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());

        database.close();

        assertTrue(TEST_RUNTIME_MODULES.isEmpty());
    }

    @Test
    public void moduleShouldBeInitializedWhenAnotherModuleIsMisConfigured() throws InterruptedException {
        Neo4j database = builder()
                .withGAConfig(RUNTIME_ENABLED_CONFIG, "true")
                .withGAConfig("com.graphaware.module.wrong1.enabled", "com.not.existent.Bootstrapper")
                .withGAConfig("com.graphaware.module.wrong2.2", "com.not.existent.Bootstrapper")
                .withGAConfig("com.graphaware.module.test.1", TestModuleBootstrapper.class.getCanonicalName())
                .withGAConfig("com.graphaware.module.test.configKey", "configValue")
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
                .withGAConfig(RUNTIME_ENABLED_CONFIG, "true")
                .withGAConfig("com.graphaware.module.test1.1", TestModuleBootstrapper.class.getCanonicalName())
                .withGAConfig("com.graphaware.module.test3.3", TestModuleBootstrapper.class.getCanonicalName())
                .withGAConfig("com.graphaware.module.test2.2", TestModuleBootstrapper.class.getCanonicalName())
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
                .withGAConfig(RUNTIME_ENABLED_CONFIG, "true")
                .withGAConfig("com.graphaware.module.test1.1", TestModuleBootstrapper.class.getCanonicalName())
                .withGAConfig("com.graphaware.module.test3.1", TestModuleBootstrapper.class.getCanonicalName())
                .withGAConfig("com.graphaware.module.test2.1", TestModuleBootstrapper.class.getCanonicalName())
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

    private GraphAwareNeo4jBuilder builder() {
        List<ExtensionFactory<?>> factories = Collections.singletonList(new RuntimeExtensionFactory());
        return GraphAwareNeo4jBuilder.builder(Neo4jBuilders.newInProcessBuilder().withExtensionFactories(new ArrayList<>(factories)));
    }

}
