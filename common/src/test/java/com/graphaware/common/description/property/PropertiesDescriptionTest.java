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

import com.graphaware.common.description.TestMapUtils;
import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

@ExtendWith(Neo4jExtension.class)
public abstract class PropertiesDescriptionTest {

    @InjectNeo4j
    protected GraphDatabaseService database;

    protected long id;

    @BeforeEach
    protected void populate() {
        Node root;

        try (Transaction tx = database.beginTx()) {
            root = tx.createNode();
            root.setProperty("two", 2);
            root.setProperty("three", "3");
            root.setProperty("array", new int[]{4, 5});

            id = root.getId();

            tx.commit();
        }


    }

    protected LazyPropertiesDescription lazy(Transaction tx) {
        return new LazyPropertiesDescription(tx.getNodeById(id));
    }

    protected LiteralPropertiesDescription literal(Transaction tx) {
        return new LiteralPropertiesDescription(tx.getNodeById(id));
    }

    protected WildcardPropertiesDescription wildcard(Transaction tx) {
        return new WildcardPropertiesDescription(tx.getNodeById(id));
    }

    protected LiteralPropertiesDescription literal(Object... stringOrPredicate) {
        return new LiteralPropertiesDescription(TestMapUtils.toMap(stringOrPredicate));
    }

    protected WildcardPropertiesDescription wildcard(Object... stringOrPredicate) {
        return new WildcardPropertiesDescription(TestMapUtils.toMap(stringOrPredicate));
    }
}
