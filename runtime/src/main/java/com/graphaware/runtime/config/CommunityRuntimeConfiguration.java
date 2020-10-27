package com.graphaware.runtime.config;

import org.apache.commons.configuration2.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommunityRuntimeConfiguration implements RuntimeConfiguration {

    private static final String NEO4J = "neo4j";
    private static final String MODULE_CONFIG_KEY = "com.graphaware.module"; //db.ID.Order = fully qualified class name of bootstrapper
    private static final Pattern MODULE_ENABLED_KEY = Pattern.compile("(\\S*[^. ]{1,})\\.(\\S[^. ]{1,})\\.([0-9]{1,})");

    protected final GraphDatabaseService database;
    protected final Configuration runtimeConfiguration;

    public CommunityRuntimeConfiguration(GraphDatabaseService database, ConfigurationReader configurationReader) {
        this.database = database;
        this.runtimeConfiguration = configurationReader.readConfiguration();
    }

    @Override
    public boolean runtimeEnabled() {
        return NEO4J.equals(database.databaseName());
    }

    @Override
    public Map<String, DeclaredConfiguration> loadConfig() {
        if (!runtimeEnabled()) {
            return Collections.emptyMap();
        }

        Map<String, DeclaredConfiguration> orderedBootstrappers = new HashMap<>();

        Configuration subset = runtimeConfiguration.subset(MODULE_CONFIG_KEY);

        subset.getKeys().forEachRemaining(s -> {
            Matcher matcher = MODULE_ENABLED_KEY.matcher(s);
            if (matcher.find()) {
                String dbName = matcher.group(1);
                if (dbNameMatches(dbName)) {
                    String moduleId = matcher.group(2);
                    int moduleOrder = Integer.parseInt(matcher.group(3));
                    String bootstrapperClass = subset.getString(s, "UNKNOWN");
                    orderedBootstrappers.put(moduleId, new DeclaredConfiguration(moduleOrder, moduleId, bootstrapperClass, database.databaseName(), subset.subset(dbName).subset(moduleId)));
                }
            }
        });

        return orderedBootstrappers;
    }

    protected boolean dbNameMatches(String confDbName) {
        return confDbName.equals(database.databaseName());
    }
}
