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

import com.graphaware.runtime.config.NullRuntimeModuleConfiguration;
import com.graphaware.runtime.config.RuntimeModuleConfiguration;
import com.graphaware.runtime.module.BaseRuntimeModule;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link RuntimeModule} that can tell whether it has been initialized for testing.
 */
public class TestRuntimeModule extends BaseRuntimeModule<Void> {

    public static final List<TestRuntimeModule> TEST_RUNTIME_MODULES = new ArrayList<>();

    private final Map<String, String> config;

    public TestRuntimeModule(String moduleId, Map<String, String> config) {
        super(moduleId);
        this.config = config;
        TEST_RUNTIME_MODULES.add(this);
    }

    @Override
    public RuntimeModuleConfiguration getConfiguration() {
        return NullRuntimeModuleConfiguration.getInstance();
    }

    public Map<String, String> getConfig() {
        return config;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Void beforeCommit(ImprovedTransactionData transactionData) {
        return null;
    }
}
