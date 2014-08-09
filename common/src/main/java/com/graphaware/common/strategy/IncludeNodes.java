package com.graphaware.common.strategy;

import com.graphaware.common.description.predicate.Predicate;
import com.graphaware.common.description.property.DetachedPropertiesDescription;
import com.graphaware.common.description.property.WildcardPropertiesDescription;
import org.neo4j.graphdb.Label;

import java.util.Collections;

/**
 * An implementation of {@link NodeInclusionStrategy} that is entirely configurable using its fluent interface.
 */
public class IncludeNodes extends BaseIncludeNodes<IncludeNodes> {

    /**
     * Get a node inclusion strategy that includes all nodes as the base-line for further configuration.
     * <p/>
     * Note that if you want to simply include all nodes, it is more efficient to use {@link IncludeAllNodes}.
     *
     * @return a strategy including all nodes.
     */
    public static IncludeNodes all() {
        return new IncludeNodes(null, new WildcardPropertiesDescription(Collections.<String, Predicate>emptyMap()));
    }

    /**
     * Create a new strategy.
     *
     * @param label                 that matching nodes must have, can be null for all labels.
     * @param propertiesDescription of the matching nodes.
     */
    protected IncludeNodes(Label label, DetachedPropertiesDescription propertiesDescription) {
        super(label, propertiesDescription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IncludeNodes newInstance(Label label) {
        return new IncludeNodes(label, getPropertiesDescription());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IncludeNodes newInstance(DetachedPropertiesDescription propertiesDescription) {
        return new IncludeNodes(getLabel(), propertiesDescription);
    }
}
