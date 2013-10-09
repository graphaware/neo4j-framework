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

package com.graphaware.description.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.graphaware.description.predicate.Predicates;
import com.graphaware.description.property.LiteralPropertiesDescription;
import com.graphaware.description.relationship.DetachedRelationshipDescriptionImpl;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

/**
 *
 */
public class Serializer {

    private static Kryo kryo;

    static {
        kryo = new Kryo();
        kryo.setRegistrationRequired(true);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());

        kryo.register(DynamicRelationshipType.class, 10);
        kryo.register(Direction.class, 11);
        kryo.register(HashMap.class, 15);

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
    }

    public static String serialize(Object object, String prefix) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Output output = new Output(stream);
        kryo.writeObject(output, object);
        output.flush();
        output.close();

        return Base64.encode(stream.toByteArray()) + prefix;
    }

    public static <T> T deserialize(String string, Class<T> clazz, String prefix) {
        return (T) kryo.readObject(new Input(Base64.decode(string.replaceFirst(prefix, ""))), clazz);
    }

}
