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

package com.graphaware.common.json;

import com.graphaware.common.representation.DetachedEntity;
import com.graphaware.common.transform.NodeIdTransformer;
import com.graphaware.test.unit.GraphUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class LongIdJsonNodeTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (Transaction tx = database.beginTx()) {
            Node node1 = database.createNode(Label.label("L1"), Label.label("L2"));
            Node node2 = database.createNode();

            node1.setProperty("k1", "v1");
            node1.setProperty("k2", 2);

            tx.success();
        }
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldCorrectlyProduceEntity() {
        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(database.getNodeById(1));
            jsonNode.produceEntity(database);
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(database.getNodeById(1), new TimesThousandNodeIdTransformer());
            jsonNode.produceEntity(database);
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(database.getNodeById(1));
            jsonNode.produceEntity(database, new TimesThousandNodeIdTransformer());
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(database.getNodeById(1), new TimesThousandNodeIdTransformer());
            jsonNode.produceEntity(database, new TimesThousandNodeIdTransformer());
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(database.getNodeById(1), new String[0]);
            jsonNode.produceEntity(database, new TimesThousandNodeIdTransformer());
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(database.getNodeById(1), new String[0], new TimesThousandNodeIdTransformer());
            jsonNode.produceEntity(database, new TimesThousandNodeIdTransformer());
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(1);
            Node node = jsonNode.produceEntity(database);
            assertEquals(node, database.getNodeById(1));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(1000);
            Node node = jsonNode.produceEntity(database, new TimesThousandNodeIdTransformer());
            assertEquals(node, database.getNodeById(1));

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(0, new String[]{"test"}, Collections.singletonMap("k", "v"));
            jsonNode.produceEntity(database);
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(0, new String[]{"test"}, Collections.singletonMap("k", "v"));
            jsonNode.produceEntity(database, new TimesThousandNodeIdTransformer());
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(555, new String[]{"test"}, Collections.singletonMap("k", "v"));
            jsonNode.produceEntity(database);
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(5000, new String[]{"test"}, Collections.singletonMap("k", "v"));
            jsonNode.produceEntity(database, new TimesThousandNodeIdTransformer());
            tx.success();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(new String[]{"Test"}, Collections.singletonMap("k", "v"));
            Node n = jsonNode.produceEntity(database, new TimesThousandNodeIdTransformer());
            assertEquals(20, n.getId());

            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonNode jsonNode = new LongIdJsonNode(new String[]{"Test"}, Collections.singletonMap("k", "v"));
            Node n = jsonNode.produceEntity(database);
            assertEquals(2, n.getId());

            tx.success();
        }

        GraphUnit.assertSameGraph(database, "CREATE " +
                "(n1:L1:L2 {k1: 'v1', k2: 2})," +
                "()," +
                "(:Test {k: 'v'})," +
                "(:Test {k: 'v'})"
        );
    }

    private class TimesThousandNodeIdTransformer implements NodeIdTransformer<Long> {

        @Override
        public long toGraphId(Long id) {
            if (id == null) {
                return DetachedEntity.NEW;
            }

            return id / 1000;
        }

        @Override
        public Long fromEntity(Node entity) {
            return entity.getId() * 1000;
        }
    }
}
