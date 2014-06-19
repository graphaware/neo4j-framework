package com.graphaware.runtime.module;

import com.graphaware.common.util.Pair;
import com.graphaware.runtime.state.GraphPosition;

/**
 *
 */
public interface TimerDrivenRuntimeModule<T, P extends GraphPosition<T>, C> extends RuntimeModule {

    Pair<P, C> pickFirstPosition();

    Pair<P, C> pickNextPosition(P lastPosition, C lastContext);

    void doSomeWork(P position, C context);
}
