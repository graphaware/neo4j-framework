package com.graphaware.example.graphaware;

import com.graphaware.test.integration.GraphAwareIntegrationTest;
import org.junit.Test;

public class FullCustomConfigExpiryTest extends GraphAwareIntegrationTest {

	private static final long SECOND = 1_000;

//    @Override
//    protected String configFile() {
//        return "neo4j-expire-full-custom.conf";
//    }

	@Test
	public void shouldExpireNodesAndRelationshipsWhenExpiryDateReached() {
//
	}
}
