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

import com.graphaware.common.strategy.IncludeNoNodes;
import com.graphaware.relcount.compact.ThresholdBasedCompactionStrategy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Unit test for {@link RelationshipCountModule}. These miscellaneous tests, most of the core logic tests are in
 * {@link com.graphaware.relcount.count.RelationshipCountIntegrationTest}.
 */
public class RelationshipCountModuleTest {

    @Test
    public void sameConfigShouldProduceSameString() {
        RelationshipCountStrategiesImpl strategies = RelationshipCountStrategiesImpl.defaultStrategies();
        RelationshipCountModule module1 = new RelationshipCountModule(strategies.with(new ThresholdBasedCompactionStrategy(5)).with(IncludeNoNodes.getInstance()));
        RelationshipCountModule module2 = new RelationshipCountModule(strategies.with(new ThresholdBasedCompactionStrategy(5)).with(IncludeNoNodes.getInstance()));

        assertEquals(module1.asString(), module2.asString());
    }

    @Test
    public void differentConfigShouldProduceDifferentString() {
        RelationshipCountStrategiesImpl strategies = RelationshipCountStrategiesImpl.defaultStrategies();
        RelationshipCountModule module1 = new RelationshipCountModule(strategies.with(new ThresholdBasedCompactionStrategy(5)));
        RelationshipCountModule module2 = new RelationshipCountModule(strategies.with(new ThresholdBasedCompactionStrategy(6)));

        assertNotSame(module1.asString(), module2.asString());
    }
}
