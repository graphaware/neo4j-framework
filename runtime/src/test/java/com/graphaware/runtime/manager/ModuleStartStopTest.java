package com.graphaware.runtime.manager;

import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.metadata.TimerDrivenModuleContext;
import com.graphaware.runtime.module.BaseTimerDrivenModule;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.*;

public class ModuleStartStopTest {
    @Test
    public void startAndStopTimerDriverModule() throws Exception {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        ExampleModule module = new ExampleModule("example");
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
        runtime.registerModule(module);
        runtime.restart();
        runtime.start();

    }
    public class ExampleModule extends BaseTimerDrivenModule {
        public ExampleModule(String moduleId) {
            super(moduleId);
        }
        @Override
        public TimerDrivenModuleContext createInitialContext(GraphDatabaseService database) {
            return null;
        }
        @Override
        public TimerDrivenModuleContext doSomeWork(TimerDrivenModuleContext lastContext, GraphDatabaseService database) {
            return null;
        }
    }
}