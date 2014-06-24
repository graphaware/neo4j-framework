package com.graphaware.runtime.manager;

import com.graphaware.runtime.module.RuntimeModule;

/**
 *
 */
public interface ModuleManager<T extends RuntimeModule> {

    void registerModule(T module);
}
