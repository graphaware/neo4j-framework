package com.graphaware.runtime.spring;

import com.graphaware.module.changefeed.cache.CachingGraphChangeReader;
import com.graphaware.module.changefeed.io.GraphChangeReader;
import com.graphaware.runtime.RuntimeRegistry;
import org.junit.rules.TemporaryFolder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;

@Configuration
public class Config {
    @Bean(destroyMethod = "shutdown")
    public GraphDatabaseService graphDatabaseService(TemporaryFolder temporaryFolder) {
        GraphDatabaseService database = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(temporaryFolder.getRoot().getAbsolutePath())
                .loadPropertiesFromURL(
                        Config.class.getClassLoader().getResource("com/graphaware/runtime/spring/neo4j.properties"))
                .newGraphDatabase();
        registerShutdownHook(database);

        RuntimeRegistry.getRuntime(database).waitUntilStarted();

        return database;
    }

    @Bean
    public GraphChangeReader graphChangeReader() throws IOException {
        return new CachingGraphChangeReader(graphDatabaseService(temporaryFolder()));
    }

    @Bean(destroyMethod = "delete")
    public TemporaryFolder temporaryFolder() throws IOException {
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        return temporaryFolder;
    }
}
