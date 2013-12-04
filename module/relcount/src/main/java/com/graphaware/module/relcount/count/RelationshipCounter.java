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

package com.graphaware.module.relcount.count;

import com.graphaware.common.description.relationship.RelationshipDescription;
import org.neo4j.graphdb.Node;

/**
 * A node in/out-degree counter.
 */
public interface RelationshipCounter {

    /**
     * Count relationships with the given description at the given node.
     *
     * @param node        on which to count relationships.
     * @param description of the relationships to count.
     * @return number of relationships.
     * @throws UnableToCountException indicating that for some reason, relationships could not be counted.
     *                                For example, when asking for a count purely based on cached values and the cached
     *                                values are not present (e.g. have been compacted-out).
     */
    int count(Node node, RelationshipDescription description);
}
