/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.description.property;

import com.graphaware.common.description.predicate.Predicate;
import org.neo4j.graphdb.Entity;

import java.util.Map;

import static com.graphaware.common.description.predicate.Predicates.any;

/**
 * A {@link BaseDetachedPropertiesDescription} where every predicate not explicitly defined is
 * {@link com.graphaware.common.description.predicate.Any}.
 */
public class WildcardPropertiesDescription extends BaseDetachedPropertiesDescription {

    /**
     * Construct a new properties description as the most specific description of the given entity.
     *
     * @param entity to construct the most specific properties description from.
     */
    public WildcardPropertiesDescription(Entity entity) {
        super(entity);
    }

    /**
     * Construct a new properties description from the given map of predicates.
     *
     * @param predicates predicates.
     */
    public WildcardPropertiesDescription(Map<String, Predicate> predicates) {
        super(predicates);
    }

    @Override
    protected DetachedPropertiesDescription newInstance(Map<String, Predicate> predicates) {
        return new WildcardPropertiesDescription(predicates);
    }

    @Override
    protected Predicate undefined() {
        return any();
    }

    @Override
    public boolean isMoreGeneralThan(PropertiesDescription other) {
        //optimization
        if (predicates.isEmpty()) {
            return true;
        }

        for (String key : getKeys()) {
            if (!get(key).isMoreGeneralThan(other.get(key))) {
                return false;
            }
        }

        return true;
    }
}
