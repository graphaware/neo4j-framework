/*
 * Copyright (c) 2015 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime.write;

import com.graphaware.writer.DatabaseWriter;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * A configuration of {@link DatabaseWriter}s for the purposes of the framework.
 */
public interface WritingConfig {

    /**
     * Produce a database writer configured by this configuration, setup to write to the given database.
     *
     * @param database that the writer will write to.
     * @return writer.
     */
    DatabaseWriter produceWriter(GraphDatabaseService database);
}
