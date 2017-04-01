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

import com.graphaware.runtime.module.RuntimeModule;

import java.util.Set;

/**
 * Component that stores {@link ModuleMetadata} so that it survives database restarts.
 */
public interface ModuleMetadataRepository {

    /**
     * Get the metadata of a module that has previously been presisted.
     *
     * @param module to get metadata for.
     * @param <M>    type of the metadata.
     * @return module metadata, null if no such metadata exists. This happens, for example, when a module has never been
     * registered and/or run before.
     */
    <M extends ModuleMetadata> M getModuleMetadata(RuntimeModule module);

    /**
     * Get the metadata of a module that has previously been presisted.
     *
     * @param moduleId to get metadata for.
     * @param <M>      type of the metadata.
     * @return module metadata, null if no such metadata exists. This happens, for example, when a module has never been
     * registered and/or run before.
     */
    <M extends ModuleMetadata> M getModuleMetadata(String moduleId);

    /**
     * Persist metadata of a module.
     *
     * @param module   for which to persist metadata.
     * @param metadata to persist.
     * @param <M>      type of the metadata.
     */
    <M extends ModuleMetadata> void persistModuleMetadata(RuntimeModule module, M metadata);

    /**
     * Persist metadata of a module.
     *
     * @param moduleId for which to persist metadata.
     * @param metadata to persist.
     * @param <M>      type of the metadata.
     */
    <M extends ModuleMetadata> void persistModuleMetadata(String moduleId, M metadata);

    /**
     * Get IDs of all modules, for which metadata has been persisted by this repository.
     *
     * @return IDs of all modules.
     */
    Set<String> getAllModuleIds();

    /**
     * Remove persisted metadata for a module.
     *
     * @param moduleId ID of the module for which to remove previously persisted metadata. Nothing happens if no such
     *                 metadata exists.
     */
    void removeModuleMetadata(String moduleId);
}
