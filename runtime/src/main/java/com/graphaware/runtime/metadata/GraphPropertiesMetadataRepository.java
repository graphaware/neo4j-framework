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

package com.graphaware.runtime.metadata;


import com.graphaware.common.kv.GraphKeyValueStore;
import com.graphaware.common.kv.KeyValueStore;
import com.graphaware.common.serialize.Serializer;
import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.config.util.InstanceRoleUtils;
import com.graphaware.runtime.module.RuntimeModule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link ModuleMetadataRepository} backed by a {@link org.neo4j.kernel.impl.core.GraphProperties}.
 */
public class GraphPropertiesMetadataRepository implements ModuleMetadataRepository {

    private static final Log LOG = LoggerFactory.getLogger(GraphPropertiesMetadataRepository.class);
    private final GraphDatabaseService database;
    private final KeyValueStore keyValueStore;
    private final String propertyPrefix;
    private final InstanceRoleUtils instanceRoleUtils;

    /**
     * Create a new repository.
     *
     * @param database       to back the repository.
     * @param configuration  of the runtime.
     * @param propertyPrefix String with which the property keys of properties written by this repository will be prefixed.
     */
    public GraphPropertiesMetadataRepository(GraphDatabaseService database, RuntimeConfiguration configuration, String propertyPrefix) {
        this.database = database;
        this.propertyPrefix = configuration.createPrefix(propertyPrefix);
        this.keyValueStore = new GraphKeyValueStore(database);
        this.instanceRoleUtils = new InstanceRoleUtils(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends ModuleMetadata> M getModuleMetadata(RuntimeModule module) {
        return getModuleMetadata(module.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends ModuleMetadata> M getModuleMetadata(String moduleId) {
        final String key = moduleKey(moduleId);

        Map<String, Object> internalProperties = getInternalProperties();

        try {
            byte[] serializedMetadata = (byte[]) internalProperties.get(key);

            if (serializedMetadata == null) {
                return null;
            }

            return Serializer.fromByteArray(serializedMetadata);
        } catch (Exception e) {
            removeModuleMetadata(moduleId);
            LOG.error("Could not deserialize metadata for module ID " + moduleId);
            throw new CorruptMetadataException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends ModuleMetadata> void persistModuleMetadata(RuntimeModule module, M metadata) {
        persistModuleMetadata(module.getId(), metadata);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends ModuleMetadata> void persistModuleMetadata(String moduleId, M metadata) {
    	if(instanceRoleUtils.getInstanceRole().isWritable()){

    		try (Transaction tx = database.beginTx()) {
    			keyValueStore.set(moduleKey(moduleId), Serializer.toByteArray(metadata));
    			tx.success();
    		}
    		
    	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAllModuleIds() {
        return getInternalProperties().keySet().stream().map(key -> key.replace(propertyPrefix, "")).collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeModuleMetadata(String moduleId) {
		if (instanceRoleUtils.getInstanceRole().isWritable()) {
			
			try (Transaction tx = database.beginTx()) {
				keyValueStore.remove(moduleKey(moduleId));
				tx.success();
			}
			
		}
    }

    /**
     * Get properties starting with {@link #propertyPrefix} from a {@link com.graphaware.common.kv.KeyValueStore}.
     *
     * @return map of properties (key-value).
     */
    private Map<String, Object> getInternalProperties() {
        Map<String, Object> result = new HashMap<>();

        try (Transaction tx = database.beginTx()) {
            for (String key : keyValueStore.getKeys()) {
                if (key.startsWith(propertyPrefix)) {
                    result.put(key, keyValueStore.get(key));
                }
            }
            tx.success();
        }

        return result;
    }

    /**
     * Build a module key to use as a property on the metadata container for storing metadata.
     *
     * @param moduleId to build a key for.
     * @return module key.
     */
    protected final String moduleKey(String moduleId) {
        return propertyPrefix + moduleId;
    }
}
