package com.graphaware.common.serialize;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.lang.reflect.InvocationTargetException;

/**
 *
 */
public class SingletonSerializer extends com.esotericsoftware.kryo.Serializer {

    @Override
    public void write(Kryo kryo, Output output, Object object) {

    }

    @Override
    public Object read(Kryo kryo, Input input, Class type) {
        try {
            return type.getDeclaredMethod("getInstance").invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
