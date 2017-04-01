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

package com.graphaware.common.policy.inclusion.composite;

import com.graphaware.common.policy.inclusion.NodePropertyInclusionPolicy;
import com.graphaware.common.policy.inclusion.RelationshipPropertyInclusionPolicy;
import com.graphaware.common.policy.inclusion.all.IncludeAllNodeProperties;
import com.graphaware.common.policy.inclusion.all.IncludeAllRelationshipProperties;
import com.graphaware.common.policy.inclusion.none.IncludeNoNodeProperties;
import com.graphaware.common.policy.inclusion.none.IncludeNoRelationshipProperties;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *  Unit test for {@link CompositePropertyInclusionPolicy}.
 */
public class CompositePropertyInclusionPolicyTest {

    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructEmptyCompositePolicy() {
        CompositeNodePropertyInclusionPolicy.of((NodePropertyInclusionPolicy) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructEmptyCompositePolicy2() {
        CompositeNodePropertyInclusionPolicy.of();
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructEmptyCompositePolicy3() {
        CompositeRelationshipPropertyInclusionPolicy.of((RelationshipPropertyInclusionPolicy[]) new RelationshipPropertyInclusionPolicy[0]);
    }

    @Test
    public void whenAllVoteYesThenTrueIsReturned() {
        assertTrue(CompositeNodePropertyInclusionPolicy.of(IncludeAllNodeProperties.getInstance(), IncludeAllNodeProperties.getInstance()).include("test", null));
        assertTrue(CompositeRelationshipPropertyInclusionPolicy.of(IncludeAllRelationshipProperties.getInstance(), IncludeAllRelationshipProperties.getInstance()).include("test", null));
    }

    @Test
    public void whenOneVotesNoThenFalseIsReturned() {
        assertFalse(CompositeNodePropertyInclusionPolicy.of(IncludeNoNodeProperties.getInstance(), IncludeAllNodeProperties.getInstance()).include("test", null));
        assertFalse(CompositeRelationshipPropertyInclusionPolicy.of(IncludeAllRelationshipProperties.getInstance(), IncludeNoRelationshipProperties.getInstance()).include("test", null));
    }
}
