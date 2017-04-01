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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.runtime.config.FluentRuntimeConfiguration;
import com.graphaware.runtime.config.FluentTxDrivenModuleConfiguration;
import com.graphaware.test.integration.cluster.HighAvailabilityClusterDatabasesIntegrationTest;

public class GraphPropertiesMetadataRepositoryTestHighAvailability extends HighAvailabilityClusterDatabasesIntegrationTest {

    private ModuleMetadataRepository repository;    
    
    @Test
    public void shouldPersistAndRetrieveMetadataMaster() {
    	GraphDatabaseService mainDatabase = getMainDatabase();
        
    	repository = new GraphPropertiesMetadataRepository(mainDatabase, FluentRuntimeConfiguration.defaultConfiguration(mainDatabase), "TEST-MASTER");

        ModuleMetadata metadata = new DefaultTxDrivenModuleMetadata(FluentTxDrivenModuleConfiguration.defaultConfiguration());

        repository.persistModuleMetadata("TEST-MASTER", metadata);

        assertEquals(metadata, repository.getModuleMetadata("TEST-MASTER"));
    }

    @Test
    public void shouldPersistAndRetrieveMetadataSlave() {
    	GraphDatabaseService mainDatabase = getOneSlaveDatabase();
    	
        repository = new GraphPropertiesMetadataRepository(mainDatabase, FluentRuntimeConfiguration.defaultConfiguration(mainDatabase), "TEST-SLAVE");

        ModuleMetadata metadata = new DefaultTxDrivenModuleMetadata(FluentTxDrivenModuleConfiguration.defaultConfiguration());

        repository.persistModuleMetadata("TEST-SLAVE", metadata);

        assertNull(repository.getModuleMetadata("TEST-SLAVE"));
    }
}
