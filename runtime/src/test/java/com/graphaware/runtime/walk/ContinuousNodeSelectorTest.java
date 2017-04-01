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

package com.graphaware.runtime.walk;

import com.graphaware.common.policy.inclusion.fluent.IncludeNodes;
import com.graphaware.common.policy.inclusion.none.IncludeNoNodes;
import com.graphaware.test.data.DatabasePopulator;
import com.graphaware.test.data.SingleTransactionPopulator;
import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.assertEquals;
import static org.neo4j.graphdb.Label.label;

/**
 *  Test for {@link ContinuousNodeSelector}.
 */
public class ContinuousNodeSelectorTest extends EmbeddedDatabaseIntegrationTest {

    @Override
    protected DatabasePopulator databasePopulator() {
        return new SingleTransactionPopulator() {
            @Override
            protected void doPopulate(GraphDatabaseService database) {
                database.createNode(label("Person")).setProperty("name", "Michal");
                database.createNode(label("Person")).setProperty("name", "Daniela");
                database.createNode(label("Person")).setProperty("name", "Vince");
                database.createNode(label("Company")).setProperty("name", "GraphAware");
                database.createNode(label("Company")).setProperty("name", "Neo");
                database.createNode(label("Person")).setProperty("name", "Adam");

                database.getNodeById(2).delete();
            }
        };
    }

    @Test
    public void shouldSelectCorrectNodes() {
        NodeSelector selector = new ContinuousNodeSelector(IncludeNodes.all().with(label("Person")));

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals("Michal", selector.selectNode(getDatabase()).getProperty("name"));
            assertEquals("Daniela", selector.selectNode(getDatabase()).getProperty("name"));
            assertEquals("Adam", selector.selectNode(getDatabase()).getProperty("name"));
            assertEquals("Michal", selector.selectNode(getDatabase()).getProperty("name"));
            assertEquals("Daniela", selector.selectNode(getDatabase()).getProperty("name"));
            assertEquals("Adam", selector.selectNode(getDatabase()).getProperty("name"));

            tx.success();
        }
    }

    @Test
    public void shouldResumeFromLastNode() {
        NodeSelector selector = new ContinuousNodeSelector(IncludeNodes.all().with(label("Person")), 2L);

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals("Adam", selector.selectNode(getDatabase()).getProperty("name"));
            assertEquals("Michal", selector.selectNode(getDatabase()).getProperty("name"));
            assertEquals("Daniela", selector.selectNode(getDatabase()).getProperty("name"));
            assertEquals("Adam", selector.selectNode(getDatabase()).getProperty("name"));

            tx.success();
        }
    }

    @Test
    public void shouldTerminateWhenNoSuitableNodesExist() {
        NodeSelector selector = new ContinuousNodeSelector(IncludeNoNodes.getInstance());

        try (Transaction tx = getDatabase().beginTx()) {
            selector.selectNode(getDatabase());
            tx.success();
        }
    }
}
