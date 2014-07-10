### GraphAware Test

Add the following snippet to your pom.xml:

```xml
 <dependency>
    <groupId>com.graphaware.neo4j</groupId>
    <artifactId>tests</artifactId>
    <version>2.1.2.9</version>
    <scope>test</scope>
</dependency>
```

<a name="graphunit"/>
#### GraphUnit

`GraphUnit` is a single class with two `public static` methods intended for easy unit-testing of code that somehow manipulates
data in the Neo4j graph database. It allows to assert the correct state of the database after the code has been run, using Cypher `CREATE` statements.

The first method `public static void assertSameGraph(GraphDatabaseService database, String sameGraphCypher)` is used to verify
that the graph in the `database` is exactly the same as the graph created by `sameGraphCypher` statement. This means that
the nodes, their properties and labels, relationships, and their properties and labels must be exactly the same. Note that
Neo4j internal node/relationship IDs are ignored. In case the graphs aren't identical, the assertion fails using standard `junit` mechanisms.

The second method `public static void assertSubgraph(GraphDatabaseService database, String subgraphCypher)` is used to
verify that the graph created by `sameGraphCypher` statement is a subgraph of the graph in the `database`.

<a name="inttest"/>
#### Integration Testing
TBD

<a name="perftest"/>
#### Performance Testing

Sometimes it is necessary to run some experiments on the database to check how your code, queries, or the database
itself performs. This is tricky because there are many moving parts:
 * size of transaction (e.g. how often do you commit)?
 * database contents (you want this to be as realistic as possible)
 * data in cache (is data on disk? low level cache? high level cache?)
 * etc...

GraphAware Framework provides a set of classes to simplify performance testing with Neo4j. Start by exploring the JavaDoc
 of `PerformanceTestSuite` and `PerformanceTest`. Then head to `examples/performance-testing` to see an implementation
 of a performance test used for <a href="http://graphaware.com/neo4j/2013/10/24/neo4j-qualifying-relationships.html" target="_blank">this blog post</a>.

In essence, each test can define a list of `Parameters` - these are the moving parts. The Framework will then generate
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
folder of `examples/performance-testing`.

License
-------

Copyright (c) 2014 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
