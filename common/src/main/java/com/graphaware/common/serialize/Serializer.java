/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.graphaware.common.description.predicate.Predicates;
import com.graphaware.common.description.property.LiteralPropertiesDescription;
import com.graphaware.common.description.relationship.DetachedRelationshipDescriptionImpl;
import com.graphaware.common.policy.inclusion.all.IncludeAllNodeProperties;
import com.graphaware.common.policy.inclusion.all.IncludeAllNodes;
import com.graphaware.common.policy.inclusion.all.IncludeAllRelationshipProperties;
import com.graphaware.common.policy.inclusion.all.IncludeAllRelationships;
import com.graphaware.common.policy.inclusion.none.IncludeNoNodeProperties;
import com.graphaware.common.policy.inclusion.none.IncludeNoNodes;
import com.graphaware.common.policy.inclusion.none.IncludeNoRelationshipProperties;
import com.graphaware.common.policy.inclusion.none.IncludeNoRelationships;
import com.graphaware.common.policy.role.AnyRole;
import com.graphaware.common.policy.role.MasterOnly;
import com.graphaware.common.policy.role.SlavesOnly;
import com.graphaware.common.policy.role.WritableRole;
import org.apache.commons.codec.binary.Base64;
import org.neo4j.graphdb.Direction;
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

        register(IncludeAllNodeProperties.class, new SingletonSerializer());
        register(IncludeAllNodes.class, new SingletonSerializer());
        register(IncludeAllRelationshipProperties.class, new SingletonSerializer());
        register(IncludeAllRelationships.class, new SingletonSerializer());
        register(IncludeNoNodeProperties.class, new SingletonSerializer());
        register(IncludeNoNodes.class, new SingletonSerializer());
        register(IncludeNoRelationshipProperties.class, new SingletonSerializer());
        register(IncludeNoRelationships.class, new SingletonSerializer());

        register(AnyRole.class, new SingletonSerializer());
        register(MasterOnly.class, new SingletonSerializer());
        register(SlavesOnly.class, new SingletonSerializer());
        register(WritableRole.class, new SingletonSerializer());
    }

    private Serializer() {
    }

    public synchronized static void register(Class type) {
        kryo.register(type);
    }

    public synchronized static void register(Class type, com.esotericsoftware.kryo.Serializer serializer) {
        kryo.register(type, serializer);
    }

    public synchronized static void register(Class type, com.esotericsoftware.kryo.Serializer serializer, int id) {
        kryo.register(type, serializer, id);
    }

    /**
     * Serialize an object to byte array.
     *
     * @param object to serialize.
     * @return byte array.
     */
    public synchronized static byte[] toByteArray(Object object) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output output = new Output(stream);
        kryo.writeClassAndObject(output, object);
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
    public synchronized static String toString(Object object, String prefix) {
        return prefix + new String(Base64.encodeBase64(toByteArray(object)));
    }

    /**
     * Read an object from byte array.
     *
     * @param array to read from.
     * @return de-serialized object.
     */
    public synchronized static <T> T fromByteArray(byte[] array) {
        return (T) kryo.readClassAndObject(new Input(array));
    }

    /**
     * Read an object from String.
     *
     * @param string to read from.
     * @return de-serialized object.
     */
    public synchronized static <T> T fromString(String string, String prefix) {
        return fromByteArray(Base64.decodeBase64(string.substring(prefix.length())));
    }
}
