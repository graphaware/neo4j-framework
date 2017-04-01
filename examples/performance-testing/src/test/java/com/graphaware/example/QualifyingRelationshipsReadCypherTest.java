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

package com.graphaware.example;

import com.graphaware.test.util.TestUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Collections;
import java.util.Map;

import static com.graphaware.example.RelationshipQualifier.PROPERTY;
import static com.graphaware.example.RelationshipQualifier.RELATIONSHIP_TYPE;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * {@link QualifyingRelationshipsReadTest} for Cypher.
 */
public class QualifyingRelationshipsReadCypherTest extends QualifyingRelationshipsReadTest {

    /**
     * {@inheritDoc}
     */
    @Override
    public String shortName() {
        return "qualifying-relationships-read-cypher";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String longName() {
        return "Qualifying Relationships (Read, Cypher)";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepare(GraphDatabaseService database, final Map<String, Object> params) {
        super.prepare(database, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long run(final GraphDatabaseService database, final Map<String, Object> params) {
        long time = 0;

        final String query = buildQuery(params);

        for (int i = 0; i < OPS_PER_TRIAL; i++) {
            time += TestUtils.time(new TestUtils.Timed() {
                @Override
                public void time() {
                    database.execute(query, Collections.<String, Object>singletonMap("id", RANDOM.nextInt(NO_NODES)));
                }
            });
        }

        return time;
    }

    private String buildQuery(Map<String, Object> params) {
        StringBuilder query = new StringBuilder("START n=node({id}) MATCH n");

        Direction direction = randomDirection();
        RelationshipQualifier typeOrProperty = (RelationshipQualifier) params.get(TYPE_OR_PROPERTY);

        if (INCOMING.equals(direction)) {
            query.append("<");
        }

        query.append("-[");

        if (RELATIONSHIP_TYPE.equals(typeOrProperty)) {
            for (int i = 0; i < NO_TYPES / 2; i++) {
                if (i == 0) {
                    query.append(":");
                } else {
                    query.append("|");
                }
                query.append(TYPE).append((NO_TYPES / 2) + i);
            }
        } else {
            query.append("r:").append(RATED.name());
        }

        query.append("]-");

        if (OUTGOING.equals(direction)) {
            query.append(">");
        }

        query.append("m");

        if (PROPERTY.equals(typeOrProperty)) {
            query.append(" WHERE r.").append(RATING).append(" >= ").append(NO_TYPES / 2);
        }

        query.append(" RETURN count (m)");

        return query.toString();
    }
}
