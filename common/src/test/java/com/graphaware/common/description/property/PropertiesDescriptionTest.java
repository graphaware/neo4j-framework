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

package com.graphaware.common.description.property;

import com.graphaware.common.description.TestMapUtils;
import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;

public abstract class PropertiesDescriptionTest {

    private GraphDatabaseService database;
    private Transaction tx;

    protected PropertyContainer propertyContainer;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        registerShutdownHook(database);

        try (Transaction tx = database.beginTx()) {
            Node root = database.createNode();
            root.setProperty("two", 2);
            root.setProperty("three", "3");
            root.setProperty("array", new int[]{4, 5});
            tx.success();
        }

        tx = database.beginTx();
        propertyContainer = database.getNodeById(0);
    }

    @After
    public void tearDown() {
        tx.close();
        database.shutdown();
    }

    protected LazyPropertiesDescription lazy() {
        return new LazyPropertiesDescription(propertyContainer);
    }

    protected LiteralPropertiesDescription literal() {
        return new LiteralPropertiesDescription(propertyContainer);
    }

    protected WildcardPropertiesDescription wildcard() {
        return new WildcardPropertiesDescription(propertyContainer);
    }

    protected LiteralPropertiesDescription literal(Object... stringOrPredicate) {
        return new LiteralPropertiesDescription(TestMapUtils.toMap(stringOrPredicate));
    }

    protected WildcardPropertiesDescription wildcard(Object... stringOrPredicate) {
        return new WildcardPropertiesDescription(TestMapUtils.toMap(stringOrPredicate));
    }
}
