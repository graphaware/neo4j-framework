/*
 * Copyright (c) 2013-2016 GraphAware
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

package com.graphaware.lifecycle.config;

import com.graphaware.common.policy.inclusion.composite.CompositeNodeInclusionPolicy;
import com.graphaware.common.policy.inclusion.spel.SpelNodeInclusionPolicy;
import com.graphaware.common.serialize.Serializer;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigEqualityTest {

    @Test
    public void equalConfigShouldBeEqual() {
        assertTrue(LifecycleConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").equals(LifecycleConfiguration.defaultConfiguration().withNodeTtlProperty("ttl")));
        assertTrue(LifecycleConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").withNodeExpirationIndex("bla").equals(LifecycleConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").withNodeExpirationIndex("bla")));
        assertTrue(LifecycleConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").with(CompositeNodeInclusionPolicy.of(new SpelNodeInclusionPolicy("hasLabel('Test')"))).equals(LifecycleConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").with(CompositeNodeInclusionPolicy.of(new SpelNodeInclusionPolicy("hasLabel('Test')")))));

        assertTrue(LifecycleConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").with(CompositeNodeInclusionPolicy.of(new SpelNodeInclusionPolicy("hasLabel('Test')"))).equals(Serializer.fromByteArray(Serializer.toByteArray(LifecycleConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").with(CompositeNodeInclusionPolicy.of(new SpelNodeInclusionPolicy("hasLabel('Test')")))))));

        assertFalse(LifecycleConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").equals(LifecycleConfiguration.defaultConfiguration().withNodeTtlProperty("different")));
        assertFalse(LifecycleConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").equals(LifecycleConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").withRelationshipExpirationProperty("ttl")));
        assertFalse(LifecycleConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").withNodeExpirationIndex("bla").equals(LifecycleConfiguration.defaultConfiguration().withNodeTtlProperty("ttl").withRelationshipExpirationIndex("bla")));
    }
}
