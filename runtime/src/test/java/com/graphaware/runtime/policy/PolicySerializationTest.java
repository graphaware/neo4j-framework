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

import com.graphaware.common.serialize.Serializer;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodeProperties;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;
import com.graphaware.runtime.policy.all.IncludeAllBusinessRelationshipProperties;
import com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PolicySerializationTest {

    @Test
    public void singletonPoliciesShouldBeEqual() {
        assertEquals(IncludeAllBusinessNodes.getInstance(), serDeser(IncludeAllBusinessNodes.getInstance()));
        assertEquals(IncludeAllBusinessRelationships.getInstance(), serDeser(IncludeAllBusinessRelationships.getInstance()));
        assertEquals(IncludeAllBusinessNodeProperties.getInstance(), serDeser(IncludeAllBusinessNodeProperties.getInstance()));
        assertEquals(IncludeAllBusinessRelationshipProperties.getInstance(), serDeser(IncludeAllBusinessRelationshipProperties.getInstance()));
    }

    private <T> T serDeser(T t) {
        return Serializer.fromByteArray(Serializer.toByteArray(t));
    }
}
