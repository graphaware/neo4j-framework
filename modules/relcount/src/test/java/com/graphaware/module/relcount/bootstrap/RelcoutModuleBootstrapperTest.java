package com.graphaware.module.relcount.bootstrap;

import com.graphaware.module.relcount.RelationshipCountConfigurationImpl;
import com.graphaware.module.relcount.count.CachedRelationshipCounter;
import com.graphaware.module.relcount.count.FallbackRelationshipCounter;
import com.graphaware.module.relcount.count.NaiveRelationshipCounter;
import com.graphaware.module.relcount.count.RelationshipCounter;
import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.wildcard;
import static com.graphaware.module.relcount.bootstrap.RelcountModuleBootstrapper.MODULE_ENABLED;
import static com.graphaware.runtime.bootstrap.RuntimeKernelExtension.RUNTIME_ENABLED;
import static org.junit.Assert.assertEquals;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Integration test for {@link RelcountModuleBootstrapper}.
 */
public class RelcoutModuleBootstrapperTest {

    protected GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(RUNTIME_ENABLED, "true")
                .setConfig(MODULE_ENABLED, MODULE_ENABLED.getDefaultValue())
                .newGraphDatabase();
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void defaultRuntimeOnExistingDatabase() {
        simulateUsage();

        verifyCounts(new NaiveRelationshipCounter(database, "relcount"));
        verifyCounts(new CachedRelationshipCounter(database, "relcount"));
        verifyCounts(new FallbackRelationshipCounter(database, "relcount"));
    }

    private void verifyCounts(RelationshipCounter counter) {
        try (Transaction tx = database.beginTx()) {
            Node one = database.getNodeById(0);

            assertEquals(1, counter.count(one, wildcard(withName("ONE"), OUTGOING)));

            tx.success();
        }
    }

    private void simulateUsage() {
        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.createNode();
                database.createNode();

                Node one = database.getNodeById(0);
                Node two = database.getNodeById(1);

                one.createRelationshipTo(two, withName("ONE"));
            }
        });
    }
}
