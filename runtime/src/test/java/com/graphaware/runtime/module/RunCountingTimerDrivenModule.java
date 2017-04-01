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

import com.graphaware.runtime.config.TimerDrivenModuleConfiguration;
import com.graphaware.runtime.metadata.TimerDrivenModuleContext;
import org.neo4j.graphdb.GraphDatabaseService;

public class RunCountingTimerDrivenModule extends BaseTimerDrivenModule {

    private final TimerDrivenModuleConfiguration config;
    private int runs = 0;

    public RunCountingTimerDrivenModule(TimerDrivenModuleConfiguration config) {
        super("test");
        this.config = config;
    }

    @Override
    public TimerDrivenModuleContext createInitialContext(GraphDatabaseService database) {
        return null;
    }

    @Override
    public TimerDrivenModuleContext doSomeWork(TimerDrivenModuleContext lastContext, GraphDatabaseService database) {
        runs++;
        return null;
    }

    @Override
    public TimerDrivenModuleConfiguration getConfiguration() {
        return config;
    }

    public int getRuns() {
        return runs;
    }
}
