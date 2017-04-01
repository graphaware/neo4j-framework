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

package com.graphaware.api.transform;

import org.neo4j.graphdb.PropertyContainer;

/**
 * A transformer of a {@link PropertyContainer}.
 *
 * @param <R> type of the object this transformer transforms to.
 * @param <P> type of the property container this transformer transforms from.
 */
public interface Transformer<R, P extends PropertyContainer> {

    /**
     * Transforms the given property container.
     *
     * @param p to transform. Must not be <code>null</code>.
     * @return transformed container. Never <code>null</code>.
     */
    R transform(P p);
}
