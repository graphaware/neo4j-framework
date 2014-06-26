package com.graphaware.runtime.config;

import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.graphaware.common.serialize.SerializableSingleton;
import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;
import com.graphaware.common.strategy.InclusionStrategies;

/**
 * {@link TxDrivenModuleConfiguration} for {@link com.graphaware.runtime.module.TxDrivenModule}s with
 * no configuration. Singleton.
 */
public final class NullTxDrivenModuleConfiguration implements TxDrivenModuleConfiguration {

    static {
        Serializer.register(NullTxDrivenModuleConfiguration.class, new SingletonSerializer());
    }

    private static final TxDrivenModuleConfiguration INSTANCE = new NullTxDrivenModuleConfiguration();

    public static TxDrivenModuleConfiguration getInstance() {
        return INSTANCE;
    }

    private NullTxDrivenModuleConfiguration() {
    }

    @Override
    public InclusionStrategies getInclusionStrategies() {
        return InclusionStrategies.all();
    }
}
