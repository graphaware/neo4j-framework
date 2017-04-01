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

package com.graphaware.runtime.bootstrap;

import com.graphaware.runtime.config.NullTxDrivenModuleConfiguration;
import com.graphaware.runtime.config.TxDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.TxDrivenModuleMetadata;
import com.graphaware.runtime.module.BaseTxDrivenModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link com.graphaware.runtime.module.TxDrivenModule} that can tell whether it has been initialized for testing.
 */
public class TestRuntimeModule extends BaseTxDrivenModule<Void> {

    public static final List<TestRuntimeModule> TEST_RUNTIME_MODULES = new ArrayList<>();

    private final Map<String, String> config;
    private boolean initialized = false;

    public TestRuntimeModule(String moduleId, Map<String, String> config) {
        super(moduleId);
        this.config = config;
        TEST_RUNTIME_MODULES.add(this);
    }

    @Override
    public TxDrivenModuleConfiguration getConfiguration() {
        return NullTxDrivenModuleConfiguration.getInstance();
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public boolean isInitialized() {
        return initialized;
    }


    @Override
    public void initialize(GraphDatabaseService database) {
        try (Transaction tx = database.beginTx()){
            Node n1 = database.createNode(Label.label("test"));
            Node n2 = database.createNode();
            n1.createRelationshipTo(n2, RelationshipType.withName("TEST"));
            n1.getRelationships().iterator().next().getType();
            Thread.sleep(200); //takes some time to initialize
            tx.success();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        initialized = true;
    }

    @Override
    public void reinitialize(GraphDatabaseService database, TxDrivenModuleMetadata oldMetadata) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutdown() {
        initialized = false;
    }

    @Override
    public Void beforeCommit(ImprovedTransactionData transactionData) {
        return null;
    }
}
