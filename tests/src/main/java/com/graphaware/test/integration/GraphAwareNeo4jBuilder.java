package com.graphaware.test.integration;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilder;
import org.neo4j.kernel.extension.ExtensionFactory;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class GraphAwareNeo4jBuilder {

    private final Neo4jBuilder wrapped;

    public static GraphAwareNeo4jBuilder builder(Neo4jBuilder neo4jBuilder) {
        return new GraphAwareNeo4jBuilder(neo4jBuilder);
    }

    public static void cleanup () {
        System.getProperties().stringPropertyNames().stream()
                .filter(s -> s.startsWith("com.graphaware"))
                .forEach(s -> System.getProperties().remove(s));
    }

    private GraphAwareNeo4jBuilder(Neo4jBuilder wrapped) {
        this.wrapped = wrapped;
    }

    public Neo4j build() {
        return wrapped.build();
    }

    public <U> GraphAwareNeo4jBuilder withConfig(Setting<U> key, U value) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withConfig(key, value));
    }

    public GraphAwareNeo4jBuilder withGAConfig(String key, String value) {
        System.setProperty(key, value);
        return this;
    }

    public GraphAwareNeo4jBuilder withUnmanagedExtension(String mountPath, Class<?> extension) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withUnmanagedExtension(mountPath, extension));
    }

    public GraphAwareNeo4jBuilder withUnmanagedExtension(String mountPath, String packageName) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withUnmanagedExtension(mountPath, packageName));
    }

    public GraphAwareNeo4jBuilder withExtensionFactories(Iterable<ExtensionFactory<?>> extensionFactories) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withExtensionFactories(extensionFactories));
    }

    public GraphAwareNeo4jBuilder withDisabledServer() {
        return GraphAwareNeo4jBuilder.builder(wrapped.withDisabledServer());
    }

    public GraphAwareNeo4jBuilder withFixture(File cypherFileOrDirectory) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withFixture(cypherFileOrDirectory));
    }

    public GraphAwareNeo4jBuilder withFixture(String fixtureStatement) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withFixture(fixtureStatement));
    }

    public GraphAwareNeo4jBuilder withFixture(Function<GraphDatabaseService, Void> fixtureFunction) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withFixture(fixtureFunction));
    }

    public GraphAwareNeo4jBuilder copyFrom(File sourceDirectory) {
        return GraphAwareNeo4jBuilder.builder(wrapped.copyFrom(sourceDirectory));
    }

    public GraphAwareNeo4jBuilder withProcedure(Class<?> procedureClass) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withProcedure(procedureClass));
    }

    public GraphAwareNeo4jBuilder withFunction(Class<?> functionClass) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withFunction(functionClass));
    }

    public GraphAwareNeo4jBuilder withAggregationFunction(Class<?> functionClass) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withAggregationFunction(functionClass));
    }

    public GraphAwareNeo4jBuilder withWorkingDir(File workingDirectory) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withWorkingDir(workingDirectory));
    }
}
