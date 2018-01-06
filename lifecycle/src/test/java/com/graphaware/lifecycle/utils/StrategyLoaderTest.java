/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.lifecycle.utils;

import static java.util.Collections.*;
import static org.junit.Assert.*;

import java.util.List;

import com.graphaware.lifecycle.strategy.AddRemoveLabels;
import com.graphaware.lifecycle.strategy.DeleteRelationship;
import com.graphaware.lifecycle.strategy.LifecycleEventStrategy;
import org.junit.Test;

public class StrategyLoaderTest {

	@Test
	public void shouldResolveListOfClassNames() throws Exception {
		StrategyLoader<? extends LifecycleEventStrategy> resolver = new StrategyLoader<>();
		List<String> classNames = singletonList("com.graphaware.lifecycle.strategy.AddRemoveLabels");
		List<? extends LifecycleEventStrategy> strategies = resolver.load(classNames);
		assertEquals(AddRemoveLabels.getInstance(), strategies.get(0));
	}

	@Test
	public void shouldResolveCommaSeparatedListOfClassNames() throws Exception {
		StrategyLoader<? extends LifecycleEventStrategy> resolver = new StrategyLoader<>();
		List<? extends LifecycleEventStrategy> strategies = resolver.load(
				"com.graphaware.lifecycle.strategy.AddRemoveLabels ,    com.graphaware.lifecycle" +
						".strategy.DeleteRelationship");
		assertEquals(AddRemoveLabels.getInstance(), strategies.get(0));
		assertEquals(DeleteRelationship.getInstance(), strategies.get(1));
	}
}