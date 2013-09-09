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

package com.graphaware.description.properties;

import com.graphaware.description.predicate.Predicate;
import org.neo4j.graphdb.PropertyContainer;

import java.util.HashMap;
import java.util.Map;

import static com.graphaware.description.predicate.Predicates.equalTo;

/**
 *
 */
public class PropertiesDescriptionImpl implements PropertiesDescription {

    private final Map<String, Predicate> predicates;

    public PropertiesDescriptionImpl(PropertyContainer propertyContainer) {
        predicates = new HashMap<>();

        for (String key : propertyContainer.getPropertyKeys()) {
            predicates.put(key, equalTo(propertyContainer.getProperty(key)));
        }
    }

    private PropertiesDescriptionImpl(Map<String, Predicate> predicates) {
        this.predicates = predicates;
    }

    @Override
    public PropertiesDescription with(String propertyKey, Predicate predicate) {
        Map<String, Predicate> newPredicates = new HashMap<>(predicates);
        newPredicates.put(propertyKey, predicate);
        return new PropertiesDescriptionImpl(newPredicates);
    }

    @Override
    public boolean isMutuallyExclusive(PropertiesDescription other) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isMoreGeneralThan(PropertiesDescription other) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isMoreSpecificThan(PropertiesDescription other) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
