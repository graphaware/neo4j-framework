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

import com.graphaware.common.policy.inclusion.fluent.IncludeNodeProperties;
import com.graphaware.common.policy.inclusion.fluent.IncludeNodes;
import com.graphaware.common.policy.inclusion.fluent.IncludeRelationshipProperties;
import com.graphaware.common.policy.inclusion.fluent.IncludeRelationships;
import com.graphaware.runtime.config.FluentRuntimeConfiguration;
import com.graphaware.runtime.config.FluentTxDrivenModuleConfiguration;
import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Direction;

import static org.junit.Assert.assertEquals;

public class GraphPropertiesMetadataRepositoryTest extends EmbeddedDatabaseIntegrationTest {

    private ModuleMetadataRepository repository;

    @Test
    public void shouldPersistAndRetrieveMetadata1() {
        repository = new GraphPropertiesMetadataRepository(getDatabase(), FluentRuntimeConfiguration.defaultConfiguration(getDatabase()), "TEST");

        ModuleMetadata metadata = new DefaultTxDrivenModuleMetadata(FluentTxDrivenModuleConfiguration.defaultConfiguration());

        repository.persistModuleMetadata("TEST", metadata);

        assertEquals(metadata, repository.getModuleMetadata("TEST"));
    }

    @Test
    public void shouldPersistAndRetrieveMetadata2() {
        repository = new GraphPropertiesMetadataRepository(getDatabase(), FluentRuntimeConfiguration.defaultConfiguration(getDatabase()), "TEST");

        ModuleMetadata metadata = new DefaultTxDrivenModuleMetadata(FluentTxDrivenModuleConfiguration.defaultConfiguration()
                .with(IncludeNodes.all().with("TestLabel"))
                .with(IncludeRelationships.all().with(Direction.OUTGOING, "TEST_RELATIONSHIP"))
                .with(IncludeNodeProperties.all().with("testKey"))
                .with(IncludeRelationshipProperties.all().with("testKey")));

        repository.persistModuleMetadata("TEST", metadata);

        assertEquals(metadata, repository.getModuleMetadata("TEST"));
    }

}
