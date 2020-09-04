package com.graphaware.common.junit;

import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.util.AnnotationUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.Neo4j;

import java.lang.reflect.Field;

import static com.graphaware.common.junit.InjectNeo4j.Lifecycle.*;

public class Neo4jExtension implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(Neo4jExtension.class);
    private static final Neo4jInstances INSTANCES = Neo4jInstances.getInstances();

    public Neo4jExtension() {
        INSTANCES.start();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        setUpNeo4j(METHOD, context);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        destroyNeo4j(METHOD, context);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        setUpNeo4j(CLASS, context);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        destroyNeo4j(CLASS, context);
    }

    private void setUpNeo4j(InjectNeo4j.Lifecycle lifecycle, ExtensionContext context) throws IllegalAccessException {
        Neo4j neo4j = null;
        InjectNeo4j.Lifecycle lc = null;

        for (Field f : AnnotationUtils.findAnnotatedFields(context.getRequiredTestClass(), InjectNeo4j.class, p -> true)) {
            if (lc != null && !lc.equals(f.getAnnotation(InjectNeo4j.class).lifecycle())) {
                throw new RuntimeException("Inconsistent lifecycle on Neo4j-related fields. Please use the same lifecycle " +
                        " (METHOD/CLASS) on all fields within one test class");
            }

            lc = f.getAnnotation(InjectNeo4j.class).lifecycle();

            if (!lifecycle.equals(lc) && !f.isAnnotationPresent(RecreateNeo4j.class)) {
                continue;
            }

            if (f.getType().isAssignableFrom(Neo4j.class)) {
                f.setAccessible(true);
                if (neo4j == null) {
                    neo4j = INSTANCES.get();
                }
                f.set(context.getTestInstance().get(), neo4j);

            }
            if (f.getType().isAssignableFrom(GraphDatabaseService.class)) {
                f.setAccessible(true);
                if (neo4j == null) {
                    neo4j = INSTANCES.get();
                }
                f.set(context.getTestInstance().get(), neo4j.defaultDatabaseService());
            }
        }

        if (neo4j != null) {
            context.getStore(NAMESPACE).put(getKey(context, lifecycle), neo4j);
        }
    }

    private void destroyNeo4j(InjectNeo4j.Lifecycle lifecycle, ExtensionContext context) {
//        new Thread(() -> {
            Neo4j neo4j = (Neo4j) context.getStore(NAMESPACE).get(getKey(context, lifecycle));
            if (neo4j != null) {
                System.out.println("Closing Neo4j");
                neo4j.close();
            }
//        }).start();
    }

    private String getKey(ExtensionContext context, InjectNeo4j.Lifecycle lc) {
        String key;

        switch (lc) {
            case CLASS:
                key = context.getRequiredTestClass().getName();
                break;
            case METHOD:
                key = context.getRequiredTestMethod().getName();
                break;
            default:
                throw new IllegalStateException("Invalid Lifecycle");
        }
        return key;
    }
}
