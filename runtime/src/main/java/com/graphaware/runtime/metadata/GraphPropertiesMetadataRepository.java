/*
 * Copyright (c) 2015 GraphAware
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

package com.graphaware.runtime.metadata;

import com.graphaware.common.policy.ObjectInclusionPolicy;
import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.util.PropertyContainerUtils;
import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.module.RuntimeModule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.impl.core.NodeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.graphaware.runtime.config.RuntimeConfiguration.*;

/**
 * {@link ModuleMetadataRepository} backed by a {@link org.neo4j.kernel.impl.core.GraphProperties}.
 */
public class GraphPropertiesMetadataRepository implements ModuleMetadataRepository {

    private static final Logger LOG = LoggerFactory.getLogger(GraphPropertiesMetadataRepository.class);
    private final GraphDatabaseService database;
    private final PropertyContainer metadataPropertyContainer;
    private final String propertyPrefix;

    /**
     * Create a new repository.
     *
     * @param database       to back the repository.
     * @param configuration  of the runtime.
     * @param propertyPrefix String with which the property keys of properties written by this repository will be prefixed.
     */
    public GraphPropertiesMetadataRepository(GraphDatabaseService database, RuntimeConfiguration configuration, String propertyPrefix) {
        this.propertyPrefix = configuration.createPrefix(propertyPrefix);
        this.database = database;
        this.metadataPropertyContainer = (((GraphDatabaseAPI) database).getDependencyResolver().resolveDependency(NodeManager.class).getGraphProperties());
    }

    /**
     * Get the {@link PropertyContainer} against which to store all metadata.
     *
     * @return {@link PropertyContainer} on which metadata is stored, null if one doesn't exist.
     */
    protected PropertyContainer getOrCreateMetadataContainer() {
        if (!metadataPropertyContainer.hasProperty(GA_METADATA)) {
            try (Transaction tx = database.beginTx()) {
                metadataPropertyContainer.setProperty(GA_METADATA, true);
                tx.success();
            }
        }
        return metadataPropertyContainer;
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
        PropertyContainer container = getOrCreateMetadataContainer();

        Map<String, Object> internalProperties = getInternalProperties(container);

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
        getOrCreateMetadataContainer().setProperty(moduleKey(moduleId), Serializer.toByteArray(metadata));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAllModuleIds() {
        Set<String> result = new HashSet<>();
        for (String key : getInternalProperties(getOrCreateMetadataContainer()).keySet()) {
            result.add(key.replace(propertyPrefix, ""));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeModuleMetadata(String moduleId) {
        getOrCreateMetadataContainer().removeProperty(moduleKey(moduleId));
    }

    /**
     * Get properties starting with {@link #propertyPrefix} from a {@link PropertyContainer}.
     *
     * @param pc {@link PropertyContainer} to get properties from.
     * @return map of properties (key-value).
     */
    private Map<String, Object> getInternalProperties(PropertyContainer pc) {
        return PropertyContainerUtils.propertiesToMap(pc, new ObjectInclusionPolicy<String>() {
            @Override
            public boolean include(String s) {
                return s.startsWith(propertyPrefix);
            }
        });
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
