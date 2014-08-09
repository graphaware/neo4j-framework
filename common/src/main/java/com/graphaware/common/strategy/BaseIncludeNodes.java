package com.graphaware.common.strategy;

import com.graphaware.common.description.property.DetachedPropertiesDescription;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.parboiled.common.StringUtils;

/**
 * Abstract base class for the most powerful (i.e., including properties) {@link NodeInclusionStrategy} implementations
 * with fluent interface.
 */
public abstract class BaseIncludeNodes<T extends BaseIncludeNodes<T>> extends IncludePropertyContainers<T, Node> implements NodeInclusionStrategy {

    private final Label label;

    /**
     * Create a new strategy.
     *
     * @param label                 that matching nodes must have, can be null for all labels.
     * @param propertiesDescription of the matching nodes.
     */
    public BaseIncludeNodes(Label label, DetachedPropertiesDescription propertiesDescription) {
        super(propertiesDescription);
        this.label = label;
    }

    /**
     * Create a new strategy from the current one, reconfigured to only match nodes with the given label.
     *
     * @param label that matching nodes must have, can be null for all labels.
     * @return reconfigured strategy.
     */
    public T with(String label) {
        if (label == null) {
            return with((Label) null);
        }

        if (StringUtils.isEmpty(label)) {
            throw new IllegalArgumentException("Empty labels are not supported"); //just because it's not a good idea and usually indicates a bug
        }

        return with(DynamicLabel.label(label));
    }

    /**
     * Create a new strategy from the current one, reconfigured to only match nodes with the given label.
     *
     * @param label that matching nodes must have, can be null for all labels.
     * @return reconfigured strategy.
     */
    public T with(Label label) {
        return newInstance(label);
    }

    /**
     * Create a new instance of this strategy with the given label.
     *
     * @param label of the new strategy.
     * @return new strategy.
     */
    protected abstract T newInstance(Label label);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Node node) {
        if (label != null && !node.hasLabel(label)) {
            return false;
        }

        return super.include(node);
    }

    /**
     * Get the label with which this strategy has been configured.
     *
     * @return label, can be <code>null</code> (representing any label incl. none).
     */
    public Label getLabel() {
        return label;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BaseIncludeNodes that = (BaseIncludeNodes) o;

        if (label != null ? !label.equals(that.label) : that.label != null) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }
}
