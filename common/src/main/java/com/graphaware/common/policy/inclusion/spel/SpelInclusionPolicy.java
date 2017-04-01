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

package com.graphaware.common.policy.inclusion.spel;

import com.graphaware.common.policy.inclusion.ObjectInclusionPolicy;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Abstract base-class for {@link ObjectInclusionPolicy} implementations that are based on
 * SPEL expressions.
 */
public abstract class SpelInclusionPolicy {

    protected transient final Expression exp;
    protected transient final SpelNode expressionNode;

    private final String expression;

    protected SpelInclusionPolicy(String expression) {
        SpelExpressionParser parser = new SpelExpressionParser();
        this.expression = expression;
        this.expressionNode = parser.parseRaw(expression).getAST();
        this.exp = parser.parseExpression(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return expression;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpelInclusionPolicy that = (SpelInclusionPolicy) o;

        if (!expression.equals(that.expression)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return expression.hashCode();
    }
}
