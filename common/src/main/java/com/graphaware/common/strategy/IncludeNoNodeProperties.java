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

package com.graphaware.common.strategy;


import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;
import org.neo4j.graphdb.Node;

/**
 * Strategy that ignores all node properties. Singleton.
 */
public final class IncludeNoNodeProperties extends IncludeNoProperties<Node> implements NodePropertyInclusionStrategy {

    private static final NodePropertyInclusionStrategy INSTANCE = new IncludeNoNodeProperties();

    static {
        Serializer.register(IncludeNoNodeProperties.class, new SingletonSerializer());
    }

    public static NodePropertyInclusionStrategy getInstance() {
        return INSTANCE;
    }

    private IncludeNoNodeProperties() {
    }
}
