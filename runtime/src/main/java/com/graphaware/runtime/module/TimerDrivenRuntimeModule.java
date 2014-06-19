package com.graphaware.runtime.module;

import com.graphaware.common.util.Pair;
import com.graphaware.runtime.state.GraphPosition;
import com.graphaware.runtime.state.ModuleContext;

/**
 *
 */
public interface TimerDrivenRuntimeModule<C extends ModuleContext<?>> extends RuntimeModule {

    C doSomeWork(C lastContext);
}
