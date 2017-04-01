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

import com.graphaware.common.policy.inclusion.RelationshipInclusionPolicy;
import com.graphaware.common.policy.inclusion.composite.CompositeRelationshipInclusionPolicy;
import com.graphaware.common.policy.inclusion.none.IncludeNoRelationships;
import com.graphaware.common.policy.inclusion.spel.SpelRelationshipInclusionPolicy;
import com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships;

/**
 * A {@link StringToInclusionPolicy} that converts String to {@link RelationshipInclusionPolicy}. Singleton.
 */
public final class StringToRelationshipInclusionPolicy extends StringToInclusionPolicy<RelationshipInclusionPolicy> {

    private static StringToRelationshipInclusionPolicy INSTANCE = new StringToRelationshipInclusionPolicy();

    public static StringToRelationshipInclusionPolicy getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RelationshipInclusionPolicy compositePolicy(RelationshipInclusionPolicy policy) {
        return CompositeRelationshipInclusionPolicy.of(IncludeAllBusinessRelationships.getInstance(), policy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RelationshipInclusionPolicy spelPolicy(String spel) {
        return new SpelRelationshipInclusionPolicy(spel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RelationshipInclusionPolicy all() {
        return IncludeAllBusinessRelationships.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RelationshipInclusionPolicy none() {
        return IncludeNoRelationships.getInstance();
    }
}
