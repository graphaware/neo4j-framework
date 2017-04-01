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

import com.graphaware.common.policy.inclusion.NodeInclusionPolicy;
import com.graphaware.common.policy.inclusion.none.IncludeNoNodes;
import com.graphaware.common.policy.inclusion.spel.SpelNodeInclusionPolicy;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;
import org.junit.Test;

import static com.graphaware.common.policy.inclusion.composite.CompositeNodeInclusionPolicy.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test for {@link StringToNodeInclusionPolicy}.
 */
public class StringToNodeInclusionPolicyTest {


    @Test
    public void shouldConstructPolicyFromAllNodesKeywords() {
        NodeInclusionPolicy policy1 = StringToNodeInclusionPolicy.getInstance().apply("all");

        assertNotNull(policy1);
        assertEquals(IncludeAllBusinessNodes.getInstance(), policy1);

        NodeInclusionPolicy policy2 = StringToNodeInclusionPolicy.getInstance().apply("true");

        assertNotNull(policy2);
        assertEquals(IncludeAllBusinessNodes.getInstance(), policy2);
    }

    @Test
    public void shouldConstructPolicyFromExcludeAllNodesKeywords() {
        NodeInclusionPolicy policy1 = StringToNodeInclusionPolicy.getInstance().apply("false");

        assertNotNull(policy1);
        assertEquals(IncludeNoNodes.getInstance(), policy1);

        NodeInclusionPolicy policy2 = StringToNodeInclusionPolicy.getInstance().apply("none");

        assertNotNull(policy2);
        assertEquals(IncludeNoNodes.getInstance(), policy2);
    }

    @Test
    public void shouldConstructPolicyFromClassName() {
        NodeInclusionPolicy policy = StringToNodeInclusionPolicy.getInstance().apply("com.graphaware.runtime.config.function.SingletonNodeInclusionPolicy");

        assertNotNull(policy);
        assertEquals(of(IncludeAllBusinessNodes.getInstance(), SingletonNodeInclusionPolicy.getInstance()), policy);
    }

    @Test
    public void shouldConstructPolicyFromClassName2() {
        NodeInclusionPolicy policy = StringToNodeInclusionPolicy.getInstance().apply("com.graphaware.runtime.config.function.TestNodeInclusionPolicy");

        assertNotNull(policy);
        assertEquals(of(IncludeAllBusinessNodes.getInstance(), new TestNodeInclusionPolicy()), policy);
    }

    @Test
    public void shouldConstructSpelPolicy() {
        NodeInclusionPolicy policy = StringToNodeInclusionPolicy.getInstance().apply("hasLabel('Test')");

        assertEquals(of(IncludeAllBusinessNodes.getInstance(), new SpelNodeInclusionPolicy("hasLabel('Test')")), policy);
    }

    @Test
    public void shouldConstructSpelPolicy2() {
        NodeInclusionPolicy policy = StringToNodeInclusionPolicy.getInstance().apply("isType('R1')");

        assertEquals(of(IncludeAllBusinessNodes.getInstance(), new SpelNodeInclusionPolicy("isType('R1')")), policy);
    }

    @Test(expected = RuntimeException.class)
    public void uninstantiableClassCausesException() {
        StringToNodeInclusionPolicy.getInstance().apply("com.graphaware.runtime.config.function.WrongNodeInclusionPolicy");
    }
}
