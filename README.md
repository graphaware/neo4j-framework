<a name="top"/>
GraphAware Neo4j Framework
==========================

[![Build Status](https://travis-ci.org/graphaware/neo4j-framework.png)](https://travis-ci.org/graphaware/neo4j-framework) | <a href="http://graphaware.com/downloads/" target="_blank">Downloads</a> | <a href="http://graphaware.com/site/framework/latest/apidocs/" target="_blank">Javadoc</a> | Latest Releases: 2.0.3.5

GraphAware Framework speeds up development with <a href="http://neo4j.org" target="_blank">Neo4j</a> by providing a
platform for building useful generic as well as domain-specific functionality, analytical capabilities, graph algorithms,
etc.

See the <a href="http://graphaware.com/neo4j/2014/05/28/graph-aware-neo4j-framework.html" target="_blank">announcement on our blog</a>.

Features Overview
-----------------

On a high level, there are two key pieces of functionality:
* [GraphAware Server](#graphaware-server) is a Neo4j server extension that allows developers to rapidly build (REST) APIs
on top of Neo4j using Spring MVC, rather than JAX-RS.
* [GraphAware Runtime](#graphaware-runtime) is a runtime environment for both embedded and server deployments, which
allows the use of pre-built as well as custom modules called [GraphAware Runtime Modules](#graphaware-runtime). These
modules typically extend the core functionality of the database by transparently enriching/modifying/preventing ongoing
transactions in real-time.

Additionally, for [Java developers only](#javadev)(1), the following functionality is provided:

* [GraphAware Test](#graphaware-test)
    * [GraphUnit](#graphunit) - a library for simple graph unit-testing
    * [Performance Testing Library](#perftest)
    * [API Testing Library](#apitest)
* [Improved Neo4j Transaction API](#tx-api)
* [Transaction Executor](#tx-executor) and [Batch Transaction Executor](#batch-tx)
* [Miscellaneous Utilities](#utils)

(1) (i.e., for embedded mode users, managed/unmanaged extensions developers, [GraphAware Runtime Module](#graphaware-runtime)
 developers and framework-powered Spring MVC controller developers

Framework Usage
---------------

<a name="servermode"/>
### Server Mode

When using Neo4j in the <a href="http://docs.neo4j.org/chunked/stable/server-installation.html" target="_blank">standalone server</a> mode,
deploying the GraphAware Framework (and any code using it) is a matter of [downloading](#download) the appropriate .jar files,
copying them into the _plugins_ directory in your Neo4j installation, and restarting the server. The framework and modules
are then used via calls to their REST APIs, if they provide any.

Note that only **Neo4j 2.0.3 and above** are supported. If you see a `java.lang.IllegalAccessError` when starting up the
server, then you're most likely using a version of Neo4j older than 2.0.3.

### Embedded Mode / Java Development

Java developers that use Neo4j in <a href="http://docs.neo4j.org/chunked/stable/tutorials-java-embedded.html" target="_blank">embedded mode</a>
and those developing Neo4j <a href="http://docs.neo4j.org/chunked/stable/server-plugins.html" target="_blank">server plugins</a>,
<a href="http://docs.neo4j.org/chunked/stable/server-unmanaged-extensions.html" target="_blank">unmanaged extensions</a>,
[GraphAware Runtime Modules](#graphaware-runtime), or Spring MVC controllers can include use the framework as a dependency
for their Java project and use it as a library of useful tested code, in addition to the functionality provided for
[server mode](#servermode).

<a name="download"/>
Getting GraphAware Framework
----------------------------

### Releases

To use the latest release, download the appropriate version and put it
the _plugins_ directory in your Neo4j server installation and restart the server (server mode), or on the classpath (embedded mode).

The following downloads are available:
* [GraphAware Framework for Embedded Mode, version 2.0.3.5](http://graphaware.com/downloads/graphaware-embedded-all-2.0.3.5.jar)
* [GraphAware Framework for Server Mode (Community), version 2.0.3.5](http://graphaware.com/downloads/graphaware-server-community-all-2.0.3.5.jar)
* [GraphAware Framework for Server Mode (Enterprise), version 2.0.3.5](http://graphaware.com/downloads/graphaware-server-enterprise-all-2.0.3.5.jar)

Releases are synced to <a href="http://search.maven.org/#search%7Cga%7C1%7Ccom.graphaware.neo4j" target="_blank">Maven Central repository</a>. When using Maven for dependency management, include one of more of the following dependencies in your pom.xml. Read further
down this page to find out which dependencies you will need. The available ones are:

    <dependencies>
        ...
        <dependency>
            <groupId>com.graphaware.neo4j</groupId>
            <artifactId>api</artifactId>
            <version>2.0.3.5</version>
        </dependency>
        <dependency>
            <groupId>com.graphaware.neo4j</groupId>
            <artifactId>common</artifactId>
            <version>2.0.3.5</version>
        </dependency>
        <dependency>
            <groupId>com.graphaware.neo4j</groupId>
            <artifactId>runtime</artifactId>
            <version>2.0.3.5</version>
        </dependency>
        <dependency>
            <groupId>com.graphaware.neo4j</groupId>
            <artifactId>tests</artifactId>
            <version>2.0.3.5</version>
        </dependency>
        <dependency>
            <groupId>com.graphaware.neo4j</groupId>
            <artifactId>tx-api</artifactId>
            <version>2.0.3.5</version>
        </dependency>
        <dependency>
            <groupId>com.graphaware.neo4j</groupId>
            <artifactId>tx-executor</artifactId>
            <version>2.0.3.5</version>
        </dependency>

        ...
    </dependencies>

### Snapshots

To use the latest development version, just clone this repository and run `mvn clean install`. This will produce 2.0.3.6-SNAPSHOT
 jar files. If you need standalone .jar files with all dependencies (for server mode), clone [this repository](https://github.com/graphaware/neo4j-framework-build.git)
and run `mvn clean install` on that.

### Note on Versioning Scheme

The version number has two parts. The first three numbers indicate compatibility with a Neo4j version.
 The last number is the version of the framework. For example, version 2.0.3.3 is version 3 of the framework
 compatible with Neo4j 2.0.3

<a name="server"/>
GraphAware Server
-----------------

With GraphAware Framework in the _plugins_ directory of your Neo4j server installation, it is possible to develop Spring
MVC controllers that have the Neo4j database wired in as `GraphDatabaseService`.

For example, to develop an API endpoint that counts all the nodes in the database using Spring MVC, create the following
controller:

```java
/**
*  Sample REST API for counting all nodes in the database.
*/
@Controller
@RequestMapping("count")
public class NodeCountApi {

   private final GraphDatabaseService database;

   @Autowired
   public NodeCountApi(GraphDatabaseService database) {
       this.database = database;
   }

   @RequestMapping(method = RequestMethod.GET)
   @ResponseBody
   public long count() {
       try (Transaction tx = database.beginTx()) {
           return Iterables.count(GlobalGraphOperations.at(database).getAllNodes());
       }
   }
}
```

**WARNING:** Your class must reside in a `com`, `net`, or `org` top-level
package and one of the package levels must be called `graphaware`. For example, `com.mycompany.graphaware.NodeCountApi`
 will do. This is currently a limitation and will be addressed in a future release.

Compile this code into a .jar file (with dependencies, see below) and place it into the _plugins_ directory of your
Neo4j server installation. You will then be able to issue a `GET` request to `http://your-neo4j-url:7474/graphaware/count`
and receive the number of nodes in the database in the response body. Note that the `graphaware` part of the URL must be
there and cannot (yet) be configured.

To get started quickly, use the provided Maven archetype by typing:

    mvn archetype:generate -DarchetypeGroupId=com.graphaware.neo4j -DarchetypeArtifactId=graphaware-springmvc-maven-archetype -DarchetypeVersion=2.0.3.5

To get started manually, you will need the following dependencies:

```xml
<dependencies>

    <!-- GraphAware Framework -->
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>common</artifactId>
        <version>2.0.3.5</version>
        <scope>provided</scope>
    </dependency>

    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>api</artifactId>
        <version>2.0.3.5</version>
        <scope>provided</scope>
    </dependency>

    <!-- Spring Framework -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webmvc</artifactId>
        <version>4.0.0.RELEASE</version>
        <scope>provided</scope>
    </dependency>

    <!-- Neo4j -->
    <dependency>
        <groupId>org.neo4j</groupId>
        <artifactId>neo4j</artifactId>
        <version>2.0.3.5</version>
        <scope>provided</scope>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>server-community</artifactId>
        <version>2.0.3.5</version>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <version>2.0.3.5</version>
        <artifactId>tests</artifactId>
        <scope>test</scope>
    </dependency>

</dependencies>
```

It is also a good idea to use make sure the resulting .jar file includes all the dependencies, if you use any external
ones that aren't listed above:

```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-assembly-plugin</artifactId>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>attached</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <finalName>your-api-module-name-${project.version}</finalName>
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
                <appendAssemblyId>false</appendAssemblyId>
            </configuration>
        </plugin>
    </plugins>
</build>
```


<a name="runtime"/>
GraphAware Runtime
------------------

GraphAware Runtime is useful when you require functionality that transparently alters transactions or prevents them from
happening at all. For example, you might want to:
* Enforce specific constraints on the graph schema
* Use optimistic locking to prevent updates of out-of-date data
* Improve performance by building (and keeping in sync) in-graph indices
* Improve performance of supernodes
* Prevent certain parts of the graph from being deleted
* Timestamp modifications
* Find out what the latest graph modifications that took place were
* Write trigger-like functionality (which can actually be unit-tested!)
* ... and much more

### Building a GraphAware Runtime Module

To get started quickly, use the provided Maven archetype by typing:

    mvn archetype:generate -DarchetypeGroupId=com.graphaware.neo4j -DarchetypeArtifactId=graphaware-runtime-module-maven-archetype -DarchetypeVersion=2.0.3.5

To start from scratch, you will need the following dependencies in your pom.xml

```xml
<dependencies>
    ...
    <!-- needed if the module exposes an API -->
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>api</artifactId>
        <version>2.0.3.5</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>common</artifactId>
        <version>2.0.3.5</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>runtime</artifactId>
        <version>2.0.3.5</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>tests</artifactId>
        <version>2.0.3.5</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>tx-api</artifactId>
        <version>2.0.3.5</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>tx-executor</artifactId>
        <version>2.0.3.5</version>
        <scope>provided</scope>
    </dependency>

    ...
</dependencies>
```

Again, if using other dependencies, you need to make sure the resulting .jar file includes all the dependencies:

```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-assembly-plugin</artifactId>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>attached</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <finalName>your-api-module-name-${project.version}</finalName>
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
                <appendAssemblyId>false</appendAssemblyId>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Your module then needs to be built by implementing the <a href="http://graphaware.com/site/framework/latest/apidocs/com/graphaware/runtime/GraphAwareRuntimeModule.html" target="_blank">GraphAwareRuntimeModule</a> interface.
As an example, if we were to build a module that logs all changes performed in a transaction to system out in a human
readable form, we would write the following code:

```java
   TBD //todo
```

### Using GraphAware Runtime (Embedded Mode)

Using the GraphAware Runtime only makes sense when there is a GraphAware Runtime Module to go with it. Assuming we want
to use the runtime with the `ChangeLoggingRuntimeModule` we've built in the previous section in embedded mode, all we
need to do is instantiate the runtime and register the module with it:

```java
   TBD //todo
```

The modules are presented with the about-to-be-committed transaction data in the order in which they've been registered.

### Using GraphAware Runtime (Server Mode)

Provided that the GraphAware Framework .jar file is present in the Neo4j `plugins` directory, the following line needs to
be added to `neo4j.properties` in order for the GraphAware Runtime to be enabled:

`com.graphaware.runtime.enabled=true`

GraphAware Runtime Modules can be registered using the following mechanism. For instance, the `ChangeLoggingRuntimeModule`
with its `ChangeLoggingRuntimeModuleBootstrapper` (assuming it lives in `com.graphaware.demo` package) can be registered
with the runtime using the following line

`com.graphaware.module.changelogger.1=com.graphaware.demo.ChangeLoggingRuntimeModuleBootstrapper`

which means that the `ChangeLoggingRuntimeModule` will be the first runtime module registered with the runtime with ID
equal to "changelogger".

### Using GraphAware Runtime (Batch Inserters)

For populating a database quickly, people sometimes use Neo4j `BatchInserter`s. The framework can be used with those as
well, in the following fashion:

```java
    tbd
```

### Existing GraphAware Runtime Modules

So far, the only production-ready GraphAware module is the <a href="https://github.com/graphaware/neo4j-relcount" target="_blank">relationship count module</a>.

<a name="javadev"/>
Features for Java Developers
----------------------------

Whether or not you use the code in this repository as a framework or runtime as described above, you can always add it
as a dependency and take advantage of its useful features.

<a name="graphaware-test"/>
### GraphAware Test

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

<a name="perftest"/>
#### Performance Testing
TBD

<a name="apitest"/>
#### API Testing
TBD

<a name="tx-api"/>
### Improved Transaction Event API

In `com.graphaware.tx.event`, you will find a decorator of the Neo4j Transaction Event API (called `TransactionData`).
Before a transaction commits, the improved API allows users to traverse the new version of the graph (as it will be
after the transaction commits), as well as a "snapshot" of the old graph (as it was before the transaction started).
It provides a clean API to access information about changes performed by the transaction as well as the option to
perform additional changes.

The least you can gain from using this functionality is avoiding `java.lang.IllegalStateException: Node/Relationship has
been deleted in this tx` when trying to access properties of nodes/relationships deleted in a transaction. You can also
easily access relationships/nodes that were changed and/or deleted in a transaction, again completely exception-free.

#### Usage

To use the API, simply instantiate one of the `ImprovedTransactionData` implementations.
`LazyTransactionData` is recommended as it is the easiest one to use.

```java
 GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

 database.registerTransactionEventHandler(new TransactionEventHandler<Object>() {
     @Override
     public Object beforeCommit(TransactionData data) throws Exception {
         ImprovedTransactionData improvedTransactionData = new LazyTransactionData(data);

         //have fun here with improvedTransactionData!

         return null;
     }

     @Override
     public void afterCommit(TransactionData data, Object state) {
     }

     @Override
     public void afterRollback(TransactionData data, Object state) {
     }
 });
```

`FilteredTransactionData` can be used instead. They effectively hide portions of the graph, including any changes performed
on nodes and relationships that are not interesting. `InclusionStrategies` are used to convey the information about
what is interesting and what is not. For example, of only nodes with name equal to "Two" and no relationships at all
are of interest, the example above could be modified as follows:

```java
GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

database.registerTransactionEventHandler(new TransactionEventHandler<Object>() {
    @Override
    public Object beforeCommit(TransactionData data) throws Exception {
        InclusionStrategies inclusionStrategies = InclusionStrategiesImpl.all()
                .with(new IncludeAllBusinessNodes() {
                    @Override
                    protected boolean doInclude(Node node) {
                        return node.getProperty("name", "default").equals("Two");
                    }

                    @Override
                    public String asString() {
                        return "includeOnlyNodeWithNameEqualToTwo";
                    }
                })
                .with(IncludeNoRelationships.getInstance());

        ImprovedTransactionData improvedTransactionData = new FilteredTransactionData(new LazyTransactionData(data), inclusionStrategies);

        //have fun here with improvedTransactionData!

        return null;
    }

    @Override
    public void afterCommit(TransactionData data, Object state) {
    }

    @Override
    public void afterRollback(TransactionData data, Object state) {
    }
});
```

#### Example Scenario

Let's illustrate why this might be useful on a very simple example. Let's say we have a `FRIEND_OF` relationship in the
system and it has a `strength` property indicating the strength of the friendship from 1 to 3. Let's further assume that
we are interested in the total strength of all `FRIEND_OF` relationships in the entire system.

We'll achieve this by creating a custom transaction event handler that keeps track of the total strength. While not an
ideal choice from a system throughput perspective, let's say for the sake of simplicity that we are going to store the
total strength on the root node (with ID=0) as a `totalFriendshipStrength` property.

```java
public class TotalFriendshipStrengthCounter implements TransactionEventHandler<Void> {
    private static final RelationshipType FRIEND_OF = DynamicRelationshipType.withName("FRIEND_OF");
    private static final String STRENGTH = "strength";
    public static final String TOTAL_FRIENDSHIP_STRENGTH = "totalFriendshipStrength";

    private final GraphDatabaseService database;

    public TotalFriendshipStrengthCounter(GraphDatabaseService database) {
        this.database = database;
    }

    @Override
    public Void beforeCommit(TransactionData data) throws Exception {
        ImprovedTransactionData improvedTransactionData = new LazyTransactionData(data);

        int delta = 0;

        //handle new friendships
        for (Relationship newFriendship : improvedTransactionData.getAllCreatedRelationships()) {
            if (newFriendship.isType(FRIEND_OF)) {
                delta += (int) newFriendship.getProperty(STRENGTH, 0);
            }
        }

        //handle changed friendships
        for (Change<Relationship> changedFriendship : improvedTransactionData.getAllChangedRelationships()) {
            if (changedFriendship.getPrevious().isType(FRIEND_OF)) {
                delta -= (int) changedFriendship.getPrevious().getProperty(STRENGTH, 0);
                delta += (int) changedFriendship.getCurrent().getProperty(STRENGTH, 0);
            }
        }

        //handle deleted friendships
        for (Relationship deletedFriendship : improvedTransactionData.getAllDeletedRelationships()) {
            if (deletedFriendship.isType(FRIEND_OF)) {
                delta -= (int) deletedFriendship.getProperty(STRENGTH, 0);
            }
        }

        Node root = database.getNodeById(0);
        root.setProperty(TOTAL_FRIENDSHIP_STRENGTH, (int) root.getProperty(TOTAL_FRIENDSHIP_STRENGTH, 0) + delta);

        return null;
    }

    @Override
    public void afterCommit(TransactionData data, Void state) {
    }

    @Override
    public void afterRollback(TransactionData data, Void state) {
    }
}
```

All that remains is registering this event handler on the database:

```java
GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();
database.registerTransactionEventHandler(new TotalFriendshipStrengthCounter(database));
```

`TotalFriendshipStrengthCountingDemo` demonstrates the entire example.

#### Usage in Detail

The API categorizes `PropertyContainer`s, i.e. `Node`s and `Relationship`s into:

* created in this transaction
* deleted in this transaction
* changed in this transaction, i.e those with at least one property created, deleted, or changed
* untouched by this transaction

Users can find out, whether a `PropertyContainer` has been created, deleted, or changed in this transaction and obtain
all the created, deleted, and changed PropertyContainers.

Properties that have been created, deleted, and changed in the transaction are categorized by the changed `PropertyContainer`
 they belong to. Users can find out, which properties have been created, deleted, and changed for a given changed
 `PropertyContainer` and check, whether a given property for a given changed `PropertyContainer` has been created,
 deleted, or changed.

Properties of created `PropertyContainer`s are available through the actual created `PropertyContainer`.
Properties of deleted `PropertyContainer`s (as they were before the transaction started) are available through the
snapshot of the deleted `PropertyContainer`, obtained by calling `getDeleted(org.neo4j.graphdb.Node)` or
`getDeleted(org.neo4j.graphdb.Relationship)`. Properties of created and deleted containers will not be returned by
`changedProperties(org.neo4j.graphdb.Node)` and `changedProperties(org.neo4j.graphdb.Relationship)` as these only return
changed properties of changed `PropertyContainer`s.

Changed `PropertyContainer`s and properties are wrapped in a `Change` object which holds the previous state of the
object before the transaction started, and the current state of the object (when the transaction commits).

All created `PropertyContainer`s and properties and current versions of changed `PropertyContainer`s and properties can
be accessed by native Neo4j API and the traversal API as one would expect. For example, one can traverse the graph starting
 from a newly created node, using a mixture of newly created and already existing relationships. In other words, one can
  traverse the graph as if the transaction has already been committed. This is similar to using TransactionData.

A major difference between this API and `TransactionData`, however, is what one can do with the returned information
about deleted `PropertyContainer`s and properties and the previous versions thereof. With this API, one can traverse a
_snapshot_ of the graph as it was before the transaction started. As opposed to the `TransactionData` API, this will not
result in exceptions being thrown.

For example, one can start traversing the graph from a deleted `Node`, or the previous version of a changed `Node`.
Such traversal will only traverse `Relationship`s that existed before the transaction started and will return properties
and their values as they were before the transaction started. This is achieved using `NodeSnapshot` and `RelationshipSnapshot`
 decorators.

One can even perform additional mutating operations on the previous version (snapshot) of the graph, provided that the
mutated objects have been changed in the transaction (as opposed to deleted). Mutating deleted `PropertyContainer`s and
properties does not make any sense and will cause exceptions.

To summarize, this API gives access to two versions of the same graph. Through created `PropertyContainer`s and/or their
 current versions, one can traverse the current version of the graph as it will be after the transaction commits.
 Through deleted and/or previous versions of `PropertyContainer`s, one can traverse the previous snapshot of the graph,
 as it was before the transaction started.

#### Batch Usage

In case you would like to use the Neo4j `BatchInserter` API but still get access to `ImprovedTransactionData` during
batch insert operations, `TransactionSimulatingBatchInserterImpl` is the class for you. It is a `BatchInserter` but
allows `TransactionEventHandler`s to be registered on it. It then simulates a transaction commit every once in a while
(configurable, please refer to JavaDoc).

As a `GraphDatabaseService` equivalent for batch inserts, this project provides `TransactionSimulatingBatchGraphDatabase`
for completeness, but its usage is discouraged.

<a name="tx-executor"/>
### Simplified Transactional Operations

Every mutating operation in Neo4j must run within the context of a transaction. The code dealing with that typically
involves try-catch blocks and looks something like this:

 ```java
 Transaction tx = database.beginTx();
 try {
     //do something useful, can throw a business exception
     tx.success();
 } catch (RuntimeException e) {
     //deal with a business exception
     tx.failure();
 } finally {
     tx.finish(); //can throw a database exception
 }
 ```

 As of Neo4j 2.0, this could be simplified to this:

 ```java
try (Transaction tx = database.beginTx()) {
    //do something useful, can throw a business exception
    tx.success();
}
 ```

GraphAware provides an alternative, callback-based API called `TransactionExecutor` in `com.graphaware.tx.executor`.
`SimpleTransactionExecutor` is a simple implementation thereof and can be used on an instance-per-database basis.
 Since you will typically run a single in-process database instance, you will also only need a single `SimpleTransactionExecutor`.

To create an empty node in a database, you would write something like this.

```java
    GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase(); //only for demo, use your own persistent one!
    TransactionExecutor executor = new SimpleTransactionExecutor(database);

    executor.executeInTransaction(new VoidReturningCallback() {
        @Override
        public void doInTx(GraphDatabaseService database) {
            database.createNode();
        }
    });
```

You have the option of selecting an `ExceptionHandlingStrategy`. By default, if an exception occurs, the transaction will be
 rolled back and the exception re-thrown. This is true for both application/business exceptions (i.e. the exception your
 code throws in the `doInTx` method above), and Neo4j exceptions (e.g. constraint violations). This default strategy is
 called `RethrowException`.

The other available implementation of `ExceptionHandlingStrategy` is `KeepCalmAndCarryOn`. It still rolls back the transaction
in case an exception occurs, but it does not re-throw it (only logs it). To use a different `ExceptionHandlingStrategy`, perhaps
  one that you implement yourself, just pass it in to the `executeInTransaction` method:

```java
  executor.executeInTransaction(transactionCallback, KeepCalmAndCarryOn.getInstance());
```

<a name="batch-tx"/>
### Batch Transactional Operations

It is a common requirement to execute operations in batches. For instance, you might want to populate the database with
data from a CSV file, or just some generated dummy data for testing. If there are many such operations (let's say 10,000
or more), doing it all in one transaction isn't the most memory-efficient approach. On the other hand, a new transaction
for each operation results in too much overhead. For some use-cases, `BatchInserters` provided by Neo4j suffice. However,
operations performed using these do not run in transactions and have some other limitations (such as no node/relationship
 delete capabilities).

GraphAware can help here with `BatchTransactionExecutor`s. There are a few of them:

#### Input-Based Batch Operations

If you have some input, such as lines from a CSV file or a result of a Neo4j traversal, and you want to perform an operation
for each item of such input, use `IterableInputBatchTransactionExecutor`. As the name suggests, the input needs to be in the form
of an `Iterable`. Additionally, you need to define a `UnitOfWork`, which will be executed against the database for each
input item. After a specified number of batch operations have been executed, the current transaction is committed and a
new one started, until we run out of input items to process.

For example, if you were to create a number of nodes from a list of node names, you would do something like this:

```java
    GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase(); //only for demo, use your own persistent one!

    List<String> nodeNames = Arrays.asList("Name1", "Name2", "Name3");  //there will be many more

    int batchSize = 10;
    BatchTransactionExecutor executor = new IterableInputBatchTransactionExecutor<>(database, batchSize, nodeNames, new UnitOfWork<String>() {
        @Override
        public void execute(GraphDatabaseService database, String nodeName, int batchNumber, int stepNumber) {
            Node node = database.createNode();
            node.setProperty("name", nodeName);
        }
    });

    executor.execute();
```

#### Batch Operations with Generated Input or No Input

In case you wish to do something input-independent, for example just generate a number of nodes with random names, you
can use the `NoInputBatchTransactionExecutor`.

First, you would create an implementation of `UnitOfWork<NullItem>`, which is a unit of work expecting no input:

```java
    /**
     * Unit of work that creates an empty node with random name. Singleton.
     */
    public class CreateRandomNode implements UnitOfWork<NullItem> {
        private static final CreateRandomNode INSTANCE = new CreateRandomNode();

        public static CreateRandomNode getInstance() {
            return INSTANCE;
        }

        private CreateRandomNode() {
        }

        @Override
        public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
            Node node = database.createNode();
            node.setProperty("name", UUID.randomUUID());
        }
    }
```

Then, you would use it in `NoInputBatchTransactionExecutor`:

```java
    //create 100,000 nodes in batches of 1,000:
    int batchSize = 1000;
    int noNodes = 100000;
    BatchTransactionExecutor batchExecutor = new NoInputBatchTransactionExecutor(database, batchSize, noNodes, CreateRandomNode.getInstance());
    batchExecutor.execute();
```

#### Multi-Threaded Batch Operations

If you wish to execute any batch operation using more than one thread, you can use the `MultiThreadedBatchTransactionExecutor`
 as a decorator of any `BatchTransactionExecutor`. For example, to execute the above example using 4 threads:

```java
    int batchSize = 1000;
    int noNodes = 100000;
    BatchTransactionExecutor batchExecutor = new NoInputBatchTransactionExecutor(database, batchSize, noNodes, CreateRandomNode.getInstance());
    BatchTransactionExecutor multiThreadedExecutor = new MultiThreadedBatchTransactionExecutor(batchExecutor, 4);
    multiThreadedExecutor.execute();
```

<a name="utils"/>
### Miscellaneous Utilities

The following functionality is also provided:

* Arrays (see `ArrayUtils`)
    * Determine if an object is a primitive array
    * Convert an array to a String representation
    * Check equality of two `Object`s which may or may not be arrays
    * Check equality of two `Map<String, Object>` instances, where the `Object`-typed values may or may not be arrays

* Property Containers (see `PropertyContainerUtils`)
    * Convert a `PropertyContainer` to a Map of properties
    * Delete nodes with all their relationships automatically, avoiding a `org.neo4j.kernel.impl.nioneo.store.ConstraintViolationException: Node record Node[xxx] still has relationships`, using `DeleteUtils.deleteNodeAndRelationships(node);`

* Relationship Directions
    * The need to determine the direction of a relationship is quite common. The `Relationship` object does not provide the
      functionality for the obvious reason that it depends on "who's point of view we're talking about". In order to resolve
      a direction from a specific Node's point of view, use `DirectionUtils.resolveDirection(Relationship relationship, Node pointOfView);`

* Iterables (see `IterableUtils` in tests)
    * Count iterables by iterating over them, unless they're a collection in which case just return `size()`
    * Randomize iterables by iterating over them and shuffling them, unless they're a collection in which case just shuffle
    * Convert iterables to lists
    * Check if iterable contains an object by iterating over the iterable, unless it's a collection in which case just return `contains(..)`

... and more, please see JavaDoc.

License
-------

Copyright (c) 2014 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.