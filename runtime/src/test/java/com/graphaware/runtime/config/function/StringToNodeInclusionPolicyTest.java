package com.graphaware.runtime.config.function;

import com.graphaware.common.policy.NodeInclusionPolicy;
import com.graphaware.common.policy.spel.SpelNodeInclusionPolicy;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;
import org.junit.Test;

import static com.graphaware.common.policy.composite.CompositeNodeInclusionPolicy.*;
import static org.junit.Assert.*;

/**
 * Unit test for {@link StringToNodeInclusionPolicy}.
 */
public class StringToNodeInclusionPolicyTest {

    @Test
    public void shouldConstructPolicyFromClassName() {
        NodeInclusionPolicy policy = StringToNodeInclusionPolicy.getInstance().apply("com.graphaware.runtime.config.function.SingletonNodeInclusionPolicy");

        assertNotNull(policy);
        assertEquals(of(IncludeAllBusinessNodes.getInstance(), SingletonNodeInclusionPolicy.getInstance()), policy);
    }

    @Test
    public void shouldConstructPolicyFromClassName2() {
        NodeInclusionPolicy policy = StringToNodeInclusionPolicy.getInstance().apply("com.graphaware.runtime.config.function.TestNodeInclusionPolicy");

        assertNotNull(policy);
        assertEquals(of(IncludeAllBusinessNodes.getInstance(), new TestNodeInclusionPolicy()), policy);
    }

    @Test
    public void shouldConstructSpelPolicy() {
        NodeInclusionPolicy policy = StringToNodeInclusionPolicy.getInstance().apply("hasLabel('Test')");

        assertEquals(of(IncludeAllBusinessNodes.getInstance(), new SpelNodeInclusionPolicy("hasLabel('Test')")), policy);
    }

    @Test
    public void shouldConstructSpelPolicy2() {
        NodeInclusionPolicy policy = StringToNodeInclusionPolicy.getInstance().apply("isType('R1')");

        assertEquals(of(IncludeAllBusinessNodes.getInstance(), new SpelNodeInclusionPolicy("isType('R1')")), policy);
    }

    @Test(expected = RuntimeException.class)
    public void uninstantiableClassCausesException() {
        StringToNodeInclusionPolicy.getInstance().apply("com.graphaware.runtime.config.function.WrongNodeInclusionPolicy");
    }
}
