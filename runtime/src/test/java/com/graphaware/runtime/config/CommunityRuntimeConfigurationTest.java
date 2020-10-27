package com.graphaware.runtime.config;

import org.apache.commons.configuration2.MapConfiguration;
import org.junit.Test;
import org.mockito.Mockito;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CommunityRuntimeConfigurationTest {

    @Test
    public void shouldReadConfigurationCorrectly() {
        GraphDatabaseService mockDb = Mockito.mock(GraphDatabaseService.class);
        Mockito.when(mockDb.databaseName()).thenReturn("neo4j");

        ConfigurationReader dummyReader = () -> {
            Map<String, String> config = new HashMap<>();
            config.put("com.graphaware.module.neo4j.MODULE_ID.1", "com.graphaware.FirstBootstrapper");
            config.put("com.graphaware.module.neo4j.MODULE_ID.testKey", "testValue1");
            config.put("com.graphaware.module.neo4j.MODULE_ID2.2", "com.graphaware.SecondBootstrapper");
            config.put("com.graphaware.module.neo4j.MODULE_ID2.testKey", "testValue2");
            return new MapConfiguration(config);
        };

        CommunityRuntimeConfiguration configuration = new CommunityRuntimeConfiguration(mockDb, dummyReader);

        assertTrue(configuration.runtimeEnabled());

        Map<String, DeclaredConfiguration> bootstrappers = configuration.loadConfig();

        assertEquals(2, bootstrappers.size());
        assertEquals(1, bootstrappers.get("MODULE_ID").getOrder());
        assertEquals(2, bootstrappers.get("MODULE_ID2").getOrder());
        assertEquals("MODULE_ID", bootstrappers.get("MODULE_ID").getId());
        assertEquals("com.graphaware.FirstBootstrapper", bootstrappers.get("MODULE_ID").getBootstrapper());
        assertEquals("neo4j", bootstrappers.get("MODULE_ID").getDatabase());
        assertEquals("MODULE_ID2", bootstrappers.get("MODULE_ID2").getId());
        assertEquals("com.graphaware.SecondBootstrapper", bootstrappers.get("MODULE_ID2").getBootstrapper());
        assertEquals("neo4j", bootstrappers.get("MODULE_ID2").getDatabase());

        assertEquals("testValue1", bootstrappers.get("MODULE_ID").getConfig().getString("testKey"));
        assertEquals("testValue2", bootstrappers.get("MODULE_ID2").getConfig().getString("testKey"));
    }

    @Test
    public void shouldNotReadConfigurationFromDifferentDatabase() {
        GraphDatabaseService mockDb = Mockito.mock(GraphDatabaseService.class);
        Mockito.when(mockDb.databaseName()).thenReturn("anotherDb");

        ConfigurationReader dummyReader = () -> {
            Map<String, String> config = new HashMap<>();
            config.put("com.graphaware.module.neo4j.MODULE_ID.1", "com.graphaware.FirstBootstrapper");
            config.put("com.graphaware.module.neo4j.MODULE_ID.testKey", "testValue1");
            config.put("com.graphaware.module.neo4j.MODULE_ID2.2", "com.graphaware.SecondBootstrapper");
            config.put("com.graphaware.module.neo4j.MODULE_ID2.testKey", "testValue2");
            return new MapConfiguration(config);
        };

        CommunityRuntimeConfiguration configuration = new CommunityRuntimeConfiguration(mockDb, dummyReader);

        assertFalse(configuration.runtimeEnabled());

        Map<String, DeclaredConfiguration> bootstrappers = configuration.loadConfig();

        assertEquals(0, bootstrappers.size());
    }

    @Test
    public void shouldNotReadConfigurationFromDifferentDatabase2() {
        GraphDatabaseService mockDb = Mockito.mock(GraphDatabaseService.class);
        Mockito.when(mockDb.databaseName()).thenReturn("neo4j");

        ConfigurationReader dummyReader = () -> {
            Map<String, String> config = new HashMap<>();
            config.put("com.graphaware.module.anotherDb.MODULE_ID.1", "com.graphaware.FirstBootstrapper");
            config.put("com.graphaware.module.anotherDb.MODULE_ID.testKey", "testValue1");
            config.put("com.graphaware.module.anotherDb.MODULE_ID2.2", "com.graphaware.SecondBootstrapper");
            config.put("com.graphaware.module.anotherDb.MODULE_ID2.testKey", "testValue2");
            return new MapConfiguration(config);
        };

        CommunityRuntimeConfiguration configuration = new CommunityRuntimeConfiguration(mockDb, dummyReader);

        assertTrue(configuration.runtimeEnabled());

        Map<String, DeclaredConfiguration> bootstrappers = configuration.loadConfig();

        assertEquals(0, bootstrappers.size());
    }

    @Test
    public void shouldNotReadWrongConfig() {
        GraphDatabaseService mockDb = Mockito.mock(GraphDatabaseService.class);
        Mockito.when(mockDb.databaseName()).thenReturn("neo4j");

        ConfigurationReader dummyReader = () -> {
            Map<String, String> config = new HashMap<>();
            config.put("com.graphaware.module.*.MODULE_ID.1", "com.graphaware.FirstBootstrapper");
            config.put("com.graphaware.module.*.MODULE_ID.testKey", "testValue1");
            config.put("com.graphaware.module.*.MODULE_ID2.2", "com.graphaware.SecondBootstrapper");
            config.put("com.graphaware.module.*.MODULE_ID2.testKey", "testValue2");
            return new MapConfiguration(config);
        };

        CommunityRuntimeConfiguration configuration = new CommunityRuntimeConfiguration(mockDb, dummyReader);

        assertTrue(configuration.runtimeEnabled());

        Map<String, DeclaredConfiguration> bootstrappers = configuration.loadConfig();

        assertEquals(0, bootstrappers.size());
    }
}
