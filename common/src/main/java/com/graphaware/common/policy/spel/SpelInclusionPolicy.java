package com.graphaware.common.policy.spel;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Abstract base-class for {@link com.graphaware.common.policy.ObjectInclusionPolicy} implementations that are based on
 * SPEL expressions.
 */
public abstract class SpelInclusionPolicy {

    protected transient final Expression exp;
    private final String expression;

    protected SpelInclusionPolicy(String expression) {
        this.expression = expression;
        this.exp = new SpelExpressionParser().parseExpression(expression);
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
