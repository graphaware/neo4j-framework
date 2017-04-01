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

package com.graphaware.runtime.module;

import com.graphaware.runtime.config.NullTxDrivenModuleConfiguration;
import com.graphaware.runtime.config.TxDrivenModuleConfiguration;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;

import java.util.Map;

/**
 * {@link com.graphaware.runtime.module.TxDrivenModule} that can tell whether it has been initialized for testing.
 */
public class BeforeAfterCommitModule extends BaseTxDrivenModule<String> {

    private final Map<String, String> config;
    private boolean afterCommitCalled = false;
    private boolean afterRollbackCalled = false;

    public BeforeAfterCommitModule(String moduleId, Map<String, String> config) {
        super(moduleId);
        this.config = config;
    }

    @Override
    public TxDrivenModuleConfiguration getConfiguration() {
        return NullTxDrivenModuleConfiguration.getInstance();
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public boolean isAfterCommitCalled() {
        return afterCommitCalled;
    }

    public boolean isAfterRollbackCalled() {
        return afterRollbackCalled;
    }

    @Override
    public String beforeCommit(ImprovedTransactionData transactionData) {
        return "test";
    }

    @Override
    public void afterCommit(String state) {
        if ("test".equals(state)) {
            afterCommitCalled = true;
        }
    }

    @Override
    public void afterRollback(String state) {
        if ("test".equals(state)) {
            afterRollbackCalled = true;
        }
    }
}
