/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime.module;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.common.policy.inclusion.*;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.config.BaseModuleConfiguration;
import com.graphaware.runtime.config.ModuleConfiguration;
import com.graphaware.runtime.config.function.StringToNodeInclusionPolicy;
import com.graphaware.runtime.config.function.StringToNodePropertyInclusionPolicy;
import com.graphaware.runtime.config.function.StringToRelationshipInclusionPolicy;
import com.graphaware.runtime.config.function.StringToRelationshipPropertyInclusionPolicy;
import com.graphaware.runtime.manager.ModuleManager;
import org.apache.commons.configuration2.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;

import java.util.Map;

/**
 * Abstract base-class for {@link ModuleBootstrapper} implementations for {@link Module}s.
 *
 * @param <C> type of {@link ModuleConfiguration} used to configure the module.
 */
public abstract class BaseModuleBootstrapper<C extends BaseModuleConfiguration<C>> implements ModuleBootstrapper {

    private static final Log LOG = LoggerFactory.getLogger(BaseModuleBootstrapper.class);

    protected static final String NODE = "node";
    protected static final String NODE_PROPERTY = "node.property";
    protected static final String RELATIONSHIP = "relationship";
    protected static final String RELATIONSHIP_PROPERTY = "relationship.property";

    /**
     * Produce default configuration for the module.
     *
     * @return default config.
     */
    protected abstract C defaultConfiguration();

    /**
     * {@inheritDoc}
     */
    @Override
    public Module<?> bootstrapModule(String moduleId, Configuration config, GraphDatabaseService database, GraphAwareRuntime runtime) {
        C configuration = defaultConfiguration();

        configuration = configureInclusionPolicies(config, configuration);

        return doBootstrapModule(moduleId, config, database, configuration, runtime);
    }

    protected C configureInclusionPolicies(Configuration config, C configuration) {
        if (configExists(config, NODE)) {
            NodeInclusionPolicy policy = StringToNodeInclusionPolicy.getInstance().apply(config.getString(NODE));
            LOG.info("Node Inclusion Policy set to %s", policy);
            configuration = configuration.with(policy);
        }

        if (configExists(config, NODE_PROPERTY)) {
            NodePropertyInclusionPolicy policy = StringToNodePropertyInclusionPolicy.getInstance().apply(config.getString(NODE_PROPERTY));
            LOG.info("Node Property Inclusion Policy set to %s", policy);
            configuration = configuration.with(policy);
        }

        if (configExists(config, RELATIONSHIP)) {
            RelationshipInclusionPolicy policy = StringToRelationshipInclusionPolicy.getInstance().apply(config.getString(RELATIONSHIP));
            LOG.info("Relationship Inclusion Policy set to %s", policy);
            configuration = configuration.with(policy);
        }

        if (configExists(config, RELATIONSHIP_PROPERTY)) {
            RelationshipPropertyInclusionPolicy policy = StringToRelationshipPropertyInclusionPolicy.getInstance().apply(config.getString(RELATIONSHIP_PROPERTY));
            LOG.info("Relationship Property Inclusion Policy set to %s", policy);
            configuration = configuration.with(policy);
        }

        return configuration;
    }

    /**
     * Apply module-specific configuration to the provided configuration, which has already been configured with "initializeUntil"
     * and all {@link InclusionPolicies}. Then bootstrap the module and return it.
     *
     * @param moduleId      ID of the module.
     * @param config        for this module as key-value pairs.
     * @param database      which the module will run on.
     * @param configuration pre-populated with configuration common for all modules, such as "initializeUntil" and all {@link InclusionPolicies}.
     * @return fully configured runtime module.
     */
    protected abstract Module<?> doBootstrapModule(String moduleId, Configuration config, GraphDatabaseService database, C configuration, GraphAwareRuntime runtime);

    /**
     * Check if a configuration has been specified.
     *
     * @param config config passed in to the bootstrapper.
     * @param key    to check for.
     * @return true iff the passed in config contains the key and the value is not empty.
     */
    protected final boolean configExists(Configuration config, String key) {
        return config.containsKey(key) && config.getString(key).length() > 0;
    }
}
