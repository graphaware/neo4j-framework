package com.graphaware.common.strategy;

import com.graphaware.common.description.predicate.Predicate;
import com.graphaware.common.description.property.DetachedPropertiesDescription;
import com.graphaware.common.description.property.LiteralPropertiesDescription;
import org.neo4j.graphdb.PropertyContainer;

/**
 * An abstract base-class for {@link PropertyContainerInclusionStrategy} implementations that are based on property
 * value {@link Predicate}s. In other words, the implementations can be used to specify, which {@link PropertyContainer}s
 * to include based on the presence and/or value of their properties.
 */
public abstract class IncludePropertyContainers<C extends IncludePropertyContainers<?, T>, T extends PropertyContainer> implements PropertyContainerInclusionStrategy<T> {

    private final DetachedPropertiesDescription propertiesDescription;

    /**
     * Construct a new strategy.
     *
     * @param propertiesDescription description of properties an included {@link PropertyContainer} must fulfill.
     *                              More precisely, a {@link LiteralPropertiesDescription} of the {@link PropertyContainer}
     *                              must be more specific than the given properties description.
     */
    protected IncludePropertyContainers(DetachedPropertiesDescription propertiesDescription) {
        this.propertiesDescription = propertiesDescription;
    }

    /**
     * Construct a new description from this description by adding/replacing a predicate with a new one.
     *
     * @param propertyKey key of the property the predicate is for.
     * @param predicate   the predicate.
     * @return a new instance of properties description.
     */
    public C with(String propertyKey, Predicate predicate) {
        return newInstance(propertiesDescription.with(propertyKey, predicate));
    }

    /**
     * Create a new instance of this strategy reconfigured with a different properties description.
     *
     * @param propertiesDescription of the new instance.
     * @return new instance.
     */
    protected abstract C newInstance(DetachedPropertiesDescription propertiesDescription);

    /**
     * Get the properties description with which this strategy has been configured.
     *
     * @return properties description.
     */
    public DetachedPropertiesDescription getPropertiesDescription() {
        return propertiesDescription;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(T propertyContainer) {
        return new LiteralPropertiesDescription(propertyContainer).isMoreSpecificThan(propertiesDescription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IncludePropertyContainers that = (IncludePropertyContainers) o;

        if (!propertiesDescription.equals(that.propertiesDescription)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return propertiesDescription.hashCode();
    }
}
