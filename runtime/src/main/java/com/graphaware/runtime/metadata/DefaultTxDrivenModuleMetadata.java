package com.graphaware.runtime.metadata;

import com.graphaware.runtime.config.TxDrivenModuleConfiguration;

/**
 * Default production implementation of {@link TxDrivenModuleMetadata}.
 */
public class DefaultTxDrivenModuleMetadata implements TxDrivenModuleMetadata {

    private final TxDrivenModuleConfiguration configuration;
    private final boolean needsInitialization;
    private final long timestamp;

    /**
     * Construct new metadata. {@link #needsInitialization} will return <code>false</code>.
     *
     * @param configuration module configuration held by the metadata.
     */
    public DefaultTxDrivenModuleMetadata(TxDrivenModuleConfiguration configuration) {
        this(configuration, false, -1);
    }

    private DefaultTxDrivenModuleMetadata(TxDrivenModuleConfiguration configuration, boolean needsInitialization, long timestamp) {
        this.configuration = configuration;
        this.needsInitialization = needsInitialization;
        this.timestamp = timestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxDrivenModuleConfiguration getConfig() {
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    public boolean needsInitialization() {
        return needsInitialization;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long problemTimestamp() {
        return timestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultTxDrivenModuleMetadata markedNeedingInitialization() {
        if (needsInitialization) {
            return this;
        }

        return new DefaultTxDrivenModuleMetadata(configuration, true, System.currentTimeMillis());
    }
}
