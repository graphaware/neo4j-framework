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

import com.graphaware.common.policy.inclusion.composite.CompositeNodeInclusionPolicy;
import com.graphaware.common.policy.inclusion.fluent.IncludeNodes;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.backup.OnlineBackupSettings;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.shell.ShellSettings;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.predicate.Predicates.undefined;
import static com.graphaware.common.policy.inclusion.composite.CompositeNodeInclusionPolicy.of;
import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static com.graphaware.runtime.config.RuntimeConfiguration.GA_PREFIX;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.neo4j.graphdb.Label.label;
import static org.neo4j.kernel.configuration.Settings.FALSE;

/**
 * Test for {@link CompositeNodeInclusionPolicy} with {@link IncludeAllBusinessNodes} and a programmatically configured {@link IncludeNodes}
 */
public class IncludeBusinessNodesTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        registerShutdownHook(database);
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldIncludeCorrectRelationships() {
        try (Transaction tx = database.beginTx()) {
            Node n = database.createNode(label("Test"));
            n.setProperty("test", "test");
            Node internal = database.createNode(label(GA_PREFIX + "test"));

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

            tx.success();
        }
    }
}
