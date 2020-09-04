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

package com.graphaware.common.policy;

import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import com.graphaware.common.policy.inclusion.fluent.IncludeNodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.predicate.Predicates.undefined;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.neo4j.graphdb.Label.label;

/**
 * Unit test for  {@link com.graphaware.common.policy.inclusion.fluent.IncludeNodes}.
 */
@TestInstance(PER_CLASS)
@ExtendWith(Neo4jExtension.class)
public class IncludeNodesTest {

    @InjectNeo4j(lifecycle = InjectNeo4j.Lifecycle.CLASS)
    private GraphDatabaseService database;

    @Test
    public void shouldIncludeCorrectRelationships() {
        try (Transaction tx = database.beginTx()) {
            Node n = tx.createNode(label("Test"));
            n.setProperty("test", "test");

            assertTrue(IncludeNodes.all().include(n));
            assertTrue(IncludeNodes.all().with("Test").include(n));
            assertFalse(IncludeNodes.all().with(label("Test2")).include(n));
            assertTrue(IncludeNodes.all().with(label("Bla")).with((Label) null).include(n));

            assertTrue(
                    IncludeNodes
                            .all()
                            .with("test", equalTo("test")).include(n));

            assertFalse(
                    IncludeNodes
                            .all()
                            .with("test", equalTo("test2")).include(n));

            assertFalse(
                    IncludeNodes
                            .all()
                            .with("test", undefined()).include(n));

            tx.commit();
        }
    }
}
