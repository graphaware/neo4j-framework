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

package com.graphaware.tx.event.batch.data;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.event.PropertyEntry;

/**
 * A copy-paste of {@link org.neo4j.kernel.impl.core.PropertyEntryImpl} as it can't be used outside its package.
 * Unfortunate and exceptional in GraphAware.
 */
public class PropertyEntryImpl<T extends PropertyContainer> implements PropertyEntry<T> {
    private final T entity;
    private final String key;
    private final Object value;
    private final Object valueBeforeTransaction;

    private PropertyEntryImpl(T entity, String key, Object value, Object valueBeforeTransaction) {
        this.entity = entity;
        this.key = key;
        this.value = value;
        this.valueBeforeTransaction = valueBeforeTransaction;
    }

    public static <T extends PropertyContainer> PropertyEntry<T> assigned(T entity, String key, Object value, Object valueBeforeTransaction) {
        if (value == null) {
            throw new IllegalArgumentException("Null value");
        }
        return new PropertyEntryImpl<>(entity, key, value, valueBeforeTransaction);
    }

    public static <T extends PropertyContainer> PropertyEntry<T> removed(T entity, String key, Object valueBeforeTransaction) {
        return new PropertyEntryImpl<>(entity, key, null, valueBeforeTransaction);
    }

    public T entity() {
        return this.entity;
    }

    public String key() {
        return this.key;
    }

    public Object previouslyCommitedValue() {
        return this.valueBeforeTransaction;
    }

    public Object value() {
        if (this.value == null) {
            throw new IllegalStateException("PropertyEntry[" + entity + ", " + key + "] has no value, it represents a removed property");
        }
        return this.value;
    }

    @Override
    public String toString() {
        return "PropertyEntry[entity:" + entity + ", key:" + key + ", value:" + value + ", valueBeforeTx:" + valueBeforeTransaction + "]";
    }
}
