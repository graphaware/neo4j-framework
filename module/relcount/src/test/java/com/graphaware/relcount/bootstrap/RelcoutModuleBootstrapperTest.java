package com.graphaware.relcount.bootstrap;

import com.graphaware.relcount.count.CachedRelationshipCounter;
import com.graphaware.relcount.count.FallbackRelationshipCounter;
import com.graphaware.relcount.count.NaiveRelationshipCounter;
import com.graphaware.relcount.count.RelationshipCounter;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Map;

import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.wildcard;
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
        Map<String, String> config = MapUtil.stringMap(
                "com.graphaware.framework.enabled", "true",
                "com.graphaware.module.relcount.enabled", "com.graphaware.relcount.bootstrap.RelcountModuleBootstrapper"
        );

        database = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder().setConfig(config).newGraphDatabase();
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void defaultFrameworkOnExistingDatabase() {
        simulateUsage();

        verifyCounts(new NaiveRelationshipCounter());
        verifyCounts(new CachedRelationshipCounter());
        verifyCounts(new FallbackRelationshipCounter());
    }

    private void verifyCounts(RelationshipCounter counter) {
        try (Transaction tx = database.beginTx()) {
            Node one = database.getNodeById(1);

            assertEquals(1, counter.count(one, wildcard(withName("ONE"), OUTGOING)));
        }
    }

    private void simulateUsage() {
        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                database.createNode();
                database.createNode();

                Node one = database.getNodeById(1);
                Node two = database.getNodeById(2);

                one.createRelationshipTo(two, withName("ONE"));
            }
        });
    }
}
