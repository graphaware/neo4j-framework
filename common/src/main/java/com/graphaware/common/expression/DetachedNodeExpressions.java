/*
 * Copyright (c) 2013-2016 GraphAware
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

package com.graphaware.common.expression;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DetachedNodeExpressions<T extends SupportsDetachedNodeExpressions<?>> extends PropertyContainerExpressions<T> {

    public DetachedNodeExpressions(T node) {
        super(node);
    }

    public boolean hasLabel(String label) {
        return propertyContainer.hasLabel(label);
    }

    public String[] getLabels() {
        List<String> labels = new LinkedList<>();
        Collections.addAll(labels, propertyContainer.getLabels());
        return labels.toArray(new String[labels.size()]);
    }
}
