/*
 * Copyright (c) 2013 GraphAware
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

package com.graphaware.propertycontainer.persistent;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Object can can be persisted to the database (merged) and detached again.
 * <p/>
 * Experimental, will probably go away / be changed
 */
public interface Persistent {

    /**
     * Detach the object from the database, i.e. hydrate/load all the information needed when used in a place without
     * the database (such as a remote location).
     */
    void detach();

    /**
     * Merge itself to the database.
     *
     * @param database to merge to.
     */
    void merge(GraphDatabaseService database);
}
