GraphAware Test
===============

This (Maven) module is part of the [GraphAware Neo4j Framework](https://github.com/graphaware/neo4j-framework).

### Introduction

This module provides means of easily testing code that talks to the Neo4j database in one way or another. The target
audience of this module are Java developers who write Neo4j-related code, as well as authors of GraphAware Modules and APIs.

### Getting the Module

Add the following snippet to your pom.xml:

```xml
 <dependency>
    <groupId>com.graphaware.neo4j</groupId>
    <artifactId>tests</artifactId>
    <version>3.3.0.51</version>
    <scope>test</scope>
</dependency>
```

Note: if your pom.xml inherits from a parent that brings in a Jetty version (e.g. if you're using Spring Boot), please put
the following in your `<properties>` section:
`<jetty.version>9.2.9.v20150224</jetty.version>`

<a name="graphunit"/>
### GraphUnit

[`GraphUnit`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/test/unit/GraphUnit.html) is a single
class with a few `public static` methods intended for easy unit-testing of code that somehow manipulates
data in the Neo4j graph database. It allows to assert the correct state of the database after the code has been run, using Cypher `CREATE` statements.

The first method `public static void assertSameGraph(GraphDatabaseService database, String sameGraphCypher)` is used to verify
that the graph in the `database` is exactly the same as the graph created by `sameGraphCypher` statement. This means that
the nodes, their properties and labels, relationships, and their properties and labels must be exactly the same. Note that
Neo4j internal node/relationship IDs are ignored. In case the graphs aren't identical, the assertion fails using standard `junit` mechanisms.

The second method `public static void assertSubgraph(GraphDatabaseService database, String subgraphCypher)` is used to
verify that the graph created by `sameGraphCypher` statement is a subgraph of the graph in the `database`.

*Note*: It is possible to use this functionality from other languages than Java over REST. Take a look at [GraphAware RestTest](https://github.com/graphaware/neo4j-resttest).

<a name="inttest"/>
### Integration Testing

GraphAware Test is very helpful for integration testing of Neo4j-related code. This includes Neo4j usage
in <a href="http://docs.neo4j.org/chunked/stable/tutorials-java-embedded.html" target="_blank">embedded mode</a>, development of
<a href="http://docs.neo4j.org/chunked/stable/server-plugins.html" target="_blank">server plugins</a>,
<a href="http://docs.neo4j.org/chunked/stable/server-unmanaged-extensions.html" target="_blank">unmanaged extensions</a>,
[GraphAware Runtime Modules](../runtime), Spring MVC Controllers that plug into [GraphAware Server](../server), etc.

Tests can be written by extending one of the provided abstract base-classes which handle all the plumbing, letting the
developer focus on the actual test logic.

Let us illustrate the scenarios, in which this library is helpful, on an example. All code presented here is avaialble
in [examples/integration-testing](../examples/integration-testing).

Let's start with creating a simple component that is capable of creating a "Hello World" node. We will then look at different
deployment options for this component, and how to test each one of them.

```java
/**
 * Very powerful class capable of creating a "Hello World" node. Intended for
 * demonstrating Neo4j integration testing with GraphAware Framework.
 */
public class HelloWorldNodeCreator {

    private final GraphDatabaseService database;

    public HelloWorldNodeCreator(GraphDatabaseService database) {
        this.database = database;
    }

    /**
     * Create a hello world node.
     *
     * @return created node.
     */
    public Node createHelloWorldNode() {
        Node node;

        try (Transaction tx = database.beginTx()) {
            node = database.createNode(Label.label("HelloWorld"));
            node.setProperty("hello", "world");
            tx.success();
        }

        return node;
    }
}
```

#### Embedded Mode

When using Neo4j in embedded mode, one would simply want to test the component itself. Typically, this is done by running
the code against an instance of `ImpermanentGraphDatabase`, which needs to be created and optionally pre-populated before
the test starts, and finally torn down after the test is finished. It then leaves no trace, because it only exists in memory.

A simple test would look like this, utilising [GraphUnit](#graphunit) to assert the state of the database, and
extending [`EmbeddedDatabaseIntegrationTest`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/test/integration/EmbeddedDatabaseIntegrationTest.html)
that handles the creation and destruction of the database. It allows tests to override a few methods, such as
`populateDatabase`, which is called before every test and does what it says on the tin.

```java
/**
 * {@link EmbeddedDatabaseIntegrationTest} for {@link com.graphaware.example.component.HelloWorldNodeCreator}.
 */
public class HelloWorldNodeCreatorTest extends EmbeddedDatabaseIntegrationTest {

    @Test
    public void shouldCreateAndReturnNode() {
        Node node = new HelloWorldNodeCreator(getDatabase()).createHelloWorldNode();

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals("world", node.getProperty("hello"));
            tx.success();
        }

        GraphUnit.assertSameGraph(getDatabase(), "CREATE (:HelloWorld {hello:'world'})");
    }
}
```

#### Server Plugin

When using Neo4j in server mode, one way of exposing the `HelloWorldNodeCreator` component is by writing a server plugin.
 It could look like this:

```java
/**
 * Managed server extension ({@link ServerPlugin}) that creates and returns a hello world node.
 */
@Description("An extension to the Neo4j Server for creating a hello world node")
public class HelloWorldServerPlugin extends ServerPlugin {

    @Name("hello_world_node")
    @Description("Create and return a hello world node")
    @PluginTarget(GraphDatabaseService.class)
    public Node createHelloWorldNode(@Source GraphDatabaseService database) {
        return new HelloWorldNodeCreator(database).createHelloWorldNode();
    }
}
```

In order to test this plugin, we have three options. First, we might want to test the plugin itself, i.e. we want to make
sure the logic it performs is correct. This would be closer to a unit-test. We would use the same base-class for the test
as previously:

```java
/**
 * {@link EmbeddedDatabaseIntegrationTest} for {@link HelloWorldServerPlugin}.
 *
 * Tests the logic, but not the API.
 */
public class HelloWorldServerPluginTest extends EmbeddedDatabaseIntegrationTest {

    @Test
    public void shouldCreateAndReturnNode() {
        Node node = new HelloWorldServerPlugin().createHelloWorldNode((getDatabase()));

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals("world", node.getProperty("hello"));
            tx.success();
        }

        GraphUnit.assertSameGraph(getDatabase(), "CREATE (:HelloWorld {hello:'world'})");
    }
}
```

One might also want to test the actual API, i.e., perform an end-to-end test by calling the API that the plugin will expose
and verify the HTTP response status code and body contents. At the same time, one might still want to make sure that
the database is in the correct state after the API call. For such kind of test, [`ServerIntegrationTest`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/test/integration/ServerIntegrationTest.html)
can be extended. It starts a database instance (again, just in memory) and also a server around it, simulating a server
deployment.

By default, the server runs on port 7575 in order not to interfere with an instance of Neo4j potentially running on port 7474
on the same machine. This can easily be changed by overriding the `neoServerPort()` method. Actually, one does not have
to worry about the port too much, because calling `baseNeoUrl()` will give you the base URL to execute requests against
(localhost:7575 by default).

The API can be exercised by using the inherited `protected` [`TestHttpClient`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/test/util/TestHttpClient.html),
namely the `get`, `post`, `put`, and `delete` methods. They take the URL to call and expected response status code as parameters,
and return the body of the response (which you might choose to assert as well). The test fails if the response status code
is different from what was expected.

Here's an example of a plugin API test:

```java
/**
 * {@link com.graphaware.test.integration.ServerIntegrationTest} for {@link HelloWorldServerPlugin}.
 *
 * Tests the logic as well as the API.
 */
public class HelloWorldServerPluginApiTest extends ServerIntegrationTest {

    @Test
    public void shouldCreateAndReturnNode() {
        httpClient.get(baseNeoUrl() + "/db/data/ext/HelloWorldServerPlugin/graphdb/hello_world_node", 200);
        String result = httpClient.post(baseNeoUrl() + "/db/data/ext/HelloWorldServerPlugin/graphdb/hello_world_node", 200);

        assertTrue(result.contains(" \"hello\" : \"world\""));
        GraphUnit.assertSameGraph(getDatabase(), "CREATE (:HelloWorld {hello:'world'})");
    }
}
```

#### Unmanaged Extension

If more power is needed than provided by server plugins, developers can deploy unmanaged extensions to the Neo4j server.
One that uses the `HelloWorldNodeCreator` from earlier could look like this:

```java
/**
 * Unmanaged server extension that creates and returns a hello world node.
 */
@Path("/helloworld")
public class HelloWorldUnmanagedExtension {

    private final HelloWorldNodeCreator nodeCreator;

    public HelloWorldUnmanagedExtension(@Context GraphDatabaseService database) {
        nodeCreator = new HelloWorldNodeCreator(database);
    }

    @POST
    @Path("/create")
    public Response createHelloWorldNode() {
        Node node = nodeCreator.createHelloWorldNode();
        return Response.ok(String.valueOf(node.getId())).build();
    }
}
```

Note that the following line needs to be added to neo4j.conf in order for the extension to be deployed
(assuming the extension lives in the `com.graphaware.example.unmanaged` package):
`dbms.unmanaged_extension_classes=com.graphaware.example.unmanaged=/ext`

We'll illustrate a way of integration-testing this extension, assuming that the business logic of `HelloWorldNodeCreator` has been
tested separately, as discussed earlier.

Similarly to the previous section where we were testing a server plugin the API of an unmanaged extension can be tested
using [`ServerIntegrationTest`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/test/integration/ServerIntegrationTest.html).
It is just a simulation of the server, so rather than having to modify a configuration file, we can override the
`thirdPartyJaxRsPackageMappings` and provide the key (package) - value (url) pairs as a `Map<String, String>`. Again,
the server will run on port 7575, but you will typically not need to worry about it:

```java
/**
 * {@link com.graphaware.test.integration.ServerIntegrationTest} for {@link com.graphaware.example.plugin.HelloWorldServerPlugin}.
 * <p/>
 * Tests the logic as well as the API.
 */
public class HelloWorldUnmanagedExtensionApiTest extends ServerIntegrationTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> thirdPartyJaxRsPackageMappings() {
        return Collections.singletonMap("com.graphaware.example.unmanaged", "/ext");
    }

    @Test
    public void shouldCreateAndReturnNode() {
        String result = httpClient.post(baseNeoUrl() + "/ext/helloworld/create", 200);
        assertEquals("0", result);

        GraphUnit.assertSameGraph(getDatabase(), "CREATE (:HelloWorld {hello:'world'})");
    }
}
```

#### Spring MVC Controller

With GraphAware Server distribution in the _plugins_ directory of a Neo4j server, it is possible to write APIs using
pure Spring MVC Controllers, which get an instance of `GraphDatabaseService` to be autowired in. In this case, we would
annotate the hello-world-node-creating component with `@Autowired` annotation and end up with the following classes:

```java
/**
 * Extension of {@link HelloWorldNodeCreator} annotated with Spring annotations, only useful for {@link com.graphaware.example.graphaware.HelloWorldController}.
 */
@Component
public class SpringHelloWorldNodeCreator extends HelloWorldNodeCreator {

    @Autowired
    public SpringHelloWorldNodeCreator(GraphDatabaseService database) {
        super(database);
    }
}
```
and
```java
/**
 * Spring controller that creates and returns a hello world node.
 */
@Controller
@RequestMapping("/helloworld")
public class HelloWorldController {

    private final HelloWorldNodeCreator nodeCreator;

    @Autowired
    public HelloWorldController(HelloWorldNodeCreator nodeCreator) {
        this.nodeCreator = nodeCreator;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public long createHelloWorldNode() {
        return nodeCreator.createHelloWorldNode().getId();
    }
}
```

Again, we can test the controller logic by extending `EmbeddedDatabaseIntegrationTest`:

```java
/**
 * {@link EmbeddedDatabaseIntegrationTest} for {@link com.graphaware.example.plugin.HelloWorldServerPlugin}.
 *
 * Tests the logic, but not the API.
 */
public class HelloWorldControllerTest extends EmbeddedDatabaseIntegrationTest {

    @Test
    public void shouldCreateAndReturnNode() {
        long nodeId = new HelloWorldController(new HelloWorldNodeCreator(getDatabase())).createHelloWorldNode();

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals("world", getDatabase().getNodeById(nodeId).getProperty("hello"));
            tx.success();
        }

        GraphUnit.assertSameGraph(getDatabase(), "CREATE (:HelloWorld {hello:'world'})");
    }
}
```

In order to test the API (end-to-end) test of a Spring MVC Controller deployed to Neo4j/GraphAware, one can extend the
`GraphAwareIntegrationTest`, which deploys the wrapping Neo4j server as before (on port 7575) and additionally exposes the MVC
Controllers. A test would then looks like this:

```java
/**
 * {@link GraphAwareIntegrationTest} for {@link HelloWorldController}.
 *
 * Tests the logic as well as the API.
 */
public class HelloWorldControllerApiTest extends GraphAwareIntegrationTest {

    @Test
    public void shouldCreateAndReturnNode() {
        assertEquals("0", httpClient.post(baseUrl() + "/helloworld/create", 200));

        GraphUnit.assertSameGraph(getDatabase(), "CREATE (:HelloWorld {hello:'world'})");
    }
}
```

<a name="perftest"/>
### Performance Testing

Sometimes it is necessary to run some experiments on the database to check how your code, queries, or the database
itself performs. This is tricky because there are many moving parts:
 * size of transaction (e.g. how often do you commit)?
 * database contents (you want this to be as realistic as possible)
 * data in cache (is data on disk? low level cache? high level cache?)
 * etc...

GraphAware Framework provides a set of classes to simplify performance testing with Neo4j. Start by exploring the Javadoc
 of [`PerformanceTestSuite`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/test/performance/PerformanceTestSuite.html)
 and [`PerformanceTest`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/test/performance/PerformanceTest.html).
 Then head to [examples/performance-testing](../examples/performance-testing) to see an implementation
 of a performance test used for <a href="http://graphaware.com/neo4j/2013/10/24/neo4j-qualifying-relationships.html" target="_blank">this blog post</a>.

In essence, each test can define a list of [`Parameter`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/test/performance/Parameter.html)s - these are the moving parts. The Framework will then generate
all permutations and run the performance test with each a specified number of times. Implementations of `PerformanceTest`
can specify, among other things:
* how many times the test should be run and measured
* how many times it should be run before measurements are started to warm up caches (dry runs)
* what parameters to use
* when to throw away and re-build the database

Here's a simple example of a performance test.

```java
/**
 * A {@link com.graphaware.test.performance.PerformanceTest} for documentation. Runs test for each of the scenarios
 * with 3 different {@link CacheConfiguration}s.
 */
public class DummyTestForDocs implements PerformanceTest {

    enum Scenario {
        SCENARIO_1,
        SCENARIO_2
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String shortName() {
        return "test-short-name";
    }

    @Override
    public String longName() {
        return "Test Long Name";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Parameter> parameters() {
        List<Parameter> result = new LinkedList<>();

        result.add(new CacheParameter("cache")); //no cache, low-level cache, high-level cache
        result.add(new EnumParameter("scenario", Scenario.class));

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int dryRuns(Map<String, Object> params) {
        return ((CacheConfiguration) params.get("cache")).needsWarmup() ? 10000 : 100;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int measuredRuns() {
        return 100;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> databaseParameters(Map<String, Object> params) {
        return ((CacheConfiguration) params.get("cache")).addToConfig(Collections.<String, String>emptyMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareDatabase(GraphDatabaseService database, final Map<String, Object> params) {
        //create 100 nodes in batches of 100
        new NoInputBatchTransactionExecutor(database, 100, 100, new UnitOfWork<NullItem>() {
            @Override
            public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                database.createNode();
            }
        }).execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RebuildDatabase rebuildDatabase() {
        return RebuildDatabase.AFTER_PARAM_CHANGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long run(GraphDatabaseService database, Map<String, Object> params) {
        Scenario scenario = (Scenario) params.get("scenario");
        switch (scenario) {
            case SCENARIO_1:
                //run test for scenario 1
                return 20; //the time it took in microseconds
            case SCENARIO_2:
                //run test for scenario 2
                return 20; //the time it took in microseconds
            default:
                throw new IllegalStateException("Unknown scenario");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rebuildDatabase(Map<String, Object> params) {
        throw new UnsupportedOperationException("never needed, database rebuilt after every param change");
    }
}
```

You would change the `run` method implementation to do some real work. Then add this test to a test suite and run it:

```java
/**
 * Dummy {@link PerformanceTestSuite} for documentation. Runs {@link DummyTestForDocs}.
 */
public class DummyTestSuiteForDocs extends PerformanceTestSuite {

    /**
     * {@inheritDoc}
     */
    @Override
    protected PerformanceTest[] getPerfTests() {
        return new PerformanceTest[]{
                new DummyTestForDocs()
        };
    }
}
```

This would result in a total of 6 different parameter permutations (3 cache types x 2 scenarios), each executed 100 times.
At the end of the run, you get a file called "test-short-name-xxx.txt" (xxx is a timestamp) in the root of your project.
The contents fo the file are the runtimes of each test, organised by parameter permutations:

```
Test Long Name

cache;scenario;times in microseconds...
nocache;SCENARIO_1;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20
nocache;SCENARIO_2;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20
lowcache;SCENARIO_1;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20
lowcache;SCENARIO_2;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20
highcache;SCENARIO_1;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20
highcache;SCENARIO_2;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20;20
```

You can now have some fun analysing the results - a good starting point could be the python scripts on the `resources`
folder of [examples/performance-testing](../examples/performance-testing).

License
-------

Copyright (c) 2013-2017 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
