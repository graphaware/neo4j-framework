package com.graphaware.runtime.module;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 */
public class TestModule implements TimerDrivenModule<TestMetadata> {

    @Override
    public TestMetadata doSomeWork(TestMetadata lastMetadata, GraphDatabaseService database) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void shutdown() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Class<TestMetadata> getMetadataClass() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
