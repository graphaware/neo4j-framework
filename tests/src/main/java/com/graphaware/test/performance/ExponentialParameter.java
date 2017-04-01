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

import java.util.LinkedList;
import java.util.List;

/**
 * Exponential {@link Parameter} generating an integer.
 */
public class ExponentialParameter extends NamedParameter<Integer> {

    private final List<Integer> values = new LinkedList<>();

    /**
     * Create a new parameter.
     *
     * @param name         param name.
     * @param base         of the exponential.
     * @param exponentMin  minimum value of the exponent (inclusive).
     * @param exponentMax  maximum value of the exponent (inclusive).
     * @param exponentStep exponential increment.
     */
    public ExponentialParameter(String name, double base, double exponentMin, double exponentMax, double exponentStep) {
        super(name);

        for (double e = exponentMin; e <= exponentMax; e += exponentStep) {
            values.add((int) (Math.round(Math.pow(base, e))));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getValues() {
        return values;
    }
}
