/*
 * Copyright (c) 2013-2017 GraphAware
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
package com.graphaware.runtime.schedule;

import com.graphaware.common.policy.role.InstanceRolePolicy;
import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.runtime.config.TimerDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.PositionNotFoundException;
import com.graphaware.runtime.metadata.TimerDrivenModuleContext;
import com.graphaware.runtime.module.RunCountingTimerDrivenModule;
import com.graphaware.runtime.module.TimerDrivenModule;

class MockTimerModuleContext implements TimerDrivenModuleContext<String>{

	@Override
	public long earliestNextCall() {
		return 0;
	}

	@Override
	public String find(GraphDatabaseService database) throws PositionNotFoundException {
		return "mock";
	}
	
	public static TimerDrivenModule<MockTimerModuleContext> buildModule(final InstanceRolePolicy policy) {
		TimerDrivenModuleConfiguration config = () -> policy;
		@SuppressWarnings("unchecked")
		TimerDrivenModule<MockTimerModuleContext> module = new RunCountingTimerDrivenModule(config);
		return module;
	}
}
