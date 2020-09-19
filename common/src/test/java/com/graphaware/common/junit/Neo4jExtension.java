package com.graphaware.common.junit;

import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.util.AnnotationUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.lang.reflect.Field;

public class Neo4jExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        setUpNeo4j(context);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (AnnotationUtils.isAnnotated(context.getRequiredTestMethod(), DirtiesNeo4j.class)) {
            context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).remove("neo4j");
        } else {
            context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).get("neo4j", Neo4j.class).defaultDatabaseService().executeTransactionally("MATCH (n) DETACH DELETE n");
        }
    }

    private void setUpNeo4j(ExtensionContext context) throws IllegalAccessException {
        Neo4j neo4j = getNeo4j(context);

        for (Field f : AnnotationUtils.findAnnotatedFields(context.getRequiredTestClass(), InjectNeo4j.class, p -> true)) {
            if (f.getType().isAssignableFrom(Neo4j.class)) {
                f.setAccessible(true);
                f.set(context.getTestInstance().get(), neo4j);

            }
            if (f.getType().isAssignableFrom(GraphDatabaseService.class)) {
                f.setAccessible(true);
                f.set(context.getTestInstance().get(), neo4j.defaultDatabaseService());
            }
        }
    }

    private Neo4j getNeo4j(ExtensionContext context) {
        Neo4j current = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).get("neo4j", Neo4j.class);

        if (current == null) {
            current = Neo4jBuilders.newInProcessBuilder().withDisabledServer().build();
            context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).put("neo4j", current);
        }

        return current;
    }
}
