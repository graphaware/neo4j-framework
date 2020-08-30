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

package com.graphaware.common.description.property;

import com.graphaware.common.UnitTest;
import com.graphaware.common.description.TestMapUtils;
import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

public abstract class PropertiesDescriptionTest extends UnitTest {

    private Transaction tx;
    protected Entity entity;

    @Override
    protected void populate(GraphDatabaseService database) {
        try (Transaction tx = database.beginTx()) {
            Node root = database.createNode();
            root.setProperty("two", 2);
            root.setProperty("three", "3");
            root.setProperty("array", new int[]{4, 5});
            tx.success();
        }

        tx = database.beginTx();
        entity = database.getNodeById(0);
    }

    protected LazyPropertiesDescription lazy() {
        return new LazyPropertiesDescription(entity);
    }

    protected LiteralPropertiesDescription literal() {
        return new LiteralPropertiesDescription(entity);
    }

    protected WildcardPropertiesDescription wildcard() {
        return new WildcardPropertiesDescription(entity);
    }

    protected LiteralPropertiesDescription literal(Object... stringOrPredicate) {
        return new LiteralPropertiesDescription(TestMapUtils.toMap(stringOrPredicate));
    }

    protected WildcardPropertiesDescription wildcard(Object... stringOrPredicate) {
        return new WildcardPropertiesDescription(TestMapUtils.toMap(stringOrPredicate));
    }
}
