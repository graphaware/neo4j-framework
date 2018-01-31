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

package com.graphaware.lifecycle;

import java.util.Map;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.runtime.module.BaseRuntimeModuleBootstrapper;
import com.graphaware.runtime.module.RuntimeModule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;

/**
 * Bootstraps the {@link LifecyleModule} in server mode.
 */
public class LifecycleModuleBootstrapper extends BaseRuntimeModuleBootstrapper<LifecycleConfiguration> {


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected LifecycleConfiguration defaultConfiguration() {
		return LifecycleConfiguration.defaultConfiguration();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected RuntimeModule doBootstrapModule(String moduleId, Map<String, String> properties,
	                                          GraphDatabaseService database, LifecycleConfiguration config) {

		config.putAll(properties);
		config.scanForEvents();

		return new LifecyleModule(moduleId, database, config, config.getNodeScheduledEvents(),
				config.getRelationshipScheduledEvents(), config.getNodeCommitEvents(),
				config.getRelationshipCommitEvents(), config.getBatchSize());
	}
}
