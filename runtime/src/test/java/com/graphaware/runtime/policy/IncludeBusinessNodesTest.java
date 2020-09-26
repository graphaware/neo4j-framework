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

package com.graphaware.runtime.policy;

import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import com.graphaware.common.policy.inclusion.composite.CompositeNodeInclusionPolicy;
import com.graphaware.common.policy.inclusion.fluent.IncludeNodes;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.predicate.Predicates.undefined;
import static com.graphaware.common.policy.inclusion.composite.CompositeNodeInclusionPolicy.of;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.neo4j.graphdb.Label.label;

/**
 * Test for {@link CompositeNodeInclusionPolicy} with {@link IncludeAllBusinessNodes} and a programmatically configured {@link IncludeNodes}
 */
@ExtendWith(Neo4jExtension.class)
public class IncludeBusinessNodesTest {

    @InjectNeo4j
    private Neo4j controls;
    @InjectNeo4j
    private GraphDatabaseService database;

    @Test
    public void shouldIncludeCorrectRelationships() {
        try (Transaction tx = database.beginTx()) {
            Node n = tx.createNode(label("Test"));
            n.setProperty("test", "test");
            Node internal = tx.createNode(label(GraphAwareRuntime.GA_PREFIX + "test"));

            assertTrue(of(IncludeAllBusinessNodes.getInstance(), IncludeNodes.all()).include(n));
            assertFalse(of(IncludeAllBusinessNodes.getInstance(), IncludeNodes.all()).include(internal));
            assertTrue(of(IncludeAllBusinessNodes.getInstance(), IncludeNodes.all().with(label("Test"))).include(n));
            assertFalse(of(IncludeAllBusinessNodes.getInstance(), IncludeNodes.all().with("Test")).include(internal));
            assertFalse(of(IncludeAllBusinessNodes.getInstance(), IncludeNodes.all().with(label("Test2"))).include(n));
            assertTrue(of(IncludeAllBusinessNodes.getInstance(), IncludeNodes.all().with(label("Bla")).with((Label) null)).include(n));

            assertTrue(
                    of(IncludeAllBusinessNodes.getInstance(), IncludeNodes
                            .all()
                            .with("test", equalTo("test"))).include(n));

            assertFalse(
                    of(IncludeAllBusinessNodes.getInstance(), IncludeNodes
                            .all()
                            .with("test", equalTo("test"))).include(internal));

            assertFalse(
                    of(IncludeAllBusinessNodes.getInstance(), IncludeNodes
                            .all()
                            .with("test", equalTo("test2"))).include(n));

            assertFalse(
                    of(IncludeAllBusinessNodes.getInstance(), IncludeNodes
                            .all()
                            .with("test", undefined())).include(n));

            tx.commit();
        }
    }
}
