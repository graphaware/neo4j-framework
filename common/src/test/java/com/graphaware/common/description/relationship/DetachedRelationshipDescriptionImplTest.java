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

package com.graphaware.common.description.relationship;

import com.graphaware.common.UnitTest;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.literal;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.neo4j.graphdb.Direction.*;

/**
 * Test for {@link com.graphaware.common.description.relationship.DetachedRelationshipDescriptionImpl}.
 */
public class DetachedRelationshipDescriptionImplTest extends UnitTest {

    @Override
    protected void populate(Transaction database) {
        Node root = database.createNode();
        Node one = database.createNode();
        root.createRelationshipTo(one, RelationshipType.withName("TEST")).setProperty("k", new int[]{2, 3, 4});
    }

    @Test
    public void shouldCorrectlyJudgeMoreGeneral() {
        assertTrue(literal("TEST", OUTGOING).isMoreGeneralThan(literal("TEST", OUTGOING)));
        assertFalse(literal("TEST2", OUTGOING).isMoreGeneralThan(literal("TEST", OUTGOING)));
        assertTrue(literal("TEST", BOTH).isMoreGeneralThan(literal("TEST", OUTGOING)));
        assertTrue(literal("TEST", OUTGOING).isMoreGeneralThan(literal("TEST", BOTH)));
        assertFalse(literal("TEST", OUTGOING).isMoreGeneralThan(literal("TEST", INCOMING)));
        assertFalse(literal("TEST", INCOMING).isMoreGeneralThan(literal("TEST", OUTGOING)));
        assertTrue(literal("TEST", OUTGOING).with("k1", equalTo("v1")).isMoreGeneralThan(literal("TEST", OUTGOING).with("k1", equalTo("v1"))));
        assertFalse(literal("TEST2", OUTGOING).with("k1", equalTo("v1")).isMoreGeneralThan(literal("TEST", OUTGOING).with("k1", equalTo("v1"))));
    }

    @Test
    public void shouldCorrectlyJudgeMoreSpecific() {
        assertTrue(literal("TEST", OUTGOING).isMoreSpecificThan(literal("TEST", OUTGOING)));
        assertFalse(literal("TEST2", OUTGOING).isMoreSpecificThan(literal("TEST", OUTGOING)));
        assertTrue(literal("TEST", BOTH).isMoreSpecificThan(literal("TEST", OUTGOING)));
        assertTrue(literal("TEST", OUTGOING).isMoreSpecificThan(literal("TEST", BOTH)));
        assertFalse(literal("TEST", OUTGOING).isMoreSpecificThan(literal("TEST", INCOMING)));
        assertFalse(literal("TEST", INCOMING).isMoreSpecificThan(literal("TEST", OUTGOING)));
        assertTrue(literal("TEST", OUTGOING).with("k1", equalTo("v1")).isMoreSpecificThan(literal("TEST", OUTGOING).with("k1", equalTo("v1"))));
        assertFalse(literal("TEST2", OUTGOING).with("k1", equalTo("v1")).isMoreSpecificThan(literal("TEST", OUTGOING).with("k1", equalTo("v1"))));
    }

    @Test
    public void shouldCorrectlyJudgeMutex() {
        assertFalse(literal("TEST", OUTGOING).isMutuallyExclusive(literal("TEST", OUTGOING)));
        assertTrue(literal("TEST2", OUTGOING).isMutuallyExclusive(literal("TEST", OUTGOING)));
        assertFalse(literal("TEST", BOTH).isMutuallyExclusive(literal("TEST", OUTGOING)));
        assertFalse(literal("TEST", OUTGOING).isMutuallyExclusive(literal("TEST", BOTH)));
        assertTrue(literal("TEST", OUTGOING).isMutuallyExclusive(literal("TEST", INCOMING)));
        assertTrue(literal("TEST", INCOMING).isMutuallyExclusive(literal("TEST", OUTGOING)));
        assertFalse(literal("TEST", OUTGOING).with("k1", equalTo("v1")).isMutuallyExclusive(literal("TEST", OUTGOING).with("k1", equalTo("v1"))));
        assertTrue(literal("TEST2", OUTGOING).with("k1", equalTo("v1")).isMutuallyExclusive(literal("TEST", OUTGOING).with("k1", equalTo("v1"))));
    }
}
