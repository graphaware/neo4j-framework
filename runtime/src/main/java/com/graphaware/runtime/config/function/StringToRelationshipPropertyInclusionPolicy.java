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

package com.graphaware.runtime.config.function;

import com.graphaware.common.policy.inclusion.RelationshipPropertyInclusionPolicy;
import com.graphaware.common.policy.inclusion.all.IncludeAllRelationshipProperties;
import com.graphaware.common.policy.inclusion.composite.CompositeRelationshipPropertyInclusionPolicy;
import com.graphaware.common.policy.inclusion.none.IncludeNoRelationshipProperties;
import com.graphaware.common.policy.inclusion.spel.SpelRelationshipPropertyInclusionPolicy;
import com.graphaware.runtime.policy.all.IncludeAllBusinessRelationshipProperties;

/**
 * A {@link StringToInclusionPolicy} that converts String to {@link RelationshipPropertyInclusionPolicy}. Singleton.
 */
public final class StringToRelationshipPropertyInclusionPolicy extends StringToInclusionPolicy<RelationshipPropertyInclusionPolicy> {

    private static StringToRelationshipPropertyInclusionPolicy INSTANCE = new StringToRelationshipPropertyInclusionPolicy();

    public static StringToRelationshipPropertyInclusionPolicy getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RelationshipPropertyInclusionPolicy compositePolicy(RelationshipPropertyInclusionPolicy policy) {
        return CompositeRelationshipPropertyInclusionPolicy.of(IncludeAllBusinessRelationshipProperties.getInstance(), policy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RelationshipPropertyInclusionPolicy spelPolicy(String spel) {
        return new SpelRelationshipPropertyInclusionPolicy(spel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RelationshipPropertyInclusionPolicy all() {
        return IncludeAllRelationshipProperties.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RelationshipPropertyInclusionPolicy none() {
        return  IncludeNoRelationshipProperties.getInstance();
    }
}
