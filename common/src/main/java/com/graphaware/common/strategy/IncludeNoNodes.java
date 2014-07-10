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
 * Strategy that ignores all nodes. Singleton.
 */
public final class IncludeNoNodes extends IncludeNone<Node> implements NodeInclusionStrategy {

    private static final NodeInclusionStrategy INSTANCE = new IncludeNoNodes();

    static {
        Serializer.register(IncludeNoNodes.class, new SingletonSerializer());
    }

    public static NodeInclusionStrategy getInstance() {
        return INSTANCE;
    }

    private IncludeNoNodes() {

    }
}
