package com.graphaware.runtime.state;

/**
 *       position (where am I + how I got here) plus extra context potentially, such as "the weight I'm carrying"
 */
public interface ModuleContext<P extends GraphPosition> {

    P getPosition();
}
