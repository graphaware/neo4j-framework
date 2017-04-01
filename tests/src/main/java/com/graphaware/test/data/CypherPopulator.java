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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 * A {@link com.graphaware.test.data.DatabasePopulator} that populates the database from Cypher statement groups, provided
 * by implementing {@link #statementGroups()}. Each statement group is executed in a single transaction and can be composed
 * of multiple statements separated by {@link #separator()}, which, by default, is a semicolon followed by a new line character.
 */
public abstract class CypherPopulator implements DatabasePopulator {

    @Override
    public void populate(GraphDatabaseService database) {
        String separator = separator();

        String[] statementGroups = statementGroups();
        if (statementGroups == null) {
            return;
        }

        for (String statementGroup : statementGroups) {
            try (Transaction tx = database.beginTx()) {
                for (String statement : normalize(statementGroup).split(separator)) {
                    database.execute(statement);
                }
                tx.success();
            }
        }
    }

    /**
     * Normalize the Cypher String. Designed to be overridden, especially on Windows.
     *
     * @param input to normalize.
     * @return normalized input.
     */
    protected String normalize(String input) {
        return input.replaceAll("\\r\\n", "\n");
    }

    /**
     * @return separator used for separating statements in statement groups.
     */
    protected String separator() {
        return ";" + "\n";
    }

    /**
     * @return Cypher statement groups. Each statement group can be composed of multiple statements separated by
     * {@link #separator()}, which, by default, is a semicolon followed by a new line character.
     */
    protected abstract String[] statementGroups();
}
