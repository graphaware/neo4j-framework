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

package com.graphaware.service;

import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LinkingServiceImpl implements LinkingService {

    private final GraphDatabaseService database;

    @Autowired
    public LinkingServiceImpl(GraphDatabaseService database) {
        this.database = database;
    }

    @Override
    public void link(long startNodeId, long endNodeId) {
        try (Transaction tx = database.beginTx()) {
            database.getNodeById(startNodeId).createRelationshipTo(database.getNodeById(endNodeId),
                    RelationshipType.withName("TEST"));
            tx.success();
        }
    }
}
