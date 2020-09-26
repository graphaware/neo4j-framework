package com.graphaware.test.integration;

import org.junit.jupiter.api.AfterEach;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilder;
import org.neo4j.kernel.extension.ExtensionFactory;

import java.io.File;
import java.util.function.Function;

/**
 * A decorator for {@link Neo4jBuilder} that adds the {@link #withGAConfig(String, String)} method.
 * <p>
 * Please always call {@link #cleanup()} in a {@link AfterEach} tear-down test method.
 */
public class GraphAwareNeo4jBuilder {

    private final Neo4jBuilder wrapped;

    /**
     * Create a decorated builder. It doesn't matter if the original builder gets decorated before or after it has been
     * configured using its methods.
     *
     * @param neo4jBuilder to decorate.
     * @return decorated builder.
     */
    public static GraphAwareNeo4jBuilder builder(Neo4jBuilder neo4jBuilder) {
        return new GraphAwareNeo4jBuilder(neo4jBuilder);
    }

    /**
     * Cleanup after itself, i.e., remove all the System properties that were set using {@link #withGAConfig(String, String).}
     */
    public static void cleanup() {
        System.getProperties().stringPropertyNames().stream()
                .filter(s -> s.startsWith("com.graphaware"))
                .forEach(s -> System.getProperties().remove(s));
    }

    private GraphAwareNeo4jBuilder(Neo4jBuilder wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * @see Neo4jBuilder#build()
     */
    public Neo4j build() {
        return wrapped.build();
    }

    /**
     * @see Neo4jBuilder#withConfig(Setting, Object) ()
     */
    public <U> GraphAwareNeo4jBuilder withConfig(Setting<U> key, U value) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withConfig(key, value));
    }

    /**
     * Add a configuration key-value pair that would normally come from a graphaware.conf file. The implementation
     * just sets this as System properties. If you use this method, you must call {@link #cleanup()} when the test is
     * finished.
     *
     * @return self.
     */
    public GraphAwareNeo4jBuilder withGAConfig(String key, String value) {
        System.setProperty(key, value);
        return this;
    }

    /**
     * @see Neo4jBuilder#withUnmanagedExtension(String, Class)
     */
    public GraphAwareNeo4jBuilder withUnmanagedExtension(String mountPath, Class<?> extension) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withUnmanagedExtension(mountPath, extension));
    }

    /**
     * @see Neo4jBuilder#withUnmanagedExtension(String, String)
     */
    public GraphAwareNeo4jBuilder withUnmanagedExtension(String mountPath, String packageName) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withUnmanagedExtension(mountPath, packageName));
    }

    /**
     * @see Neo4jBuilder#withExtensionFactories(Iterable)
     */
    public GraphAwareNeo4jBuilder withExtensionFactories(Iterable<ExtensionFactory<?>> extensionFactories) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withExtensionFactories(extensionFactories));
    }

    /**
     * @see Neo4jBuilder#withDisabledServer()
     */
    public GraphAwareNeo4jBuilder withDisabledServer() {
        return GraphAwareNeo4jBuilder.builder(wrapped.withDisabledServer());
    }

    /**
     * @see Neo4jBuilder#withFixture(File)
     */
    public GraphAwareNeo4jBuilder withFixture(File cypherFileOrDirectory) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withFixture(cypherFileOrDirectory));
    }

    /**
     * @see Neo4jBuilder#withFixture(String)
     */
    public GraphAwareNeo4jBuilder withFixture(String fixtureStatement) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withFixture(fixtureStatement));
    }

    /**
     * @see Neo4jBuilder#withFixture(Function)
     */
    public GraphAwareNeo4jBuilder withFixture(Function<GraphDatabaseService, Void> fixtureFunction) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withFixture(fixtureFunction));
    }

    /**
     * @see Neo4jBuilder#copyFrom(File)
     */
    public GraphAwareNeo4jBuilder copyFrom(File sourceDirectory) {
        return GraphAwareNeo4jBuilder.builder(wrapped.copyFrom(sourceDirectory));
    }

    /**
     * @see Neo4jBuilder#withProcedure(Class)
     */
    public GraphAwareNeo4jBuilder withProcedure(Class<?> procedureClass) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withProcedure(procedureClass));
    }

    /**
     * @see Neo4jBuilder#withFunction(Class)
     */
    public GraphAwareNeo4jBuilder withFunction(Class<?> functionClass) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withFunction(functionClass));
    }

    /**
     * @see Neo4jBuilder#withAggregationFunction(Class)
     */
    public GraphAwareNeo4jBuilder withAggregationFunction(Class<?> functionClass) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withAggregationFunction(functionClass));
    }

    /**
     * @see Neo4jBuilder#withWorkingDir(File)
     */
    public GraphAwareNeo4jBuilder withWorkingDir(File workingDirectory) {
        return GraphAwareNeo4jBuilder.builder(wrapped.withWorkingDir(workingDirectory));
    }
}
