package com.graphaware.common.strategy.expression;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Abstract base-class for {@link com.graphaware.common.strategy.InclusionStrategy} implementations that are based on
 * SPEL expressions.
 */
public abstract class SpelInclusionStrategy {

    protected final Expression exp;

    protected SpelInclusionStrategy(String expression) {
        exp = new SpelExpressionParser().parseExpression(expression);
    }
}
