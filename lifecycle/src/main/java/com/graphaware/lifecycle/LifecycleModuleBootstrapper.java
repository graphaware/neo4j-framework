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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.lifecycle.config.LifecycleConfiguration;
import com.graphaware.lifecycle.strategy.*;
import com.graphaware.lifecycle.utils.StrategyLoader;
import com.graphaware.runtime.module.BaseRuntimeModuleBootstrapper;
import com.graphaware.runtime.module.RuntimeModule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.logging.Log;

/**
 * Bootstraps the {@link LifecyleModule} in server mode.
 */
public class LifecycleModuleBootstrapper extends BaseRuntimeModuleBootstrapper<LifecycleConfiguration> {

	private static final Log LOG = LoggerFactory.getLogger(LifecycleModuleBootstrapper.class);

	private static final String NODE_EXPIRATION_INDEX = "nodeExpirationIndex";
	private static final String RELATIONSHIP_EXPIRATION_INDEX = "relationshipExpirationIndex";
	private static final String NODE_EXPIRATION_PROPERTY = "nodeExpirationProperty";
	private static final String RELATIONSHIP_EXPIRATION_PROPERTY = "relationshipExpirationProperty";
	private static final String NODE_REVIVAL_PROPERTY = "nodeRevivalProperty";
	private static final String RELATIONSHIP_REVIVAL_PROPERTY = "relationshipRevivalProperty";
	private static final String NODE_TTL_PROPERTY = "nodeTtlProperty";
	private static final String RELATIONSHIP_TTL_PROPERTY = "relationshipTtlProperty";
	private static final String NODE_EXPIRATION_STRATEGY = "nodeExpirationStrategy";
	private static final String RELATIONSHIP_EXPIRATION_STRATEGY = "relationshipExpirationStrategy";
	private static final String NODE_REVIVAL_STRATEGY = "nodeRevivalStrategy";
	private static final String RELATIONSHIP_REVIVAL_STRATEGY = "relationshipRevivalStrategy";
	private static final String MAX_NO_EXPIRATIONS = "maxExpirations";

	/**
	 * Expiry date is evaluated as expiry field + offset in ms.
	 */
	private static final String EXPIRY_OFFSET = "expiryOffset";

	/**
	 * Revival date is evaluated as expiry field + offset in ms.
	 */
	private static final String REVIVAL_OFFSET = "revivalOffset";

	private static final String FORCE_DELETE = "force";
	private static final String ORPHAN_DELETE = "orphan";
	private static final String DELETE_REL = "delete";
	private static final String COMPOSITE = "composite\\((.*?)\\)";

	private StrategyLoader<? extends LifecycleEventStrategy<Node>> nodeStrategyLoader = new StrategyLoader<>();
	private StrategyLoader<? extends LifecycleEventStrategy<Relationship>> relStrategyLoader = new StrategyLoader<>();

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
	                                          GraphDatabaseService database, LifecycleConfiguration configuration) {

		configuration = withNodeExpirationIndex(properties, configuration);
		configuration = withRelationshipExpirationIndex(properties, configuration);
		configuration = withNodeExpirationProperty(properties, configuration);
		configuration = withRelationshipExpirationProperty(properties, configuration);
		configuration = withNodeTtlProperty(properties, configuration);
		configuration = withRelationshipTtlProperty(properties, configuration);
		configuration = withNodeRevivalProperty(properties, configuration);
		configuration = withRelationshipRevivalProperty(properties, configuration);
		configuration = withNodeExpirationStrategy(properties, configuration);
		configuration = withRelationshipExpirationStrategy(properties, configuration);
		configuration = withNodeRevivalStrategy(properties, configuration);
		configuration = withRelationshipRevivalStrategy(properties, configuration);
		configuration = withMaxNoExpirations(properties, configuration);
		configuration = withExpiryOffset(properties, configuration);
		configuration = withRevivalOffset(properties, configuration);

		return new LifecyleModule(moduleId, database, configuration, configuration.scheduledEvents(),
				configuration.commitEvents(), configuration.getMaxNoExpirations());
	}

	private LifecycleConfiguration withNodeExpirationIndex(Map<String, String> properties, LifecycleConfiguration configuration) {
		if (configExists(properties, NODE_EXPIRATION_INDEX)) {
			String nodeExpirationIndex = properties.get(NODE_EXPIRATION_INDEX);
			LOG.info("Node expiration index set to %s", nodeExpirationIndex);
			configuration = configuration.withNodeExpirationIndex(nodeExpirationIndex);
		}
		return configuration;
	}

	private LifecycleConfiguration withRelationshipExpirationIndex(Map<String, String> properties, LifecycleConfiguration configuration) {
		if (configExists(properties, RELATIONSHIP_EXPIRATION_INDEX)) {
			String relationshipExpirationIndex = properties.get(RELATIONSHIP_EXPIRATION_INDEX);
			LOG.info("Relationship expiration index set to %s", relationshipExpirationIndex);
			configuration = configuration.withRelationshipExpirationIndex(relationshipExpirationIndex);
		}
		return configuration;
	}

	private LifecycleConfiguration withNodeExpirationProperty(Map<String, String> properties, LifecycleConfiguration configuration) {
		if (configExists(properties, NODE_EXPIRATION_PROPERTY)) {
			String nodeExpirationProperty = properties.get(NODE_EXPIRATION_PROPERTY);
			LOG.info("Node expiration property set to %s", nodeExpirationProperty);
			configuration = configuration.withNodeExpirationProperty(nodeExpirationProperty);
		}
		return configuration;
	}

	private LifecycleConfiguration withRelationshipExpirationProperty(Map<String, String> properties, LifecycleConfiguration configuration) {
		if (configExists(properties, RELATIONSHIP_EXPIRATION_PROPERTY)) {
			String relationshipExpirationProperty = properties.get(RELATIONSHIP_EXPIRATION_PROPERTY);
			LOG.info("Relationship expiration property set to %s", relationshipExpirationProperty);
			configuration = configuration
					.withRelationshipExpirationProperty(relationshipExpirationProperty);
		}
		return configuration;
	}

	private LifecycleConfiguration withNodeTtlProperty(Map<String, String> properties, LifecycleConfiguration configuration) {
		if (configExists(properties, NODE_TTL_PROPERTY)) {
			String nodeTtlProperty = properties.get(NODE_TTL_PROPERTY);
			LOG.info("Node ttl property set to %s", nodeTtlProperty);
			configuration = configuration.withNodeTtlProperty(nodeTtlProperty);
		}
		return configuration;
	}

	private LifecycleConfiguration withRelationshipTtlProperty(Map<String, String> properties, LifecycleConfiguration configuration) {
		if (configExists(properties, RELATIONSHIP_TTL_PROPERTY)) {
			String relationshipTtlProperty = properties.get(RELATIONSHIP_TTL_PROPERTY);
			LOG.info("Relationship ttl property set to %s", relationshipTtlProperty);
			configuration = configuration.withRelationshipTtlProperty(relationshipTtlProperty);
		}
		return configuration;
	}

	private LifecycleConfiguration withNodeRevivalProperty(Map<String, String> properties, LifecycleConfiguration configuration) {
		if (configExists(properties, NODE_REVIVAL_PROPERTY)) {
			String nodeRevivalProperty = properties.get(NODE_REVIVAL_PROPERTY);
			LOG.info("Node revival property set to %s", nodeRevivalProperty);
			configuration = configuration.withNodeRevivalProperty(nodeRevivalProperty);
		}
		return configuration;
	}

	private LifecycleConfiguration withRelationshipRevivalProperty(Map<String, String> properties, LifecycleConfiguration configuration) {
		if (configExists(properties, RELATIONSHIP_REVIVAL_PROPERTY)) {
			String relationshipRevivalProperty = properties.get(RELATIONSHIP_REVIVAL_PROPERTY);
			LOG.info("Relationship revival property set to %s", relationshipRevivalProperty);
			configuration = configuration.withRelationshipRevivalProperty(relationshipRevivalProperty);
		}
		return configuration;
	}


	private LifecycleConfiguration withNodeExpirationStrategy(Map<String, String> properties, LifecycleConfiguration configuration) {
		if (configExists(properties, NODE_EXPIRATION_STRATEGY)) {
			String strategy = properties.get(NODE_EXPIRATION_STRATEGY);
			LOG.info("Node expiration strategy set to %s", strategy);
			Matcher matcher = Pattern.compile(COMPOSITE).matcher(strategy);
			if (FORCE_DELETE.equals(strategy)) {
				DeleteNodeAndRelationships instance = DeleteNodeAndRelationships.getInstance();
				instance.setConfig(properties);
				configuration = configuration.withNodeExpirationStrategy(instance);
			} else if (ORPHAN_DELETE.endsWith(strategy)) {
				DeleteOrphanedNodeOnly instance = DeleteOrphanedNodeOnly.getInstance();
				instance.setConfig(properties);
				configuration = configuration.withNodeExpirationStrategy(instance);
			} else if (matcher.find()) {
				List<? extends LifecycleEventStrategy<Node>> list = nodeStrategyLoader.load(matcher.group(1));
				CompositeStrategy<Node> instance = new CompositeStrategy<>(list);
				instance.setConfig(properties);
				configuration = configuration.withNodeExpirationStrategy(instance);
			} else {
				LOG.error("Not a valid node expiration strategy: %s", strategy);
				throw new IllegalArgumentException(String.format("Not a valid expiration strategy: %s",
						strategy));
			}
		}
		return configuration;
	}

	private LifecycleConfiguration withRelationshipExpirationStrategy(Map<String, String> properties, LifecycleConfiguration configuration) {
		if (configExists(properties, RELATIONSHIP_EXPIRATION_STRATEGY)) {
			String strategy = properties.get(RELATIONSHIP_EXPIRATION_STRATEGY);
			LOG.info("Relationship expiration strategy set to %s", strategy);
			Matcher composite = Pattern.compile(COMPOSITE).matcher(strategy);
			if (DELETE_REL.endsWith(strategy)) {
				DeleteRelationship instance = DeleteRelationship.getInstance();
				instance.setConfig(properties);
				configuration = configuration.withRelationshipExpirationStrategy(instance);
			} else if (composite.find()) {
				List<? extends LifecycleEventStrategy<Relationship>> list = relStrategyLoader.load(composite.group(1));
				CompositeStrategy<Relationship> instance = new CompositeStrategy<>(list);
				instance.setConfig(properties);
				configuration = configuration.withRelationshipExpirationStrategy(instance);
			} else {
				LOG.error("Not a valid relationship expiration strategy: %s", strategy);
				throw new IllegalArgumentException(String.format("Not a valid expiration strategy: %s",
						strategy));
			}
		}
		return configuration;
	}

	private LifecycleConfiguration withNodeRevivalStrategy(Map<String, String> properties, LifecycleConfiguration configuration) {
		if (configExists(properties, NODE_REVIVAL_STRATEGY)) {
			String strategy = properties.get(NODE_REVIVAL_STRATEGY);
			LOG.info("Node revival strategy set to %s", strategy);
			Matcher composite = Pattern.compile(COMPOSITE).matcher(strategy);
			if (composite.find()) {
				List<? extends LifecycleEventStrategy<Node>> list = nodeStrategyLoader.load(composite.group(1));
				CompositeStrategy<Node> instance = new CompositeStrategy<>(list);
				instance.setConfig(properties);
				configuration = configuration.withNodeRevivalStrategy(instance);
			} else {
				LOG.error("Not a valid node revival strategy: %s", strategy);
				throw new IllegalArgumentException(String.format("Not a valid expiration strategy: %s",
						strategy));
			}
		}
		return configuration;
	}

	private LifecycleConfiguration withRelationshipRevivalStrategy(Map<String, String> properties, LifecycleConfiguration configuration) {
		if (configExists(properties, RELATIONSHIP_REVIVAL_STRATEGY)) {
			String strategy = properties.get(RELATIONSHIP_REVIVAL_STRATEGY);
			LOG.info("Relationship revival strategy set to %s", strategy);
			Matcher composite = Pattern.compile(COMPOSITE).matcher(strategy);
			if (composite.find()) {
				List<? extends LifecycleEventStrategy<Relationship>> list = relStrategyLoader.load(composite.group(1));
				CompositeStrategy<Relationship> instance = new CompositeStrategy<>(list);
				instance.setConfig(properties);
				configuration = configuration.withRelationshipRevivalStrategy(instance);
			} else {
				LOG.error("Not a valid relationship revival strategy: %s", strategy);
				throw new IllegalArgumentException(String.format("Not a valid expiration strategy: %s",
						strategy));
			}
		}
		return configuration;
	}


	private LifecycleConfiguration withMaxNoExpirations(Map<String, String> properties, LifecycleConfiguration configuration) {
		if (configExists(properties, MAX_NO_EXPIRATIONS)) {
			String maxNoExpirations = properties.get(MAX_NO_EXPIRATIONS);
			LOG.info("Max number of expirations set to %s", maxNoExpirations);
			configuration = configuration.withMaxNoExpirations(Integer.valueOf(maxNoExpirations));
		}
		return configuration;
	}

	private LifecycleConfiguration withExpiryOffset(Map<String, String> properties, LifecycleConfiguration configuration) {
		if (configExists(properties, EXPIRY_OFFSET)) {
			String expiryOffset = properties.get(EXPIRY_OFFSET);
			LOG.info("Expiry offset (ms) set to %s", expiryOffset);
			configuration = configuration.withExpiryOffset(Long.valueOf(expiryOffset));
		}
		return configuration;
	}

	private LifecycleConfiguration withRevivalOffset(Map<String, String> properties, LifecycleConfiguration configuration) {
		if (configExists(properties, REVIVAL_OFFSET)) {
			String revivalOffset = properties.get(REVIVAL_OFFSET);
			LOG.info("Revival offset (ms) set to %s", revivalOffset);
			configuration = configuration.withRevivalOffset(Long.valueOf(revivalOffset));
		}
		return configuration;
	}
}
