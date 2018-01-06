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

package com.graphaware.lifecycle.strategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.graphaware.lifecycle.event.LifecycleEvent;
import org.neo4j.graphdb.PropertyContainer;

/**
 * An lifecycle strategy that delegates to 0..n child strategies. The purpose of this strategy is to:
 * <ul>
 * <li>Execute multiple strategies on expiry.</li>
 * <li>Execute user-defined strategies that exist on the class-path.</li>
 * </ul>
 *
 * @param <P>
 */
public class CompositeStrategy<P extends PropertyContainer> extends LifecycleEventStrategy<P> {

	List<? extends LifecycleEventStrategy<P>> strategies;

	public CompositeStrategy(List<? extends LifecycleEventStrategy<P>> strategies) {
		if (strategies != null) {
			this.strategies = strategies;
		} else {
			this.strategies = Collections.emptyList();
		}
	}

	@Override
	public boolean applyIfNeeded(P pc, LifecycleEvent event) {
		boolean allApplied = true;
		for (LifecycleEventStrategy<P> strategy : strategies) {
			boolean expired = strategy.applyIfNeeded(pc, event);
			if (!expired) {
				allApplied = false;
			}
		}
		return allApplied;
	}

	@Override
	public void setConfig(Map<String, String> config) {
		super.setConfig(config);
		for (LifecycleEventStrategy<P> strategy : strategies) {
			strategy.setConfig(config);
		}
	}
}
