package com.graphaware.runtime.module;

/**
 * Base class for {@link com.graphaware.runtime.module.RuntimeModule} implementations.
 *
 */
public abstract class BaseRuntimeModule implements RuntimeModule {

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
