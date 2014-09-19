package com.graphaware.runtime.config.function;

import com.graphaware.common.policy.NodeInclusionPolicy;
import org.neo4j.graphdb.Node;

public class TestNodeInclusionPolicy implements NodeInclusionPolicy {

    private final String someField = "test";

    public TestNodeInclusionPolicy() {
    }

    @Override
    public boolean include(Node object) {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestNodeInclusionPolicy that = (TestNodeInclusionPolicy) o;

        if (!someField.equals(that.someField)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return someField.hashCode();
    }
}
