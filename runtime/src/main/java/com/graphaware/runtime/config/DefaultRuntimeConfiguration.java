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

	private static final DefaultRuntimeConfiguration INSTANCE = new DefaultRuntimeConfiguration();

	private final ComponentFactory componentFactory;
	private final int activityThreshold;
    private final long defaultDelayMillis;
    private final long minimumDelayMillis;
    private final long maximumDelayMillis;

    /**
     * Retrieves the singleton instance of the {@link DefaultRuntimeConfiguration}.
     *
     * @return The {@link DefaultRuntimeConfiguration} instance
     */
    public static DefaultRuntimeConfiguration getInstance() {
        return INSTANCE;
    }

    private DefaultRuntimeConfiguration() {
    	this(10, 2000, 0, 10_000, new DefaultImplementationComponentFactory());
    }

	private DefaultRuntimeConfiguration(int activityThreshold, long defaultDelayMillis, long minimumDelayMillis, long maximumDelayMillis,
			ComponentFactory componentFactory) {
		this.activityThreshold = activityThreshold;
		this.defaultDelayMillis = defaultDelayMillis;
		this.minimumDelayMillis = minimumDelayMillis;
		this.maximumDelayMillis = maximumDelayMillis;
		this.componentFactory = componentFactory;
	}

	/**
	 * Returns a copy of this {@link DefaultRuntimeConfiguration} reconfigured to use the given database activity threshold.
	 *
	 * @see ScheduleConfiguration
	 * @param databaseActivityThreshold The new database activity threshold to use.
	 * @return A new {@link DefaultRuntimeConfiguration}.
	 */
	public DefaultRuntimeConfiguration withDatabaseActivityThreshold(int databaseActivityThreshold) {
		return new DefaultRuntimeConfiguration(databaseActivityThreshold, this.defaultDelayMillis, this.minimumDelayMillis, this.maximumDelayMillis, this.componentFactory);
	}

	/**
	 * Returns a copy of this {@link DefaultRuntimeConfiguration} reconfigured to use the specified default timer-driven
	 * module scheduling delay.
	 *
	 * @see ScheduleConfiguration
	 * @param defaultDelayMillis The new default scheduling delay in milliseconds.
	 * @return A new {@link DefaultRuntimeConfiguration}.
	 */
    public DefaultRuntimeConfiguration withDefaultDelayMillis(long defaultDelayMillis) {
    	return new DefaultRuntimeConfiguration(this.activityThreshold, defaultDelayMillis, this.minimumDelayMillis, this.maximumDelayMillis, this.componentFactory);
    }

	/**
	 * Returns a copy of this {@link DefaultRuntimeConfiguration} reconfigured to use the specified minimum delay between
	 * timer-driven module invocations.
	 *
	 * @see ScheduleConfiguration
	 * @param minimumDelayMillis The new minimum delay between timer-driven module invocations.
	 * @return A new {@link DefaultRuntimeConfiguration}.
	 */
    public DefaultRuntimeConfiguration withMinimumDelayMillis(long minimumDelayMillis) {
    	return new DefaultRuntimeConfiguration(this.activityThreshold, this.defaultDelayMillis, minimumDelayMillis, this.maximumDelayMillis, this.componentFactory);
    }

	/**
	 * Returns a copy of this {@link DefaultRuntimeConfiguration} reconfigured to use the specified maximum delay between
	 * timer-driven module invocations.
	 *
	 * @see ScheduleConfiguration
	 * @param maximumDelayMillis The new maximum delay between timer-driven module invocations.
	 * @return A new {@link DefaultRuntimeConfiguration}.
	 */
    public DefaultRuntimeConfiguration withMaximumDelayMillis(long maximumDelayMillis) {
    	return new DefaultRuntimeConfiguration(this.activityThreshold, this.defaultDelayMillis, this.minimumDelayMillis, maximumDelayMillis, this.componentFactory);
    }

    /**
     * Returns a copy of this {@link DefaultRuntimeConfiguration} reconfigured to use the specified {@link ComponentFactory}.
     *
     * @param componentFactory The {@link ComponentFactory} to use.
     * @return A new {@link DefaultRuntimeConfiguration}.
     */
    public DefaultRuntimeConfiguration withComponentFactory(ComponentFactory componentFactory) {
    	return new DefaultRuntimeConfiguration(this.activityThreshold, this.defaultDelayMillis, this.minimumDelayMillis, this.maximumDelayMillis, componentFactory);
    }

    @Override
    public long defaultDelayMillis() {
		return defaultDelayMillis;
    }

    @Override
    public long minimumDelayMillis() {
		return minimumDelayMillis;
    }

    @Override
    public long maximumDelayMillis() {
    	return maximumDelayMillis;
    }

    @Override
    public int databaseActivityThreshold() {
    	return activityThreshold; // number of transactions between timer-driven module invocations
    }

    @Override
    public ScheduleConfiguration getScheduleConfiguration() {
    	return this;
    }

	@Override
	public ComponentFactory getComponentFactory() {
		return componentFactory;
	}

}
