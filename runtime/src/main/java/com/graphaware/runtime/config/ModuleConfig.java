package com.graphaware.runtime.config;

import org.apache.commons.configuration2.Configuration;

public class ModuleConfig implements Comparable<ModuleConfig> {

    private final int order;
    private final String id;
    private final String bootstrapper;
    private final String database;
    private final Configuration config;

    public ModuleConfig(int order, String id, String bootstrapper, String database, Configuration config) {
        this.order = order;
        this.id = id;
        this.bootstrapper = bootstrapper;
        this.database = database;
        this.config = config;
    }

    public int getOrder() {
        return order;
    }

    public String getId() {
        return id;
    }

    public String getBootstrapper() {
        return bootstrapper;
    }

    public String getDatabase() {
        return database;
    }

    public Configuration getConfig() {
        return config;
    }

    @Override
    public int compareTo(ModuleConfig o) {
        return Integer.compare(this.order, o.order);
    }
}
