/*
 * Copyright (c) 2013-2018 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.policy.inclusion.composite;

import com.graphaware.common.policy.inclusion.BaseEntityInclusionPolicy;
import com.graphaware.common.policy.inclusion.EntityInclusionPolicy;
import org.neo4j.graphdb.Entity;

import java.util.Arrays;

/**
 * {@link EntityInclusionPolicy} composed of multiple other policies.
 * All contained policies must "vote" <code>true</code> to {@link #include(org.neo4j.graphdb.Entity)} in
 * order for this policy to return <code>true</code>.
 */
public abstract class CompositeEntityInclusionPolicy<E extends Entity, T extends EntityInclusionPolicy<E>> extends BaseEntityInclusionPolicy<E> implements EntityInclusionPolicy<E> {

    protected final T[] policies;

    protected CompositeEntityInclusionPolicy(T[] policies) {
        if (policies == null || policies.length < 1) {
            throw new IllegalArgumentException("There must be at least one wrapped policy in composite policy");
        }
        this.policies = policies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(E object) {
        for (T policy : policies) {
            if (!policy.include(object)) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositeEntityInclusionPolicy that = (CompositeEntityInclusionPolicy) o;

        if (!Arrays.equals(policies, that.policies)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(policies);
    }
}
