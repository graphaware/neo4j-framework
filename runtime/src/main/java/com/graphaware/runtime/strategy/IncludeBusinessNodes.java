package com.graphaware.runtime.strategy;

import com.graphaware.common.description.predicate.Predicate;
import com.graphaware.common.description.property.DetachedPropertiesDescription;
import com.graphaware.common.description.property.WildcardPropertiesDescription;
import com.graphaware.common.strategy.BaseIncludeNodes;
import com.graphaware.runtime.config.RuntimeConfiguration;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.Collections;

/**
 * An implementation of {@link com.graphaware.common.strategy.NodeInclusionStrategy} that is entirely configurable using
 * its fluent interface and never includes nodes internal to the framework and/or {@link com.graphaware.runtime.GraphAwareRuntime}.
 */
public class IncludeBusinessNodes extends BaseIncludeNodes<IncludeBusinessNodes> {

    /**
     * Get a node inclusion strategy that includes all business nodes as the base-line for further configuration.
     * <p/>
     * Note that if you want to simply include all business nodes, it is more efficient to use {@link IncludeAllBusinessNodes}.
     *
     * @return a strategy including all nodes.
     */
    public static IncludeBusinessNodes all() {
        return new IncludeBusinessNodes(null, new WildcardPropertiesDescription(Collections.<String, Predicate>emptyMap()));
    }

    /**
     * Create a new strategy.
     *
     * @param label                 that matching nodes must have, can be null for all labels.
     * @param propertiesDescription of the matching nodes.
     */
    protected IncludeBusinessNodes(Label label, DetachedPropertiesDescription propertiesDescription) {
        super(label, propertiesDescription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IncludeBusinessNodes newInstance(Label label) {
        return new IncludeBusinessNodes(label, getPropertiesDescription());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IncludeBusinessNodes newInstance(DetachedPropertiesDescription propertiesDescription) {
        return new IncludeBusinessNodes(getLabel(), propertiesDescription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Node node) {
        for (Label label : node.getLabels()) {
            if (label.name().startsWith(RuntimeConfiguration.GA_PREFIX)) {
                return false;
            }
        }

        return super.include(node);
    }
}
