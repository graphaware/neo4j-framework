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

package com.graphaware.runtime.module.thirdparty;

import com.graphaware.writer.thirdparty.ThirdPartyWriter;
import com.graphaware.writer.thirdparty.WriteOperation;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Collection;

import static org.springframework.util.Assert.notNull;

/**
 * A {@link ThirdPartyIntegrationModule} that integrates with the third-party system by using a {@link ThirdPartyWriter}.
 */
public abstract class WriterBasedThirdPartyIntegrationModule<ID> extends ThirdPartyIntegrationModule<ID> {

    private final ThirdPartyWriter writer;

    /**
     * Construct a new module.
     *
     * @param moduleId ID of this module. Must not be <code>null</code> or empty.
     * @param writer to use for integrating with third-party system. Must not be <code>null</code>.
     */
    protected WriterBasedThirdPartyIntegrationModule(String moduleId, ThirdPartyWriter writer) {
        super(moduleId);

        notNull(writer);
        this.writer = writer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterCommit(Collection<WriteOperation<?>> state) {
        writer.write(state, getId() + "-" + System.currentTimeMillis());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(GraphDatabaseService database) {
        super.start(database);
        writer.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        writer.stop();
        super.shutdown();
    }
}
