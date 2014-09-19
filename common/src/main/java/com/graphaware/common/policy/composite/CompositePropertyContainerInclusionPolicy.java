package com.graphaware.common.policy.composite;

import com.graphaware.common.policy.PropertyContainerInclusionPolicy;
import org.neo4j.graphdb.PropertyContainer;

import java.util.Arrays;

/**
 * {@link com.graphaware.common.policy.PropertyContainerInclusionPolicy} composed of multiple other policies.
 * All contained policies must "vote" <code>true</code> to {@link #include(org.neo4j.graphdb.PropertyContainer)} in
 * order for this policy to return <code>true</code>.
 */
public abstract class CompositePropertyContainerInclusionPolicy<P extends PropertyContainer, T extends PropertyContainerInclusionPolicy<P>> implements PropertyContainerInclusionPolicy<P> {

    protected final T[] policies;

    protected CompositePropertyContainerInclusionPolicy(T[] policies) {
        if (policies == null || policies.length < 1) {
            throw new IllegalArgumentException("There must be at least one wrapped policy in composite policy");
        }
        this.policies = policies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(P object) {
        for (T policy : policies) {
            if (!policy.include(object)) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositePropertyContainerInclusionPolicy that = (CompositePropertyContainerInclusionPolicy) o;

        if (!Arrays.equals(policies, that.policies)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(policies);
    }
}
