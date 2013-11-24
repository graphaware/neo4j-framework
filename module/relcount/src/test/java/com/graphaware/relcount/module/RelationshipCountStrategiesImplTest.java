/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.relcount.module;

import com.graphaware.relcount.compact.ThresholdBasedCompactionStrategy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Unit test for {@link com.graphaware.relcount.module.RelationshipCountStrategiesImpl}.
 */
public class RelationshipCountStrategiesImplTest {

    @Test
    public void sameStrategiesShouldProduceSameString() {
        assertEquals(RelationshipCountStrategiesImpl.defaultStrategies().with(new ThresholdBasedCompactionStrategy(2)).asString(),
                RelationshipCountStrategiesImpl.defaultStrategies().with(new ThresholdBasedCompactionStrategy(2)).asString());
    }

    @Test
    public void differentStrategiesShouldHaveADifferentHashCode() {
        assertNotSame(RelationshipCountStrategiesImpl.defaultStrategies().with(new ThresholdBasedCompactionStrategy(2)).asString(),
                RelationshipCountStrategiesImpl.defaultStrategies().with(new ThresholdBasedCompactionStrategy(3)).asString());
    }
}
