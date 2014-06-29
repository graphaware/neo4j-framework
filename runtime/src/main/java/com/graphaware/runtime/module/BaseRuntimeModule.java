package com.graphaware.runtime.module;

import com.graphaware.runtime.metadata.ModuleMetadata;

/**
 * Base class for {@link com.graphaware.runtime.module.RuntimeModule} implementations.
 *
 * @param <M> type of metadata this module needs.
 */
public abstract class BaseRuntimeModule<M extends ModuleMetadata> implements RuntimeModule<M> {

    private final String moduleId;

    /**
     * Construct a new module.
     *
     * @param moduleId ID of this module.
     */
    protected BaseRuntimeModule(String moduleId) {
        this.moduleId = moduleId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return moduleId;
    }
}
