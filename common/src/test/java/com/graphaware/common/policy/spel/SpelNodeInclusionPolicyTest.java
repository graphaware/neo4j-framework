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
import org.neo4j.helpers.collection.Iterables;

import static org.junit.Assert.*;

/**
 * Unit test for {@link com.graphaware.common.policy.spel.SpelNodeInclusionPolicy}.
 */
public class SpelNodeInclusionPolicyTest extends SpelInclusionPolicyTest {

    private NodeInclusionPolicy policy1;
    private NodeInclusionPolicy policy2;
    private NodeInclusionPolicy policy3;
    private NodeInclusionPolicy policy4;
    private NodeInclusionPolicy policy5;

    @Override
    public void setUp() {
        super.setUp();

        policy1 = new SpelNodeInclusionPolicy("hasLabel('Employee') || hasProperty('form') || getProperty('age', 0) > 20");
        policy2 = new SpelNodeInclusionPolicy("getDegree('OUTGOING') > 1");
        policy3 = new SpelNodeInclusionPolicy("getDegree('WORKS_FOR', 'BOTH') > 1");
        policy4 = new SpelNodeInclusionPolicy("getDegree('WORKS_FOR', 'incoming') > 1");
        policy5 = new SpelNodeInclusionPolicy("degree > 2");
    }

    @Test
    public void shouldIncludeCorrectNodes() {
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

    @Test
    public void shouldCorrectlyGetAllNodes() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(1, Iterables.count(policy3.getAll(database)));
            assertEquals(graphaware(), policy3.getAll(database).iterator().next());
            tx.success();
        }
    }
}
