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

package com.graphaware.common.description.predicate;

import com.graphaware.common.description.MutuallyExclusive;
import com.graphaware.common.description.PartiallyComparable;

/**
 * A predicate on beta, i.e. the a property value.
 */
public interface Predicate extends PartiallyComparable<Predicate>, MutuallyExclusive<Predicate> {

    /**
     * Evaluate this predicate for beta, i.e., a property value.
     *
     * @param beta to evaluate this predicate for.
     * @return evaluation of this predicate for beta.
     */
    boolean evaluate(Object beta);
}
