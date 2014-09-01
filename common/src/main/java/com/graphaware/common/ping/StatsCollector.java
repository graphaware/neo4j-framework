package com.graphaware.common.ping;

/**
 * A simple component collecting anonymous statistics about the framework usage.
 */
public interface StatsCollector {

    public static final String VERSION = "2.1.3.15";

    /**
     * Report framework start (server mode).
     *
     * @param edition of the framework.
     */
    void frameworkStart(String edition);

    /**
     * Report runtime start (server or embedded mode).
     */
    void runtimeStart();

    /**
     * Report a module start.
     *
     * @param moduleClassName fully qualified name of the module.
     */
    void moduleStart(String moduleClassName);
}
