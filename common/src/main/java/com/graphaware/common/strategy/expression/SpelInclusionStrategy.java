package com.graphaware.common.strategy.expression;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Abstract base-class for {@link com.graphaware.common.strategy.ObjectInclusionStrategy} implementations that are based on
 * SPEL expressions.
 */
public abstract class SpelInclusionStrategy {

    protected final Expression exp;

    protected SpelInclusionStrategy(String expression) {
        exp = new SpelExpressionParser().parseExpression(expression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpelInclusionStrategy that = (SpelInclusionStrategy) o;

        if (!exp.getExpressionString().equals(that.exp.getExpressionString())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return exp.getExpressionString().hashCode();
    }
}
