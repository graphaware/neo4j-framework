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

package com.graphaware.test.data;

import com.graphaware.test.integration.DatabaseIntegrationTest;
import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static com.graphaware.test.unit.GraphUnit.assertEmpty;
import static com.graphaware.test.unit.GraphUnit.assertSameGraph;

/**
 * Test for {@link com.graphaware.test.data.CypherFilesPopulator}.
 */
public class CypherFilesPopulatorTest extends EmbeddedDatabaseIntegrationTest {

    @Test
    public void shouldProduceEmptyDatabaseWhenPopulatorReturnsNoFiles() {
        new CypherFilesPopulator() {
            @Override
            protected String[] files() {
                return new String[0];
            }
        }.populate(getDatabase());

        assertEmpty(getDatabase());
    }

    @Test
    public void shouldProduceEmptyDatabaseWhenPopulatorReturnsNull() {
        new CypherFilesPopulator() {
            @Override
            protected String[] files() {
                return null;
            }
        }.populate(getDatabase());

        assertEmpty(getDatabase());
    }

    @Test
    public void shouldPopulateDatabaseFromSingleFile() {
        new CypherFilesPopulator() {
            @Override
            protected String[] files() throws IOException {
                return new String[]{new ClassPathResource("statements.cyp").getFile().getAbsolutePath()};
            }
        }.populate(getDatabase());

        assertSameGraph(getDatabase(), "CREATE " +
                "(n1:Person {name: 'Isabell McGlynn'})," +
                "(n2:Person {name: 'Kelton Kuhn'})," +
                "(n3:Person {name: 'Chesley Feil'})," +
                "(n4:Person {name: 'Adrain Daugherty'})," +
                "(n5:Person {name: 'Kyleigh Stehr'})," +
                "(n1)-[:KNOWS]->(n5)," +
                "(n1)-[:KNOWS]->(n3)," +
                "(n2)-[:KNOWS]->(n1)," +
                "(n3)-[:KNOWS]->(n5)," +
                "(n4)-[:KNOWS]->(n5)," +
                "(n4)-[:KNOWS]->(n2)");
    }

    @Test
    public void shouldPopulateDatabaseFromMultipleFiles() {
        new CypherFilesPopulator() {
            @Override
            protected String[] files() throws IOException {
                return new String[]{new ClassPathResource("statements2.cyp").getFile().getAbsolutePath(), new ClassPathResource("statements3.cyp").getFile().getAbsolutePath()};
            }
        }.populate(getDatabase());

        assertSameGraph(getDatabase(), "CREATE (m:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})<-[:WORKS_FOR]-(d:Person {name: 'Daniela'})");
    }
}
