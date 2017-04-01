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

package com.graphaware.common.description.property;

import com.graphaware.common.description.BasePartiallyComparable;

/**
 * Base class for {@link PropertiesDescription} implementations.
 */
public abstract class BasePropertiesDescription extends BasePartiallyComparable<PropertiesDescription> implements PropertiesDescription {

    /**
     * {@inheritDoc}
     */
    @Override
    protected final PropertiesDescription self() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMoreGeneralThan(PropertiesDescription other) {
        for (String key : getKeys()) {
            if (!get(key).isMoreGeneralThan(other.get(key))) {
                return false;
            }
        }

        for (String key : other.getKeys()) {
            if (!get(key).isMoreGeneralThan(other.get(key))) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMutuallyExclusive(PropertiesDescription other) {
        for (String key : getKeys()) {
            if (get(key).isMutuallyExclusive(other.get(key))) {
                return true;
            }
        }

        for (String key : other.getKeys()) {
            if (get(key).isMutuallyExclusive(other.get(key))) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (String key : getKeys()) {
            result.append(key).append(get(key)).append("#");
        }
        return result.toString();
    }
}
