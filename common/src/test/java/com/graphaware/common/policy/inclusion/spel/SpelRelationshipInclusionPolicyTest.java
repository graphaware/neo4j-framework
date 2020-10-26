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

import com.graphaware.common.policy.inclusion.RelationshipInclusionPolicy;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.internal.helpers.collection.Iterables;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link com.graphaware.common.policy.inclusion.spel.SpelRelationshipInclusionPolicy}.
 */
public class SpelRelationshipInclusionPolicyTest extends SpelInclusionPolicyTest {

    private RelationshipInclusionPolicy policy1 = new SpelRelationshipInclusionPolicy("isType('WORKS_FOR')");
    private RelationshipInclusionPolicy policy2 = new SpelRelationshipInclusionPolicy("getType() == 'WORKS_FOR'");
    private RelationshipInclusionPolicy policy3 = new SpelRelationshipInclusionPolicy("isType('LIVES_IN') && isIncoming()");
    private RelationshipInclusionPolicy policy4 = new SpelRelationshipInclusionPolicy("startNode.hasLabel('Employee')");
    private RelationshipInclusionPolicy policy5 = new SpelRelationshipInclusionPolicy("otherNode.hasLabel('Employee')");
    private RelationshipInclusionPolicy policy6 = new SpelRelationshipInclusionPolicy("hasProperty('until')");
    private RelationshipInclusionPolicy policy7 = new SpelRelationshipInclusionPolicy("type == 'WORKS_FOR'");

    @Test
    public void shouldIncludeCorrectRelationships() {
        try (Transaction tx = database.beginTx()) {
            assertTrue(policy1.include(michalWorksFor(tx)));
            assertTrue(policy1.include(vojtaWorksFor(tx)));
            assertFalse(policy1.include(michalLivesIn(tx)));
            assertFalse(policy1.include(vojtaLivesIn(tx)));

            assertTrue(policy2.include(michalWorksFor(tx)));
            assertTrue(policy2.include(vojtaWorksFor(tx)));
            assertFalse(policy2.include(michalLivesIn(tx)));
            assertFalse(policy2.include(vojtaLivesIn(tx)));

            assertTrue(policy7.include(michalWorksFor(tx)));
            assertTrue(policy7.include(vojtaWorksFor(tx)));
            assertFalse(policy7.include(michalLivesIn(tx)));
            assertFalse(policy7.include(vojtaLivesIn(tx)));

            assertTrue(policy3.include(michalLivesIn(tx), london(tx)));
            assertFalse(policy3.include(michalLivesIn(tx), michal(tx)));
            assertTrue(policy3.include(vojtaLivesIn(tx), london(tx)));
            assertFalse(policy3.include(vojtaLivesIn(tx), vojta(tx)));
            assertFalse(policy3.include(michalWorksFor(tx), michal(tx)));
            assertFalse(policy3.include(michalWorksFor(tx), graphaware(tx)));

            assertTrue(policy4.include(michalLivesIn(tx), michal(tx)));
            assertTrue(policy4.include(michalLivesIn(tx), london(tx)));
            assertTrue(policy4.include(michalWorksFor(tx), michal(tx)));
            assertTrue(policy4.include(michalWorksFor(tx), graphaware(tx)));
            assertFalse(policy4.include(vojtaLivesIn(tx), london(tx)));
            assertFalse(policy4.include(vojtaLivesIn(tx), vojta(tx)));

            assertTrue(policy4.include(michalLivesIn(tx)));
            assertTrue(policy4.include(michalWorksFor(tx)));
            assertFalse(policy4.include(vojtaLivesIn(tx)));

            assertFalse(policy5.include(michalLivesIn(tx), michal(tx)));
            assertTrue(policy5.include(michalLivesIn(tx), london(tx)));
            assertFalse(policy5.include(michalWorksFor(tx), michal(tx)));
            assertTrue(policy5.include(michalWorksFor(tx), graphaware(tx)));
            assertFalse(policy5.include(vojtaLivesIn(tx), london(tx)));
            assertFalse(policy5.include(vojtaLivesIn(tx), vojta(tx)));

            assertFalse(policy6.include(michalLivesIn(tx)));
            assertFalse(policy6.include(michalWorksFor(tx)));
            assertFalse(policy6.include(vojtaLivesIn(tx)));
            assertTrue(policy6.include(vojtaWorksFor(tx)));

            tx.commit();
        }
    }

    @Test
    public void shouldComplainAboutIncorrectUsage1() {
        assertThrows(Exception.class, () -> {
            try (Transaction tx = database.beginTx()) {
                policy5.include(michalLivesIn(tx), vojta(tx));
                tx.commit();
            }
        });
    }

    @Test
    public void shouldComplainAboutIncorrectUsage2() {
        assertThrows(Exception.class, () -> {
            try (Transaction tx = database.beginTx()) {
                policy3.include(michalLivesIn(tx));
                tx.commit();
            }
        });
    }

    @Test
    public void shouldComplainAboutIncorrectUsage3() {
        assertThrows(Exception.class, () -> {
            try (Transaction tx = database.beginTx()) {
                policy5.include(michalLivesIn(tx));
                tx.commit();
            }
        });
    }

    @Test
    public void shouldIncludeAllCorrectRels() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(1, Iterables.count(policy6.getAll(tx)));
            assertEquals(vojtaWorksFor(tx), policy6.getAll(tx).iterator().next());
            tx.commit();
        }
    }
}
