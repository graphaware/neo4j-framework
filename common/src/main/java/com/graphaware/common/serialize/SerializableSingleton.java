package com.graphaware.common.serialize;

/**
 * Base-class for serializable singletons. For framework use only.
 */
public abstract class SerializableSingleton {

    protected SerializableSingleton() {
        Serializer.register(this.getClass(), new SingletonSerializer());
    }
}
