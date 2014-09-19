package com.graphaware.common.policy.composite;

import com.graphaware.common.policy.PropertyInclusionPolicy;
import org.neo4j.graphdb.PropertyContainer;

import java.util.Arrays;

/**
 * {@link com.graphaware.common.policy.PropertyInclusionPolicy} composed of multiple other policies. All contained policies must "vote"
 * <code>true</code> to {@link #include(String, org.neo4j.graphdb.PropertyContainer)} in order for this policy to
 * return <code>true</code>.
 */
public abstract class CompositePropertyInclusionPolicy<T extends PropertyContainer> implements PropertyInclusionPolicy<T> {

    private final PropertyInclusionPolicy<T>[] policies;

    protected CompositePropertyInclusionPolicy(PropertyInclusionPolicy<T>[] policies) {
        if (policies == null || policies.length < 1) {
            throw new IllegalArgumentException("There must be at least one wrapped policy in composite policy");
        }
        this.policies = policies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(String key, T object) {
        for (PropertyInclusionPolicy<T> policy : policies) {
            if (!policy.include(key, object)) {
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

        CompositePropertyInclusionPolicy that = (CompositePropertyInclusionPolicy) o;

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
