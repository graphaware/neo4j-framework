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

package com.graphaware.framework.strategy;

import com.graphaware.common.strategy.IncludeAllNodeProperties;
import com.graphaware.common.strategy.InclusionStrategiesImpl;
import com.graphaware.common.strategy.IncludeNoNodeProperties;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Unit test for {@link com.graphaware.common.strategy.InclusionStrategiesImpl}.
 */
public class InclusionStrategiesImplTest {

    @Test
    public void sameStrategiesShouldReturnSameString() {
        assertEquals(InclusionStrategiesImpl.all().asString(), InclusionStrategiesImpl.all().asString());
        assertEquals(InclusionStrategiesImpl.all().with(IncludeAllNodeProperties.getInstance()).asString(), InclusionStrategiesImpl.all().asString());
    }

    @Test
    public void differentStrategiesShouldReturnDifferentString() {
        assertNotSame(InclusionStrategiesImpl.all().asString(), InclusionStrategiesImpl.none().asString());
        assertNotSame(InclusionStrategiesImpl.all().asString(), InclusionStrategiesImpl.all().with(IncludeNoNodeProperties.getInstance()).asString());
    }
}
