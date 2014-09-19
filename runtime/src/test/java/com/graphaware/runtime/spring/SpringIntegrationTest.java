package com.graphaware.runtime.spring;

import com.graphaware.module.changefeed.io.GraphChangeReader;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.Assert.assertNotNull;

/**
 * Integration test for Spring.
 */
@Ignore("until release of changefeed compatible with this")
public class SpringIntegrationTest {

    @Test
    public void changeFeedShouldWorkWithSpring() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class);

        assertNotNull(context.getBean(GraphDatabaseService.class));
        assertNotNull(context.getBean(GraphChangeReader.class));

        context.destroy();
    }
}
