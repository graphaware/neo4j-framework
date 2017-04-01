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

package com.graphaware.runtime.policy;

import com.graphaware.common.policy.inclusion.InclusionPolicies;
import com.graphaware.common.policy.inclusion.all.IncludeAllNodeProperties;
import com.graphaware.common.policy.inclusion.none.IncludeNoNodeProperties;
import com.graphaware.common.serialize.Serializer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Unit test for {@link InclusionPolicies}.
 */
public class InclusionPoliciesImplTest {

    @Test
    public void samePoliciesShouldSerializeToSameString() {
        assertEquals(Serializer.toString(InclusionPolicies.all(), "test"), Serializer.toString(InclusionPolicies.all(), "test"));
        assertEquals(Serializer.toString(InclusionPolicies.all().with(IncludeAllNodeProperties.getInstance()), "test"), Serializer.toString(InclusionPolicies.all(), "test"));
    }

    @Test
    public void differentPoliciesShouldReturnDifferentString() {
        assertNotSame(Serializer.toString(InclusionPolicies.all(), "test"), Serializer.toString(InclusionPolicies.none(), "test"));
        assertNotSame(Serializer.toString(InclusionPolicies.all(), "test"), Serializer.toString(InclusionPolicies.all().with(IncludeNoNodeProperties.getInstance()), "test"));
    }
}
