package com.graphaware.common.serialize;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.lang.reflect.InvocationTargetException;

/**
 *  {@link com.esotericsoftware.kryo.Serializer} for singletons. Singletons must declare a
 *  <code>public static X getInstance</code> method, where <code>X</code> is the singleton class.
 *
 *  @see {@link com.graphaware.common.strategy.IncludeNoNodes}, for example.
 */
public class SingletonSerializer extends com.esotericsoftware.kryo.Serializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(Kryo kryo, Output output, Object object) {
        //no need to write anything - class name (written by Kryo) is sufficient
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
