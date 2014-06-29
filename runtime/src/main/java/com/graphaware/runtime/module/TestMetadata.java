package com.graphaware.runtime.module;

import com.graphaware.runtime.metadata.NodeInGraph;
import com.graphaware.runtime.metadata.TimerDrivenModuleMetadata;

/**
 *
 */
public class TestMetadata implements TimerDrivenModuleMetadata<NodeInGraph> {

    @Override
    public NodeInGraph getLastPosition() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
