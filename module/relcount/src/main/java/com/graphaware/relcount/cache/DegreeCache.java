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

package com.graphaware.relcount.cache;

import com.graphaware.framework.config.FrameworkConfigured;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Internal component that caches node degrees. Degrees must be cached by {@link org.neo4j.graphdb.RelationshipType},
 * {@link Direction} (only {@link Direction#INCOMING} or {@link Direction#OUTGOING}), and potentially properties.
 * <p/>
 * When handling a transaction event, callers must first call {@link #startCaching()} before any other method is called.
 * When finished handling the event, {@link #endCaching()} must be called, even if the handling methods throw an exception.
 * Implementations can choose to optimize based on the information about started and finished event handling.
 */
public interface DegreeCache extends FrameworkConfigured {

    /**
     * Tell the component that caching has started. {@link #endCaching()} must be called before this method is called again.
     *
     * @throws IllegalStateException if this method has been previously called and wasn't followed by a call to {@link #endCaching()}.
     */
    void startCaching();

    /**
     * Tell the component that caching has been finished.
     *
     * @throws IllegalStateException if {@link #startCaching()} method has not been previously called.
     */
    void endCaching();

    /**
     * Handle a created relationship.
     *
     * @param relationship     the has been created.
     * @param pointOfView      node whose point of view the created relationships is being handled. Can be wrapped in a decorator, e.g. for filtering.
     * @param defaultDirection in case the relationship direction would be resolved to {@link org.neo4j.graphdb.Direction#BOTH}, what
     *                         should it actually be resolved to? This must be {@link org.neo4j.graphdb.Direction#OUTGOING} or {@link org.neo4j.graphdb.Direction#INCOMING},
     *                         never cache {@link org.neo4j.graphdb.Direction#BOTH}!
     */
    void handleCreatedRelationship(Relationship relationship, Node pointOfView, Direction defaultDirection);

    /**
     * Handle a deleted relationship.
     *
     * @param relationship     the has been deleted.
     * @param pointOfView      node whose point of view the deleted relationships is being handled. Can be wrapped in a decorator, e.g. for filtering.
     * @param defaultDirection in case the relationship direction would be resolved to {@link Direction#BOTH}, what
     *                         should it actually be resolved to? This must be {@link Direction#OUTGOING} or {@link Direction#INCOMING},
     *                         never cache {@link Direction#BOTH}!
     */
    void handleDeletedRelationship(Relationship relationship, Node pointOfView, Direction defaultDirection);

    // Explanation of the relationship direction above: The meaning of BOTH can be unclear - is it just the cyclical relationship
    // or all? Also, there would be trouble during compaction and eventually, incoming and outgoing relationships could
    // be compacted to BOTH, so it would be impossible to find only incoming or outgoing.
}
