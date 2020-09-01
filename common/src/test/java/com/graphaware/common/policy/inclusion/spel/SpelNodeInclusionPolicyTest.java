/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.policy.inclusion.spel;

import com.graphaware.common.policy.inclusion.NodeInclusionPolicy;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.internal.helpers.collection.Iterables;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link com.graphaware.common.policy.inclusion.spel.SpelNodeInclusionPolicy}.
 */
public class SpelNodeInclusionPolicyTest extends SpelInclusionPolicyTest {

    private NodeInclusionPolicy simplePolicy1;
    private NodeInclusionPolicy simplePolicy2;

    private NodeInclusionPolicy policy1;
    private NodeInclusionPolicy policy2;
    private NodeInclusionPolicy policy3;
    private NodeInclusionPolicy policy4;
    private NodeInclusionPolicy policy5;
    private NodeInclusionPolicy policy6;
    private NodeInclusionPolicy policy7;

    @Override
    public void setUp() {
        super.setUp();

        simplePolicy1 = new SpelNodeInclusionPolicy("hasLabel('Employee')");
        simplePolicy2 = new SpelNodeInclusionPolicy("!hasLabel('Employee')");

        policy1 = new SpelNodeInclusionPolicy("hasLabel('Employee') || hasProperty('form') || getProperty('age', 0) > 20");
        policy2 = new SpelNodeInclusionPolicy("getDegree('OUTGOING') > 1");
        policy3 = new SpelNodeInclusionPolicy("getDegree('WORKS_FOR', 'BOTH') > 1");
        policy4 = new SpelNodeInclusionPolicy("getDegree('WORKS_FOR', 'incoming') > 1");
        policy5 = new SpelNodeInclusionPolicy("degree > 2");
        policy6 = new SpelNodeInclusionPolicy("hasLabel('Employee') || hasLabel('Intern')");
        policy7 = new SpelNodeInclusionPolicy("hasLabel('Intern') || hasLabel('Employee')");
    }

    @Test
    public void shouldIncludeCorrectNodes() {
        try (Transaction tx = database.beginTx()) {
            assertTrue(simplePolicy1.include(michal(tx)));
            assertFalse(simplePolicy1.include(graphaware(tx)));
            assertFalse(simplePolicy1.include(vojta(tx)));
            assertFalse(simplePolicy1.include(london(tx)));

            assertFalse(simplePolicy2.include(michal(tx)));
            assertTrue(simplePolicy2.include(graphaware(tx)));
            assertTrue(simplePolicy2.include(vojta(tx)));
            assertTrue(simplePolicy2.include(london(tx)));

            assertTrue(policy1.include(michal(tx)));
            assertTrue(policy1.include(graphaware(tx)));
            assertTrue(policy1.include(vojta(tx)));
            assertFalse(policy1.include(london(tx)));

            assertTrue(policy2.include(michal(tx)));
            assertFalse(policy2.include(graphaware(tx)));
            assertTrue(policy2.include(vojta(tx)));
            assertFalse(policy2.include(london(tx)));

            assertFalse(policy3.include(michal(tx)));
            assertTrue(policy3.include(graphaware(tx)));
            assertFalse(policy3.include(vojta(tx)));
            assertFalse(policy3.include(london(tx)));

            assertFalse(policy4.include(michal(tx)));
            assertTrue(policy4.include(graphaware(tx)));
            assertFalse(policy4.include(vojta(tx)));
            assertFalse(policy4.include(london(tx)));

            assertFalse(policy5.include(michal(tx)));
            assertFalse(policy5.include(graphaware(tx)));
            assertFalse(policy5.include(vojta(tx)));
            assertFalse(policy5.include(london(tx)));

            tx.commit();
        }
    }

    @Test
    public void shouldCorrectlyGetAllNodes() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(1, Iterables.count(simplePolicy1.getAll(tx)));
            assertEquals(michal(tx), simplePolicy1.getAll(tx).iterator().next());

            assertEquals(3, Iterables.count(simplePolicy2.getAll(tx)));

            assertEquals(3, Iterables.count(policy1.getAll(tx)));

            assertEquals(2, Iterables.count(policy2.getAll(tx)));

            assertEquals(1, Iterables.count(policy3.getAll(tx)));
            assertEquals(graphaware(tx), policy3.getAll(tx).iterator().next());

            assertEquals(1, Iterables.count(policy4.getAll(tx)));
            assertEquals(graphaware(tx), policy4.getAll(tx).iterator().next());

            assertEquals(0, Iterables.count(policy5.getAll(tx)));

            assertEquals(2, Iterables.count(policy6.getAll(tx)));
            assertEquals(2, Iterables.count(policy7.getAll(tx)));

            tx.commit();
        }
    }
}
