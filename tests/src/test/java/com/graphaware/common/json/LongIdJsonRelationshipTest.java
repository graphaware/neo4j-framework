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
import com.graphaware.common.transform.RelationshipIdTransformer;
import com.graphaware.test.unit.GraphUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LongIdJsonRelationshipTest {
    private Neo4j controls;
    protected GraphDatabaseService database;

    @BeforeEach
    public void setUp() {
        controls = Neo4jBuilders.newInProcessBuilder().build();
        database = controls.defaultDatabaseService();

        try (Transaction tx = database.beginTx()) {
            Node node1 = tx.createNode(Label.label("L1"), Label.label("L2"));
            Node node2 = tx.createNode();

            node1.setProperty("k1", "v1");
            node1.setProperty("k2", 2);

            Relationship r = node1.createRelationshipTo(node2, RelationshipType.withName("R"));
            r.setProperty("k1", "v1");
            r.setProperty("k2", 2);

            Relationship r2 = node1.createRelationshipTo(node2, RelationshipType.withName("R2"));
            r2.setProperty("k1", "v2");
            r2.setProperty("k2", 4);

            tx.commit();
        }
    }

    @AfterEach
    public void tearDown() {
        controls.close();
    }

    @Test
    public void shouldCorrectlyProduceEntity() {
        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship rel = new LongIdJsonRelationship(tx.getRelationshipById(1), new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            rel.produceEntity(database);
            tx.commit();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship rel = new LongIdJsonRelationship(tx.getRelationshipById(1), new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            rel.produceEntity(database, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            tx.commit();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship rel = new LongIdJsonRelationship(tx.getRelationshipById(1));
            rel.produceEntity(database);
            tx.commit();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship rel = new LongIdJsonRelationship(tx.getRelationshipById(1));
            rel.produceEntity(database, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            tx.commit();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship rel = new LongIdJsonRelationship(tx.getRelationshipById(0), new String[0], new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            rel.produceEntity(database);
            tx.commit();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship rel = new LongIdJsonRelationship(tx.getRelationshipById(0), new String[0], new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            rel.produceEntity(database, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            tx.commit();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = new LongIdJsonRelationship(0);
            Relationship r = jsonRel.produceEntity(database);
            assertEquals(r, tx.getRelationshipById(0));

            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = new LongIdJsonRelationship(1000);
            Relationship r = jsonRel.produceEntity(database, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            assertEquals(r, tx.getRelationshipById(1));

            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = new LongIdJsonRelationship(0, 0, 0, "test", Collections.emptyMap());
            jsonRel.produceEntity(database);
            tx.commit();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = new LongIdJsonRelationship(0, 0, 0, "test", Collections.emptyMap());
            jsonRel.produceEntity(database, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            tx.commit();
        } catch (IllegalStateException e) {
            //ok
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = new LongIdJsonRelationship(0, 1000, "TEST", Collections.singletonMap("k", "v"));
            Relationship r = jsonRel.produceEntity(database, new TimesThousandRelationshipIdTransformer(), new TimesThousandNodeIdTransformer());
            assertEquals(20, r.getId());

            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            LongIdJsonRelationship jsonRel = new LongIdJsonRelationship(0, 1, "TEST", Collections.singletonMap("k", "v"));
            Relationship r = jsonRel.produceEntity(database);
            assertEquals(2, r.getId());

            tx.commit();
        }

        GraphUnit.assertSameGraph(database, "CREATE " +
                "(n1:L1:L2 {k1: 'v1', k2: 2})," +
                "(n2)," +
                "(n1)-[:R {k1: 'v1', k2: 2}]->(n2)," +
                "(n1)-[:R2 {k1: 'v2', k2: 4}]->(n2)," +
                "(n1)-[:TEST {k: 'v'}]->(n2)," +
                "(n1)-[:TEST {k: 'v'}]->(n2)");
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

    private class TimesThousandRelationshipIdTransformer implements RelationshipIdTransformer<Long> {

        @Override
        public long toGraphId(Long id) {
            if (id == null) {
                return DetachedEntity.NEW;
            }

            return id / 1000;
        }

        @Override
        public Long fromEntity(Relationship entity) {
            return entity.getId() * 1000;
        }
    }
}
