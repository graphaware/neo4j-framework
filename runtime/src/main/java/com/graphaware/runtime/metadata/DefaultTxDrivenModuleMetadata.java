package com.graphaware.runtime.metadata;

import com.graphaware.runtime.config.TxDrivenModuleConfiguration;

/**
 *
 */
public class DefaultTxDrivenModuleMetadata implements TxDrivenModuleMetadata {

    private final TxDrivenModuleConfiguration configuration;
    private final boolean needsInitialization;
    private final long timestamp;

    public DefaultTxDrivenModuleMetadata(TxDrivenModuleConfiguration configuration) {
        this(configuration, false, -1);
    }

    private DefaultTxDrivenModuleMetadata(TxDrivenModuleConfiguration configuration, boolean needsInitialization, long timestamp) {
        this.configuration = configuration;
        this.needsInitialization = needsInitialization;
        this.timestamp = timestamp;
    }

    public TxDrivenModuleConfiguration getConfig() {
        return configuration;
    }

    public boolean needsInitialization() {
        return needsInitialization;
    }

    @Override
    public long timestamp() {
        return timestamp;
    }

    @Override
    public DefaultTxDrivenModuleMetadata markedNeedingInitialization() {
        if (needsInitialization) {
            return this;
        }

        return new DefaultTxDrivenModuleMetadata(configuration, true, System.currentTimeMillis());
    }
}
