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

package com.graphaware.api.transform;

import com.graphaware.api.json.LongIdJsonRelationship;
import com.graphaware.api.json.JsonRelationship;
import org.neo4j.graphdb.Relationship;

public class TrivialRelationshipTransformer implements RelationshipTransformer<JsonRelationship> {

    public static final TrivialRelationshipTransformer INSTANCE = new TrivialRelationshipTransformer();

    public static TrivialRelationshipTransformer getInstance() {
        return INSTANCE;
    }

    private TrivialRelationshipTransformer() {
    }

    @Override
    public JsonRelationship transform(Relationship relationship) {
        return new LongIdJsonRelationship(relationship);
    }
}
