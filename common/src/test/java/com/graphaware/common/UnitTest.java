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

package com.graphaware.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

/**
 *  Unit test with Neo4j.
 */
public abstract class UnitTest {

    private ServerControls controls;
    protected GraphDatabaseService database;

    @BeforeEach
    public void setUp() {
        createDatabase();

        populate(database);
    }

    @AfterEach
    public void tearDown() {
        destroyDatabase();
    }

    protected void populate(GraphDatabaseService database) {

    }

    protected final void createDatabase() {
        controls = TestServerBuilders.newInProcessBuilder().newServer();
        database = controls.graph();
    }

    protected final void destroyDatabase() {
        controls.close();
    }
}
