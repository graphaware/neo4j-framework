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

package com.graphaware.runtime.bootstrap;

import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.config.ModuleConfiguration;
import com.graphaware.runtime.config.NullModuleConfiguration;
import com.graphaware.runtime.module.BaseModule;
import com.graphaware.runtime.module.Module;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.apache.commons.configuration2.Configuration;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

/**
 * {@link Module} that can tell whether it has been initialized for testing.
 */
public class WritingModule extends BaseModule<Void> {

    private final Configuration config;

    public WritingModule(String moduleId, Configuration config) {
        super(moduleId);
        this.config = config;
    }

    @Override
    public ModuleConfiguration getConfiguration() {
        return NullModuleConfiguration.getInstance();
    }

    public Configuration getConfig() {
        return config;
    }

    @Override
    public Void beforeCommit(ImprovedTransactionData transactionData) {
        transactionData.mutationsToStrings().forEach(System.out::println);
        return null;
    }

    @Override
    public void start(GraphAwareRuntime runtime) {
        super.start(runtime);

        try (Transaction tx = runtime.getDatabase().beginTx()) {
            Node node = tx.createNode();
            node.setProperty("test", "init");
            tx.commit();
        }
    }
}
