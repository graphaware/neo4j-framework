package com.graphaware.runtime.spring;

import com.graphaware.module.changefeed.cache.CachingGraphChangeReader;
import com.graphaware.module.changefeed.io.GraphChangeReader;
import com.graphaware.runtime.ProductionRuntime;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

    @Configuration
    public class Config {

        @Bean(destroyMethod = "shutdown")
        public GraphDatabaseService graphDatabaseService() {
            GraphDatabaseService database = new GraphDatabaseFactory()
                    .newEmbeddedDatabaseBuilder("/tmp/test")
                    .loadPropertiesFromURL(
                            Config.class.getClassLoader().getResource("com/graphaware/runtime/spring/neo4j.properties"))
                    .newGraphDatabase();

            ProductionRuntime.getRuntime(database).waitUntilStarted();

            return database;
        }

        @Bean
        public GraphChangeReader graphChangeReader() {
            return new CachingGraphChangeReader(graphDatabaseService());
        }
    }
