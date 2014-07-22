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

package com.graphaware.runtime.config;

import com.graphaware.runtime.bootstrap.ComponentFactory;
import com.graphaware.runtime.bootstrap.DefaultImplementationComponentFactory;

/**
 * Default {@link RuntimeConfiguration} for {@link com.graphaware.runtime.GraphAwareRuntime}. Singleton.
 */
public final class DefaultRuntimeConfiguration extends BaseRuntimeConfiguration implements ScheduleConfiguration {

    public static final DefaultRuntimeConfiguration INSTANCE = new DefaultRuntimeConfiguration();

    public static DefaultRuntimeConfiguration getInstance() {
        return INSTANCE;
    }

    private DefaultRuntimeConfiguration() {
    	// no instantiation permitted
    }

    @Override
    public long defaultDelayMillis() {
    	return 2000L;
    }

    @Override
    public long minimumDelayMillis() {
    	return 0L;
    }

    @Override
    public long maximumDelayMillis() {
    	return 10_000L;
    }

    @Override
    public int databaseActivityThreshold() {
    	return 10; // number of transactions between timer-driven module invocations
    }

    @Override
    public ScheduleConfiguration getScheduleConfiguration() {
    	return this;
    }

	@Override
	public ComponentFactory getComponentFactory() {
		return new DefaultImplementationComponentFactory();
	}

}
