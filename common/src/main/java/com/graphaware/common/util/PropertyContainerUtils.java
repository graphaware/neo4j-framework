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

package com.graphaware.common.util;

import com.graphaware.common.policy.inclusion.ObjectInclusionPolicy;
import com.graphaware.common.policy.inclusion.all.IncludeAll;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import java.util.*;

import static com.graphaware.common.util.ArrayUtils.isPrimitiveOrStringArray;
import static com.graphaware.common.util.ArrayUtils.primitiveOrStringArrayToString;

/**
 * Utility methods for dealing with {@link org.neo4j.graphdb.PropertyContainer}s.
 */
public final class PropertyContainerUtils {

    /**
     * Convert a collection of {@link org.neo4j.graphdb.PropertyContainer}s to a map of {@link org.neo4j.graphdb.PropertyContainer}s keyed by their ID.
     *
     * @param propertyContainers to convert.
     * @param <T>                type of the {@link org.neo4j.graphdb.PropertyContainer}.
     * @return map keyed by {@link org.neo4j.graphdb.PropertyContainer} ID with the actual {@link org.neo4j.graphdb.PropertyContainer}s as values.
     */
    public static <T extends PropertyContainer> Map<Long, T> propertyContainersToMap(Collection<T> propertyContainers) {
        Map<Long, T> result = new HashMap<>();

        for (T propertyContainer : propertyContainers) {
            result.put(id(propertyContainer), propertyContainer);
        }

        return result;
    }

    /**
     * Get ID from a {@link org.neo4j.graphdb.PropertyContainer}.
     *
     * @param propertyContainer to get ID from. Must be a {@link org.neo4j.graphdb.Node} or {@link org.neo4j.graphdb.Relationship}. Must not be <code>null</code>.
     * @return ID
     * @throws IllegalStateException in case the propertyContainer is not a {@link org.neo4j.graphdb.Node} or a {@link org.neo4j.graphdb.Relationship}.
     */
    public static long id(PropertyContainer propertyContainer) {
        Objects.requireNonNull(propertyContainer);

        if (Node.class.isAssignableFrom(propertyContainer.getClass())) {
            return ((Node) propertyContainer).getId();
        }

        if (Relationship.class.isAssignableFrom(propertyContainer.getClass())) {
            return ((Relationship) propertyContainer).getId();
        }

        throw new IllegalStateException("Unknown Property Container: " + propertyContainer.getClass().getName());
    }

    /**
     * Get IDs from an {@link Iterable} of {@link org.neo4j.graphdb.PropertyContainer}s.
     *
     * @param propertyContainers to get ID from. Must be an {@link Iterable} of {@link org.neo4j.graphdb.Node}s or {@link org.neo4j.graphdb.Relationship}s.
     * @return IDs
     * @throws IllegalStateException in case one of the propertyContainers is not a {@link org.neo4j.graphdb.Node} or a {@link org.neo4j.graphdb.Relationship}.
     */
    public static Long[] ids(Iterable<? extends PropertyContainer> propertyContainers) {
        List<Long> result = new LinkedList<>();

        for (PropertyContainer pc : propertyContainers) {
            result.add(id(pc));
        }

        return result.toArray(new Long[result.size()]);
    }

    /**
     * Convert a property value to String. If the value is <code>null</code>, then it will be converted to an empty String.
     *
     * @param value to convert.
     * @return property value as String.
     */
    public static String valueToString(Object value) {
        if (value == null) { //this will not happen as of Neo4j 2.0
            return "";
        }

        if (isPrimitiveOrStringArray(value)) {
            return primitiveOrStringArrayToString(value);
        }
        return String.valueOf(value);
    }

    /**
     * Convert all properties from a {@link org.neo4j.graphdb.PropertyContainer} to a {@link java.util.Map}, where the key is the
     * property key and value is the property value. Keys must not be <code>null</code>
     * or empty. <code>Null</code> values are fine.
     *
     * @param propertyContainer to convert properties from.
     * @return converted properties.
     */
    public static Map<String, Object> propertiesToMap(PropertyContainer propertyContainer) {
        return propertiesToMap(propertyContainer, new IncludeAll<String>());
    }

    /**
     * Convert selected properties from a {@link org.neo4j.graphdb.PropertyContainer} to a {@link java.util.Map}, where the key is the
     * property key and value is the property value. Keys must not be <code>null</code>
     * or empty. <code>Null</code> values are fine.
     *
     * @param propertyContainer         to convert properties from.
     * @param propertyInclusionPolicy policy to select which properties to include. Decides based on the property key.
     * @return converted properties.
     */
    public static Map<String, Object> propertiesToMap(PropertyContainer propertyContainer, ObjectInclusionPolicy<String> propertyInclusionPolicy) {
        Map<String, Object> result = new HashMap<>();
        for (String key : propertyContainer.getPropertyKeys()) {
            if (propertyInclusionPolicy.include(key)) {
                result.put(key, propertyContainer.getProperty(key));
            }
        }
        return result;
    }

    /**
     * Delete a node, but delete all its relationships first.
     * This method assumes a transaction is in progress.
     *
     * @param toDelete node to delete along with its relationships.
     * @return number of deleted relationships.
     */
    public static int deleteNodeAndRelationships(Node toDelete) {
        int result = 0;
        for (Relationship relationship : toDelete.getRelationships()) {
            relationship.delete();
            result++;
        }
        toDelete.delete();
        return result;
    }

    /**
     * Convert a {@link Node} to a human-readable String.
     *
     * @param node to convert.
     * @return node as String.
     */
    public static String nodeToString(Node node) {
        StringBuilder string = new StringBuilder("(");

        List<String> labelNames = new LinkedList<>();
        for (Label label : node.getLabels()) {
            labelNames.add(label.name());
        }
        Collections.sort(labelNames);

        for (String labelName : labelNames) {
            string.append(":").append(labelName);
        }

        String props = propertiesToString(node);

        if (StringUtils.isNotEmpty(props) && !labelNames.isEmpty()) {
            string.append(" ");
        }

        string.append(props);

        string.append(")");

        return string.toString();
    }

    /**
     * Convert a {@link Relationship} to a human-readable String.
     *
     * @param relationship to convert.
     * @return relationship as String.
     */
    public static String relationshipToString(Relationship relationship) {
        StringBuilder string = new StringBuilder();

        string.append(nodeToString(relationship.getStartNode()));
        string.append("-[:").append(relationship.getType().name());
        String props = propertiesToString(relationship);
        if (StringUtils.isNotEmpty(props)) {
            string.append(" ");
        }
        string.append(props);
        string.append("]->");
        string.append(nodeToString(relationship.getEndNode()));

        return string.toString();
    }

    /**
     * Convert a {@link PropertyContainer} to a human-readable String.
     *
     * @param propertyContainer to convert.
     * @return propertyContainer as String.
     */
    public static String propertiesToString(PropertyContainer propertyContainer) {
        if (!propertyContainer.getPropertyKeys().iterator().hasNext()) {
            return "";
        }

        StringBuilder string = new StringBuilder("{");

        List<String> propertyKeys = new LinkedList<>();
        for (String key : propertyContainer.getPropertyKeys()) {
            propertyKeys.add(key);
        }
        Collections.sort(propertyKeys);

        boolean first = true;
        for (String key : propertyKeys) {
            if (!first) {
                string.append(", ");
            }
            first = false;
            string.append(key).append(": ").append(valueToString(propertyContainer.getProperty(key)));
        }

        string.append("}");

        return string.toString();
    }

    /**
     * Get a property from the given property container as int.
     *
     * @param propertyContainer to get property from.
     * @param key               key of the property.
     * @return value.
     * @throws ClassCastException if value isn't a number.
     * @throws org.neo4j.graphdb.NotFoundException
     *                            if the property doesn't exist.
     */
    public static int getInt(PropertyContainer propertyContainer, String key) {
        return getInt(propertyContainer.toString(), key, propertyContainer.getProperty(key));
    }

    /**
     * Get a property from the given property container as int.
     *
     * @param propertyContainer to get property from.
     * @param key               key of the property.
     * @param defaultValue      value returned if property does not exist.
     * @return value.
     * @throws ClassCastException if value isn't a number.
     */
    public static int getInt(PropertyContainer propertyContainer, String key, int defaultValue) {
        return getInt(propertyContainer.toString(), key, propertyContainer.getProperty(key, defaultValue));
    }

    /**
     * Get a property from the given property container as long.
     *
     * @param propertyContainer to get property from.
     * @param key               key of the property.
     * @return value.
     * @throws ClassCastException if value isn't a number.
     * @throws org.neo4j.graphdb.NotFoundException
     *                            if the property doesn't exist.
     */
    public static long getLong(PropertyContainer propertyContainer, String key) {
        return getLong(propertyContainer.toString(), key, propertyContainer.getProperty(key));
    }

    /**
     * Get a property from the given property container as long.
     *
     * @param propertyContainer to get property from.
     * @param key               key of the property.
     * @param defaultValue      value returned if property does not exist.
     * @return value.
     * @throws ClassCastException if value isn't a number.
     */
    public static long getLong(PropertyContainer propertyContainer, String key, long defaultValue) {
        return getLong(propertyContainer.toString(), key, propertyContainer.getProperty(key, defaultValue));
    }

    /**
     * Get a property from the given property container as float.
     *
     * @param propertyContainer to get property from.
     * @param key               key of the property.
     * @return value.
     * @throws ClassCastException if value isn't a number.
     * @throws org.neo4j.graphdb.NotFoundException
     *                            if the property doesn't exist.
     */
    public static float getFloat(PropertyContainer propertyContainer, String key) {
        return getFloat(propertyContainer.toString(), key, propertyContainer.getProperty(key));
    }

    /**
     * Get a property from the given property container as float.
     *
     * @param propertyContainer to get property from.
     * @param key               key of the property.
     * @param defaultValue      value returned if property does not exist.
     * @return value.
     * @throws ClassCastException if value isn't a number.
     */
    public static float getFloat(PropertyContainer propertyContainer, String key, float defaultValue) {
        return getFloat(propertyContainer.toString(), key, propertyContainer.getProperty(key, defaultValue));
    }

    private static int getInt(String pc, String key, Object value) {
        if (value instanceof Byte) {
            return ((Byte) value).intValue();
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Long) {
            return ((Long) value).intValue();
        }

        throw new ClassCastException(value + " is not a number! (" + pc + ", key=" + key + ")");
    }

    private static long getLong(String pc, String key, Object value) {
        if (value instanceof Byte) {
            return ((Byte) value).longValue();
        }

        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        throw new ClassCastException(value + " is not a number! (" + pc + ", key=" + key + ")");
    }

    private static float getFloat(String pc, String key, Object value) {
        if (value instanceof Byte) {
            return ((Byte) value).floatValue();
        }

        if (value instanceof Double) {
            return ((Double) value).floatValue();
        }


        if (value instanceof Integer) {
            return ((Integer) value).floatValue();
        }

        if (value instanceof Long) {
            return ((Long) value).floatValue();
        }

        if (value instanceof Float) {
            return (Float) value;
        }

        throw new ClassCastException(value + " is not a number! (" + pc + ", key=" + key + ")");
    }

    private PropertyContainerUtils() {
    }
}
