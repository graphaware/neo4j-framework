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

package com.graphaware.common.transform;

import com.graphaware.common.util.PropertyContainerUtils;
import org.neo4j.graphdb.PropertyContainer;

import static org.springframework.util.Assert.notNull;

/**
 * Abstract base-class for trivial {@link IdTransformer} implementations that in fact perform no transformation.
 */
public abstract class BaseTrivialIdTransformer<P extends PropertyContainer> extends BaseIdTransformer<Long, P> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected long toExistingGraphId(Long id) {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Long fromContainer(P pc) {
        notNull(pc);
        return PropertyContainerUtils.id(pc);
    }
}
