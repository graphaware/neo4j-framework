/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.description.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.graphaware.common.description.predicate.Predicates;
import com.graphaware.common.description.property.LiteralPropertiesDescription;
import com.graphaware.common.description.relationship.DetachedRelationshipDescriptionImpl;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Utility class for serializing objects to/from String or byte array using {@link Kryo}. For framework use only.
 * <p/>
 * Note: every class that wishes to be serialized must be registered with Kryo. It is the responsibility of the class
 * developer to test that serialization works ok for that class.
 */
public final class Serializer {

    private static Kryo kryo;

    static {
        kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());

        kryo.register(DynamicRelationshipType.class, 10);
        kryo.register(Direction.class, 11);
        kryo.register(HashMap.class, 15);
        kryo.register(TreeMap.class, 16);

        Predicates.register(kryo); //allocated 20-30

        kryo.register(LiteralPropertiesDescription.class, 31);

        kryo.register(DetachedRelationshipDescriptionImpl.class, 41);

        kryo.register(byte[].class, 100);
        kryo.register(char[].class, 101);
        kryo.register(boolean[].class, 102);
        kryo.register(long[].class, 103);
        kryo.register(double[].class, 104);
        kryo.register(int[].class, 105);
        kryo.register(short[].class, 106);
        kryo.register(float[].class, 107);
        kryo.register(String[].class, 108);

        //todo do we need to register anything else?
    }

    private Serializer() {
    }

    public static void register(Class type) {
        kryo.register(type);
    }

    /**
     * Serialize an object to byte array.
     *
     * @param object to serialize.
     * @return byte array.
     */
    public static byte[] toByteArray(Object object) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output output = new Output(stream);
        kryo.writeObject(output, object);
        output.flush();
        output.close();

        return stream.toByteArray();
    }

    /**
     * Serialize an object to String.
     *
     * @param object to serialize.
     * @return object as String.
     */
    public static String toString(Object object, String prefix) {
        return prefix + Base64.encode(toByteArray(object));
    }

    /**
     * Read an object from byte array.
     *
     * @param array to read from.
     * @param clazz to return.
     * @return de-serialized object.
     */
    public static <T> T fromByteArray(byte[] array, Class<T> clazz) {
        return kryo.readObject(new Input(array), clazz);
    }

    /**
     * Read an object from String.
     *
     * @param string to read from.
     * @param clazz  to return.
     * @return de-serialized object.
     */
    public static <T> T fromString(String string, Class<T> clazz, String prefix) {
        return fromByteArray(Base64.decode(string.substring(prefix.length())), clazz);
    }

}
