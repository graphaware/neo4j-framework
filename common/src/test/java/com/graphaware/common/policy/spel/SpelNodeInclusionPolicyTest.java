/*
 * Copyright (c) 2015 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.policy.spel;

import com.graphaware.common.policy.NodeInclusionPolicy;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link com.graphaware.common.policy.spel.SpelNodeInclusionPolicy}.
 */
public class SpelNodeInclusionPolicyTest extends SpelInclusionPolicyTest {

    @Test
    public void shouldIncludeCorrectNodes() {
        NodeInclusionPolicy policy1 = new SpelNodeInclusionPolicy("hasLabel('Employee') || hasProperty('form') || getProperty('age', 0) > 20");
        NodeInclusionPolicy policy2 = new SpelNodeInclusionPolicy("getDegree('OUTGOING') > 1");
        NodeInclusionPolicy policy3 = new SpelNodeInclusionPolicy("getDegree('WORKS_FOR', 'BOTH') > 1");
        NodeInclusionPolicy policy4 = new SpelNodeInclusionPolicy("getDegree('WORKS_FOR', 'incoming') > 1");
        NodeInclusionPolicy policy5 = new SpelNodeInclusionPolicy("degree > 2");

        try (Transaction tx = database.beginTx()) {
            assertTrue(policy1.include(michal()));
            assertTrue(policy1.include(graphaware()));
            assertTrue(policy1.include(vojta()));
            assertFalse(policy1.include(london()));

            assertTrue(policy2.include(michal()));
            assertFalse(policy2.include(graphaware()));
            assertTrue(policy2.include(vojta()));
            assertFalse(policy2.include(london()));

            assertFalse(policy3.include(michal()));
            assertTrue(policy3.include(graphaware()));
            assertFalse(policy3.include(vojta()));
            assertFalse(policy3.include(london()));

            assertFalse(policy4.include(michal()));
            assertTrue(policy4.include(graphaware()));
            assertFalse(policy4.include(vojta()));
            assertFalse(policy4.include(london()));

            assertFalse(policy5.include(michal()));
            assertFalse(policy5.include(graphaware()));
            assertFalse(policy5.include(vojta()));
            assertFalse(policy5.include(london()));

            tx.success();
        }
    }
}
