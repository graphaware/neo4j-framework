package com.graphaware.common.policy.spel;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Abstract base-class for {@link com.graphaware.common.policy.ObjectInclusionPolicy} implementations that are based on
 * SPEL expressions.
 */
public abstract class SpelInclusionPolicy {

    protected final Expression exp;

    protected SpelInclusionPolicy(String expression) {
        exp = new SpelExpressionParser().parseExpression(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "SpelInclusionPolicy{exp=" + exp + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpelInclusionPolicy that = (SpelInclusionPolicy) o;

        if (!exp.getExpressionString().equals(that.exp.getExpressionString())) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return exp.getExpressionString().hashCode();
    }
}
