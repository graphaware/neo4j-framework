/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.lifecycle.programmatic;

import com.graphaware.common.util.IterableUtils;
import com.graphaware.lifecycle.LifecyleModule;
import com.graphaware.lifecycle.event.NodeExpiryEvent;
import com.graphaware.lifecycle.event.RelationshipExpiryEvent;
import com.graphaware.lifecycle.event.strategy.DeleteNodeAndRelationships;
import com.graphaware.lifecycle.LifecycleConfiguration;
import com.graphaware.lifecycle.event.strategy.DeleteOrphanedNodeOnly;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.config.FluentRuntimeConfiguration;
import com.graphaware.runtime.schedule.FixedDelayTimingStrategy;
import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterables;

import static com.graphaware.lifecycle.event.NodeExpiryEvent.*;
import static com.graphaware.lifecycle.event.NodeExpiryEvent.EXPIRY_OFFSET;
import static com.graphaware.lifecycle.event.RelationshipExpiryEvent.*;
import static com.graphaware.test.unit.GraphUnit.assertEmpty;
import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static com.graphaware.test.util.TestUtils.waitFor;
import static org.junit.Assert.*;

public class ProgrammaticIntegrationTest extends EmbeddedDatabaseIntegrationTest {

	private static final long SECOND = 1_000;


	@Test
	public void shouldExpireNodesWhenExpiryDateReached() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_EXPIRATION_PROPERTY, "expire")
				.scanForEvents());

		long now = System.currentTimeMillis();
		long twoSecondsFromNow = now + 2 * SECOND;
		long threeSecondsFromNow = now + 3 * SECOND;

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', expire:" + twoSecondsFromNow + "}), (s2:State {name:'Windy', expire:" + threeSecondsFromNow + "})");

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy', expire:" + twoSecondsFromNow + "}), (s2:State {name:'Windy', expire:" + threeSecondsFromNow + "})");

		waitFor(2100 - (System.currentTimeMillis() - now));

		assertSameGraph(getDatabase(), "CREATE (s2:State {name:'Windy', expire:" + threeSecondsFromNow + "})");

		waitFor(3100 - (System.currentTimeMillis() - now));

		assertEmpty(getDatabase());
	}

	@Test
	public void shouldExpireNodesWhenTtlReached() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.scanForEvents());

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', ttl:2000}), (s2:State {name:'Windy', ttl:3000})");

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy', ttl:2000}), (s2:State {name:'Windy', ttl:3000})");

		long now = System.currentTimeMillis();

		waitFor(2100 - (System.currentTimeMillis() - now));

		assertSameGraph(getDatabase(), "CREATE (s2:State {name:'Windy', ttl:3000})");

		waitFor(3100 - (System.currentTimeMillis() - now));

		assertEmpty(getDatabase());
	}

	@Test
	public void shouldExpireRelationshipsWhenExpiryDateReached() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(RELATIONSHIP_EXPIRATION_PROPERTY, "expire")
				.scanForEvents());

		long now = System.currentTimeMillis();
		long twoSecondsFromNow = now + 2 * SECOND;
		long threeSecondsFromNow = now + 3 * SECOND;

		getDatabase().execute("CREATE (s1:State {name:'Cloudy'})-[:THEN {expire:" + twoSecondsFromNow + "}]->(s2:State {name:'Windy'})-[:THEN {expire:" + threeSecondsFromNow + "}]->(s1)");

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy'})-[:THEN {expire:" + twoSecondsFromNow + "}]->(s2:State {name:'Windy'})-[:THEN {expire:" + threeSecondsFromNow + "}]->(s1)");

		waitFor(2100 - (System.currentTimeMillis() - now));

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy'}),(s2:State {name:'Windy'}), (s2)-[:THEN {expire:" + threeSecondsFromNow + "}]->(s1)");

		waitFor(3100 - (System.currentTimeMillis() - now));

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy'}), (s2:State {name:'Windy'})");
	}

	@Test
	public void shouldExpireRelationshipsWhenTtlDateReached() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(RELATIONSHIP_TTL_PROPERTY, "ttl")
				.scanForEvents());

		getDatabase().execute("CREATE (s1:State {name:'Cloudy'})-[:THEN {ttl:2000}]->(s2:State {name:'Windy'})-[:THEN {ttl:3000}]->(s1)");

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy'})-[:THEN {ttl:2000}]->(s2:State {name:'Windy'})-[:THEN {ttl:3000}]->(s1)");

		long now = System.currentTimeMillis();

		waitFor(2100 - (System.currentTimeMillis() - now));

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy'}),(s2:State {name:'Windy'}), (s2)-[:THEN {ttl:3000}]->(s1)");

		waitFor(3100 - (System.currentTimeMillis() - now));

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy'}), (s2:State {name:'Windy'})");
	}

	@Test
	public void shouldHaveEmptyIndexWhenEverythingHasExpired() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.withConfig(RELATIONSHIP_TTL_PROPERTY, "ttl")
				.scanForEvents());

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', ttl:1000})-[:THEN {ttl:1000}]->(s2:State {name:'Windy',ttl:1000})-[:THEN {ttl:1000}]->(s1)");

		try (Transaction tx = getDatabase().beginTx()) {
			assertNotEquals(0, countNodesInIndex());
			assertNotEquals(0, countRelationshipsInIndex());
			tx.success();
		}

		waitFor(1100);

		assertEmpty(getDatabase());

		try (Transaction tx = getDatabase().beginTx()) {
			assertEquals(0, countNodesInIndex());
			assertEquals(0, countRelationshipsInIndex());
			tx.success();
		}
	}

	@Test
	public void shouldHaveEmptyIndexWhenEverythingHasBeenManuallyWiped() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.withConfig(RELATIONSHIP_TTL_PROPERTY, "ttl")
				.scanForEvents());

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', ttl:10000})-[:THEN {ttl:10000}]->(s2:State {name:'Windy',ttl:10000})-[:THEN {ttl:10000}]->(s1)");

		try (Transaction tx = getDatabase().beginTx()) {
			assertNotEquals(0, countNodesInIndex());
			assertNotEquals(0, countRelationshipsInIndex());
			tx.success();
		}

		getDatabase().execute("MATCH (n) DETACH DELETE (n)");

		assertEmpty(getDatabase());

		try (Transaction tx = getDatabase().beginTx()) {
			assertEquals(0, countNodesInIndex());
			assertEquals(0, countRelationshipsInIndex());
			tx.success();
		}
	}

	@Test
	public void shouldForceDeleteRelationshipsWhenConfiguredToDoSo() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.withConfig(RELATIONSHIP_TTL_PROPERTY, "ttl")
				.withConfig(NODE_EXPIRATION_STRATEGY, DeleteNodeAndRelationships.CONFIG_KEY)
				.scanForEvents());

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', ttl:1000})-[:THEN {ttl:5000}]->(s2:State {name:'Windy',ttl:1000})-[:THEN]->(s1)");

		waitFor(1100);

		assertEmpty(getDatabase());
	}

	@Test
	public void shouldWaitForAllRelationshipsToExpireWhenConfiguredToDoSo() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.withConfig(RELATIONSHIP_TTL_PROPERTY, "ttl")
				.withConfig(NODE_EXPIRATION_STRATEGY, DeleteOrphanedNodeOnly.CONFIG_KEY)
				.scanForEvents());

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', ttl:1000})-[:THEN {ttl:3000}]->(s2:State {name:'Windy',ttl:1000})-[:THEN {ttl:2000}]->(s1)");

		waitFor(2100);

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy', ttl:1000})-[:THEN {ttl:3000}]->(s2:State {name:'Windy',ttl:1000})");

		waitFor(1100);

		assertEmpty(getDatabase());
	}

	@Test
	public void shouldWaitForAllRelationshipsToBeDeletedWhenConfiguredToDoSo() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.withConfig(RELATIONSHIP_TTL_PROPERTY, "ttl")
				.withConfig(NODE_EXPIRATION_STRATEGY, DeleteOrphanedNodeOnly.CONFIG_KEY)
				.scanForEvents());

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', ttl:1000})-[:THEN]->(s2:State {name:'Windy',ttl:1000})-[:THEN]->(s1)");

		waitFor(1500);

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy', ttl:1000})-[:THEN]->(s2:State {name:'Windy',ttl:1000})-[:THEN]->(s1)");

		getDatabase().execute("MATCH (n)-[r]-() DELETE r");

		waitFor(110);

		assertEmpty(getDatabase());
	}

	@Test
	public void shouldBeAbleToUpdateNodesAndRelsWithTtl() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.withConfig(RELATIONSHIP_TTL_PROPERTY, "ttl")
				.scanForEvents());

		getDatabase().execute("CREATE (s1:State {name:'Cloudy'})-[:THEN]->(s2:State {name:'Windy'})-[:THEN]->(s1)");

		getDatabase().execute("MATCH (n)-[r]-() SET n.ttl=500, r.ttl=500");

		waitFor(700);

		assertEmpty(getDatabase());
	}

	@Test
	public void removedTtlShouldMeanNoExpiration() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.withConfig(RELATIONSHIP_TTL_PROPERTY, "ttl")
				.scanForEvents());

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', ttl:2000})-[:THEN {ttl:2000}]->(s2:State {name:'Windy',ttl:2000})");

		getDatabase().execute("MATCH (n)-[r]-() REMOVE n.ttl, r.ttl");

		waitFor(2200);

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy'})-[:THEN]->(s2:State {name:'Windy'})");
	}

	@Test
	public void shouldBeAbleToUpdateNodesAndRelsWithExpiryDate() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_EXPIRATION_PROPERTY, "expire")
				.withConfig(RELATIONSHIP_EXPIRATION_PROPERTY, "expire")
				.scanForEvents());

		long now = System.currentTimeMillis();
		long nowAndABit = now + 500;

		getDatabase().execute("CREATE (s1:State {name:'Cloudy'})-[:THEN]->(s2:State {name:'Windy'})-[:THEN]->(s1)");

		getDatabase().execute("MATCH (n)-[r]-() SET n.expire=" + nowAndABit + ", r.expire=" + nowAndABit);

		waitFor(700);

		assertEmpty(getDatabase());
	}

	@Test
	public void removedExpiryDateMeanNoExpiration() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_EXPIRATION_PROPERTY, "expire")
				.withConfig(RELATIONSHIP_EXPIRATION_PROPERTY, "expire")
				.scanForEvents());

		long now = System.currentTimeMillis();
		long twoSecondsFromNow = now + 2 * SECOND;

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', expire:" + twoSecondsFromNow + "})-[:THEN {expire:" + twoSecondsFromNow + "}]->(s2:State {name:'Windy',expire:" + twoSecondsFromNow + "})");

		getDatabase().execute("MATCH (n)-[r]-() REMOVE n.expire, r.expire");

		waitFor(2200);

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy'})-[:THEN]->(s2:State {name:'Windy'})");
	}

	@Test
	public void shouldBeAbleToUpdateTtl() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.withConfig(RELATIONSHIP_TTL_PROPERTY, "ttl")
				.scanForEvents());

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', ttl:1000})-[:THEN {ttl:1000}]->(s2:State {name:'Windy', ttl:1000})");

		getDatabase().execute("MATCH (n)-[r]-() SET n.ttl=2000, r.ttl=2000");

		waitFor(1200);

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy', ttl:2000})-[:THEN {ttl:2000}]->(s2:State {name:'Windy', ttl:2000})");

		waitFor(1200);

		assertEmpty(getDatabase());
	}

	@Test
	public void shouldBeAbleToUpdateExpiryDate() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_EXPIRATION_PROPERTY, "expire")
				.withConfig(RELATIONSHIP_EXPIRATION_PROPERTY, "expire")
				.scanForEvents());

		long now = System.currentTimeMillis();
		long nowAndABit = now + 500;
		long nowAndTwoSeconds = now + 2 * SECOND;

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', expire:" + nowAndABit + "})-[:THEN {expire:" + nowAndABit + "}]->(s2:State {name:'Windy', expire:" + nowAndABit + "})");

		getDatabase().execute("MATCH (n)-[r]-() SET n.expire=" + nowAndTwoSeconds + ", r.expire=" + nowAndTwoSeconds);

		waitFor(1200);

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy', expire:" + nowAndTwoSeconds + "})-[:THEN {expire:" + nowAndTwoSeconds + "}]->(s2:State {name:'Windy', expire:" + nowAndTwoSeconds + "})");

		waitFor(1200);

		assertEmpty(getDatabase());
	}

	@Test
	public void shouldBeAbleToUpdateNodeWithoutChangingExpiry() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.withConfig(RELATIONSHIP_TTL_PROPERTY, "ttl")
				.scanForEvents());

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', ttl:2000})-[:THEN {ttl:2000}]->(s2:State {name:'Windy',ttl:2000})");

		getDatabase().execute("MATCH (n) SET n.someKey='someValue'");

		waitFor(2200);

		assertEmpty(getDatabase());
	}

	@Test
	public void laterExpiryShouldTakePrecedence() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.withConfig(NODE_EXPIRATION_PROPERTY, "expire")
				.scanForEvents());

		long now = System.currentTimeMillis();
		long nowAndThreeSeconds = now + 3 * SECOND;

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', ttl:1000, expire:" + nowAndThreeSeconds + "})");

		waitFor(1200);

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy', ttl:1000, expire:" + nowAndThreeSeconds + "})");

		waitFor(2200);

		assertEmpty(getDatabase());
	}

	@Test
	public void laterExpiryShouldTakePrecedence2() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.withConfig(NODE_EXPIRATION_PROPERTY, "expire")
				.scanForEvents());

		long now = System.currentTimeMillis();
		long nowAndOneSecond = now + SECOND;

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', ttl:3000, expire:" + nowAndOneSecond + "})");

		waitFor(1200);

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy', ttl:3000, expire:" + nowAndOneSecond + "})");

		waitFor(2200);

		assertEmpty(getDatabase());
	}

	@Test
	public void wrongExpiryDateShouldHaveNoEffect() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_EXPIRATION_PROPERTY, "expire")
				.scanForEvents());

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', ttl:'wrong', expire:'wrong'})");

		waitFor(1200);

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy', ttl:'wrong', expire:'wrong'})");
	}

	@Test
	public void wrongTtlDateShouldHaveNoEffect() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.scanForEvents());

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', ttl:'wrong', expire:'wrong'})");

		waitFor(1200);

		assertSameGraph(getDatabase(), "CREATE (s1:State {name:'Cloudy', ttl:'wrong', expire:'wrong'})");
	}

	@Test
	public void negativeTtlShouldBeExpiredImmediately() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_EXPIRATION_PROPERTY, "expire")
				.scanForEvents());

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', expire:0})");

		waitFor(200);

		assertEmpty(getDatabase());
	}

	@Test
	public void expiryDateInThePastShouldBeExpiredImmediately() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.scanForEvents());

		getDatabase().execute("CREATE (s1:State {name:'Cloudy', ttl:'-20'})");

		waitFor(200);

		assertEmpty(getDatabase());
	}

	@Test
	public void shouldBeAbleToApplyAnOffsetToExpiryDates() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_EXPIRATION_PROPERTY, "lastActive")
				.withConfig(EXPIRY_OFFSET, "10000")
				.scanForEvents());

		long now = System.currentTimeMillis();
		getDatabase().execute(String.format("CREATE (s1:State {name:'Cloudy', lastActive:'%s'})", now));

		waitFor(200);

		//not expired yet, because of expiry offset
		//TODO: replace with assertNotEmpty
		assertSameGraph(getDatabase(), String.format("CREATE (s1:State {name:'Cloudy', lastActive:'%s'})", now));

		waitFor(10_000);
		assertEmpty(getDatabase());
	}

	@Test
	public void moduleRegisteredOnExistingDatabaseWithTtlShouldExpireRelevantData() {
		getDatabase().execute("CREATE (s1:State {name:'Cloudy', ttl:1000})-[:THEN {ttl:1000}]->(s2:State {name:'Windy',ttl:1000})");

		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.withConfig(RELATIONSHIP_TTL_PROPERTY, "ttl")
				.scanForEvents());

		waitFor(1100);

		assertEmpty(getDatabase());
	}

	@Test
	public void moduleRegisteredOnExistingDatabaseWithExpiryDatesShouldExpireRelevantData() {
		getDatabase().execute("CREATE (s1:State {name:'Cloudy', expire:123})-[:THEN {expire:123}]->(s2:State {name:'Windy',expire:123})");

		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_EXPIRATION_PROPERTY, "expire")
				.withConfig(RELATIONSHIP_EXPIRATION_PROPERTY, "expire")
				.scanForEvents());

		waitFor(1100);

		assertEmpty(getDatabase());
	}

	@Test
	public void shouldExpireAllNodesInOneGo() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_EXPIRATION_PROPERTY, "expire")
				.withBatchSize(100)
				.scanForEvents());

		long now = System.currentTimeMillis();
		long twoSecondsFromNow = now + 2 * SECOND;

		try (Transaction tx = getDatabase().beginTx()) {
			for (int i = 0; i < 100; i++) {
				getDatabase().execute("CREATE (s1:State {name:'Cloudy" + i + "', expire:" + twoSecondsFromNow + "})");
			}
			tx.success();
		}

		try (Transaction tx = getDatabase().beginTx()) {
			assertEquals(100, IterableUtils.countNodes(getDatabase()));
		}

		waitFor(3100 - (System.currentTimeMillis() - now));

		assertEmpty(getDatabase());
	}

	@Test
	public void shouldExpireAllNodesOneByOne() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_EXPIRATION_PROPERTY, "expire")
				.withBatchSize(1)
				.scanForEvents());

		long now = System.currentTimeMillis();
		long twoSecondsFromNow = now + 2 * SECOND;

		try (Transaction tx = getDatabase().beginTx()) {
			for (int i = 0; i < 100; i++) {
				getDatabase().execute("CREATE (s1:State {name:'Cloudy" + i + "', expire:" + twoSecondsFromNow + "})");
			}
			tx.success();
		}

		try (Transaction tx = getDatabase().beginTx()) {
			assertEquals(100, IterableUtils.countNodes(getDatabase()));
		}

		waitFor(20100 - (System.currentTimeMillis() - now));

		assertEmpty(getDatabase());
	}

	@Test(expected = IllegalStateException.class)
	public void shouldFailToStartWithInvalidConfig2() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_EXPIRATION_INDEX, null)
				.withConfig(RELATIONSHIP_EXPIRATION_INDEX, null)
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.withConfig(RELATIONSHIP_TTL_PROPERTY, "ttl")
				.register(NodeExpiryEvent.class)
				.register(RelationshipExpiryEvent.class));
	}

	@Test(expected = IllegalStateException.class)
	public void shouldFailToStartWithInvalidConfig3() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.withConfig(RELATIONSHIP_TTL_PROPERTY, "ttl")
				.withConfig(NODE_EXPIRATION_PROPERTY, "ttl")
				.scanForEvents()
		);
	}

	@Test(expected = IllegalStateException.class)
	public void shouldFailToStartWithInvalidConfig4() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withConfig(NODE_TTL_PROPERTY, "ttl")
				.withConfig(RELATIONSHIP_TTL_PROPERTY, "ttl")
				.withConfig(RELATIONSHIP_EXPIRATION_PROPERTY, "ttl")
				.scanForEvents()
		);
	}

	@Test(expected = IllegalStateException.class)
	public void shouldFailToStartWithInvalidConfig5() {
		bootstrap(LifecycleConfiguration.defaultConfiguration()
				.withBatchSize(-1)
		);
	}

	private void bootstrap(LifecycleConfiguration config) {
		GraphAwareRuntime runtime = createRuntime();
		runtime.registerModule(new LifecyleModule("LM", getDatabase(), config, config.getNodeScheduledEvents(),
				config.getRelationshipScheduledEvents(), config.getNodeCommitEvents(),
				config.getRelationshipCommitEvents(), config.getBatchSize()));

		runtime.start();
		runtime.waitUntilStarted();
	}

	private GraphAwareRuntime createRuntime() {
		return GraphAwareRuntimeFactory.createRuntime(getDatabase(),
				FluentRuntimeConfiguration.defaultConfiguration(getDatabase())
						.withTimingStrategy(FixedDelayTimingStrategy.getInstance().withInitialDelay(100).withDelay(100)));
	}

	private long countNodesInIndex() {
		return Iterables.count(getDatabase().index().forNodes("nodeExpirationIndex").query(new MatchAllDocsQuery()));
	}

	private long countRelationshipsInIndex() {
		return Iterables.count(getDatabase().index().forRelationships("relationshipExpirationIndex").query(new MatchAllDocsQuery()));
	}
}
