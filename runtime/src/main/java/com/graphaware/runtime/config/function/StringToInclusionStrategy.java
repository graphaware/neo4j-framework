package com.graphaware.runtime.config.function;

import com.graphaware.common.strategy.InclusionStrategy;
import org.neo4j.helpers.Function;

import java.util.regex.Pattern;

/**
 * A {@link org.neo4j.helpers.Function} that converts String to {@link InclusionStrategy}.
 * <p/>
 * Converts a fully qualified class name to an instance of the class, or a SPEL expression to {@link com.graphaware.common.strategy.ObjectInclusionStrategy}.
 */
public abstract class StringToInclusionStrategy<T extends InclusionStrategy> implements Function<String, T> {

    private static final Pattern CLASS_NAME_REGEX = Pattern.compile("([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)*[\\p{L}_$][\\p{L}\\p{N}_$]*");

    /**
     * {@inheritDoc}
     */
    @Override
    public T apply(String s) {
        if (CLASS_NAME_REGEX.matcher(s).matches()) {
            try {
                return (T) Class.forName(s).newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return spelStrategy(s);
    }

    /**
     * Instantiate a new SPEL-bases strategy.
     *
     * @param spel expression.
     * @return strategy.
     */
    protected abstract T spelStrategy(String spel);
}
