package com.graphaware.neo4j.framework.strategy;

import com.graphaware.neo4j.tx.event.strategy.IncludeNoNodeProperties;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 *  Unit test for {@link InclusionStrategiesImpl}.
 */
public class InclusionStrategiesImplTest {

    @Test
    public void sameStrategiesShouldHaveSameHashCode() {
        assertEquals(InclusionStrategiesImpl.all().hashCode(), InclusionStrategiesImpl.all().hashCode());
        assertEquals(InclusionStrategiesImpl.all().with(IncludeAllNodeProperties.getInstance()).hashCode(), InclusionStrategiesImpl.all().hashCode());
    }

    @Test
    public void differentStrategiesShouldHaveDifferentHashCode() {
        assertNotSame(InclusionStrategiesImpl.all().hashCode(), InclusionStrategiesImpl.none().hashCode());
        assertNotSame(InclusionStrategiesImpl.all().hashCode(), InclusionStrategiesImpl.all().with(IncludeNoNodeProperties.getInstance()).hashCode());
    }
}
