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

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.test.util.TestUtils;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;

import java.util.Map;

/**
 * {@link QualifyingRelationshipsReadTest} for Java API.
 */
public class QualifyingRelationshipsReadJavaTest extends QualifyingRelationshipsReadTest {

    private static final Log LOG = LoggerFactory.getLogger(QualifyingRelationshipsReadJavaTest.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String shortName() {
        return "qualifying-relationships-read-java";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String longName() {
        return "Qualifying Relationships (Reading, Java API)";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long run(GraphDatabaseService database, final Map<String, Object> params) {
        long time = 0;

        try (Transaction tx = database.beginTx()) {

            for (int i = 0; i < OPS_PER_TRIAL; i++) {
                final Node node = randomNode(database, NO_NODES);

                time += TestUtils.time(new TestUtils.Timed() {
                    @Override
                    public void time() {
                        switch ((RelationshipQualifier) params.get(TYPE_OR_PROPERTY)) {
                            case PROPERTY:
                                int count = 0;
                                for (Relationship r : node.getRelationships(RATED, randomDirection())) {
                                    if ((int) r.getProperty(RATING) >= (NO_TYPES / 2)) {
                                        count++;
                                    }
                                }
                                LOG.debug(""+ count);
                                break;
                            case RELATIONSHIP_TYPE:
                                count = 0;
                                RelationshipType[] types = new RelationshipType[NO_TYPES / 2];
                                for (int i = 0; i < NO_TYPES / 2; i++) {
                                    types[i] = REL_TYPES[(NO_TYPES / 2) + i];
                                }

                                for (Relationship r : node.getRelationships(randomDirection(), types)) {
                                    count++;
                                }

                                LOG.debug("" + count);
                                break;
                        }
                    }
                });

            }

            tx.success();
        }

        return time;
    }
}
