package com.graphaware.runtime.policy;

import com.graphaware.common.serialize.Serializer;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodeProperties;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;
import com.graphaware.runtime.policy.all.IncludeAllBusinessRelationshipProperties;
import com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class PolicySerializationTest {

    @Test
    public void singletonPoliciesShouldBeEqual() {
        assertEquals(IncludeAllBusinessNodes.getInstance(), serDeser(IncludeAllBusinessNodes.getInstance()));
        assertEquals(IncludeAllBusinessRelationships.getInstance(), serDeser(IncludeAllBusinessRelationships.getInstance()));
        assertEquals(IncludeAllBusinessNodeProperties.getInstance(), serDeser(IncludeAllBusinessNodeProperties.getInstance()));
        assertEquals(IncludeAllBusinessRelationshipProperties.getInstance(), serDeser(IncludeAllBusinessRelationshipProperties.getInstance()));
    }

    private <T> T serDeser(T t) {
        return Serializer.fromByteArray(Serializer.toByteArray(t));
    }
}
