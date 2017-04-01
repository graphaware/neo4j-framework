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

package com.ga;

import com.graphaware.common.util.IterableUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.proc.Procedures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HelloWorldService implements GreetingService {

    @Autowired
    private GraphDatabaseService database;

    @Autowired
    private Procedures procedures;

    @Override
    public String greet() {
        if (procedures == null) {
            throw new IllegalStateException("Procedures not wired in!");
        }

        try (Transaction tx = database.beginTx()) {
            return "Hello World! There are " + IterableUtils.count(database.getAllNodes()) + " nodes in the database.";
        }
    }
}
