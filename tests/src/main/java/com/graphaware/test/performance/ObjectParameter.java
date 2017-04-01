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

package com.graphaware.test.performance;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * {@link Parameter} whose value is represented by any object.
 */
public class ObjectParameter<T> extends NamedParameter<T> {

    private final List<T> values = new LinkedList<>();

    /**
     * Construct a new parameter.
     *
     * @param name   of the parameter.
     * @param values of the parameter.
     */
    public ObjectParameter(String name, T... values) {
        super(name);
        this.values.addAll(Arrays.asList(values));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> getValues() {
        return values;
    }
}
