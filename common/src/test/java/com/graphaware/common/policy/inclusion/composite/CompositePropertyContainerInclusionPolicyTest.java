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

import com.graphaware.common.policy.inclusion.NodeInclusionPolicy;
import com.graphaware.common.policy.inclusion.RelationshipInclusionPolicy;
import com.graphaware.common.policy.inclusion.all.IncludeAllNodes;
import com.graphaware.common.policy.inclusion.all.IncludeAllRelationships;
import com.graphaware.common.policy.inclusion.none.IncludeNoNodes;
import com.graphaware.common.policy.inclusion.none.IncludeNoRelationships;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *  Unit test for {@link CompositePropertyContainerInclusionPolicy}.
 */
public class CompositePropertyContainerInclusionPolicyTest {

    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructEmptyCompositePolicy() {
        CompositeNodeInclusionPolicy.of((NodeInclusionPolicy[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructEmptyCompositePolicy2() {
        CompositeNodeInclusionPolicy.of();
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotConstructEmptyCompositePolicy3() {
        CompositeRelationshipInclusionPolicy.of((RelationshipInclusionPolicy[]) new RelationshipInclusionPolicy[0]);
    }

    @Test
    public void whenAllVoteYesThenTrueIsReturned() {
        assertTrue(CompositeNodeInclusionPolicy.of(IncludeAllNodes.getInstance(), IncludeAllNodes.getInstance()).include(null));
        assertTrue(CompositeRelationshipInclusionPolicy.of(IncludeAllRelationships.getInstance(), IncludeAllRelationships.getInstance()).include(null));
    }

    @Test
    public void whenOneVotesNoThenFalseIsReturned() {
        assertFalse(CompositeNodeInclusionPolicy.of(IncludeNoNodes.getInstance(), IncludeAllNodes.getInstance()).include(null));
        assertFalse(CompositeRelationshipInclusionPolicy.of(IncludeAllRelationships.getInstance(), IncludeNoRelationships.getInstance()).include(null));
    }
}
