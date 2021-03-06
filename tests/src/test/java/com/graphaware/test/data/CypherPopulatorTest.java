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

package com.graphaware.test.data;

import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.GraphDatabaseService;

import static com.graphaware.test.unit.GraphUnit.assertEmpty;
import static com.graphaware.test.unit.GraphUnit.assertSameGraph;

/**
 * Test for {@link com.graphaware.test.data.CypherPopulator}.
 */
@ExtendWith(Neo4jExtension.class)
public class CypherPopulatorTest {

    @InjectNeo4j
    private GraphDatabaseService database;
    
    @Test
    public void shouldProduceEmptyDatabaseWhenPopulatorReturnsNoStatementGroups() {
        new CypherPopulator() {
            @Override
            protected String[] statementGroups() {
                return new String[0];
            }
        }.populate(database);

        assertEmpty(database);
    }

    @Test
    public void shouldProduceEmptyDatabaseWhenPopulatorReturnsNull() {
        new CypherPopulator() {
            @Override
            protected String[] statementGroups() {
                return null;
            }
        }.populate(database);

        assertEmpty(database);
    }

    @Test
    public void shouldPopulateDatabaseFromSingleStatement() {
        new CypherPopulator() {
            @Override
            protected String[] statementGroups() {
                return new String[]{"CREATE (m:Person {name:'Michal'})"};
            }
        }.populate(database);

        assertSameGraph(database, "CREATE (m:Person {name:'Michal'})");
    }

    @Test
    public void shouldPopulateDatabaseFromSingleStatementEndedBySemicolon() {
        new CypherPopulator() {
            @Override
            protected String[] statementGroups() {
                return new String[]{"CREATE (m:Person {name:'Michal'});"};
            }
        }.populate(database);

        assertSameGraph(database, "CREATE (m:Person {name:'Michal'})");
    }

    @Test
    public void shouldPopulateDatabaseFromSingleStatementGroup() {
        new CypherPopulator() {
            @Override
            protected String[] statementGroups() {
                return new String[]{"CREATE (m:Person {name:'Michal'}); MATCH (m:Person {name:'Michal'}) MERGE (m)-[:WORKS_FOR]->(ga:Company {name:'GraphAware'})"};
            }

    @Override
            protected String separator() {
                return ";";
            }
        }.populate(database);

        assertSameGraph(database, "CREATE (m:Person {name:'Michal'})-[:WORKS_FOR]->(ga:Company {name:'GraphAware'})");
    }

    @Test
    public void shouldPopulateDatabaseFromSingleStatementGroup2() {
        new CypherPopulator() {
            @Override
            protected String[] statementGroups() {
                return new String[]{"CREATE (m:Person {name:'Michal'});" + System.getProperty("line.separator") + "MATCH (m:Person {name:'Michal'}) MERGE (m)-[:WORKS_FOR]->(ga:Company {name:'GraphAware'})"};
            }
        }.populate(database);

        assertSameGraph(database, "CREATE (m:Person {name:'Michal'})-[:WORKS_FOR]->(ga:Company {name:'GraphAware'})");
    }

    @Test
    public void shouldPopulateDatabaseFromMultipleStatementGroups() {
        new CypherPopulator() {
            @Override
            protected String[] statementGroups() {
                return new String[]{"CREATE (m:Person {name:'Michal'}); CREATE (d:Person {name:'Daniela'});", "MATCH (m:Person {name:'Michal'}) MERGE (m)-[:WORKS_FOR]->(ga:Company {name:'GraphAware'});"};
            }

    @Override
            protected String separator() {
                return ";";
            }
        }.populate(database);

        assertSameGraph(database, "CREATE (d:Person {name:'Daniela'}), (m:Person {name:'Michal'})-[:WORKS_FOR]->(ga:Company {name:'GraphAware'})");
    }
}
