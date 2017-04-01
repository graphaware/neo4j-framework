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

package com.graphaware.runtime.schedule;

import com.graphaware.runtime.metadata.TimerDrivenModuleContext;
import com.graphaware.runtime.module.TimerDrivenModule;

/**
 * A component delegating to registered {@link TimerDrivenModule}s on a scheduled basis.
 */
public interface TaskScheduler {

    /**
     * Register a module and its context. Registered modules will be delegated to and contexts managed by the implementation
     * of this interface after the first registration. Must be called before {@link #start()}.
     *
     * @param module   to register.
     * @param context of the module.
     * @param <C>      type of the metadata.
     * @param <T>      type of the module.
     */
    <C extends TimerDrivenModuleContext, T extends TimerDrivenModule<C>> void registerModuleAndContext(T module, C context);

    /**
     * Start scheduling tasks / delegating work to registered modules.
     */
    void start();

    /**
     * Stop scheduling tasks. Perform cleanup. No other methods should be called afterwards as this object will be useless.
     */
    void stop();
}
