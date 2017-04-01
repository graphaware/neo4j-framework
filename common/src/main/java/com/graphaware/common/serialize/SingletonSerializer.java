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

/**
 * {@link com.esotericsoftware.kryo.Serializer} for singletons. Singletons must declare a
 * <code>public static X getInstance</code> method, where <code>X</code> is the singleton class.
 *
 * @see com.graphaware.common.policy.inclusion.none.IncludeNoNodes, for example.
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
