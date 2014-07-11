GraphAware Runtime
==================

This (Maven) module is part of the [GraphAware Neo4j Framework](https://github.com/graphaware/neo4j-framework).

### Introduction

GraphAware Runtime is a runtime component of the GraphAware Framework.

GraphAware Runtime is useful when you:
* require functionality that transparently alters transactions or prevents them from happening at all. For example, you might want to:
    * Enforce specific constraints on the graph schema
    * Use optimistic locking to prevent updates of out-of-date data
    * Improve performance by building (and keeping in sync) in-graph indices
    * Improve performance of supernodes
    * Prevent certain parts of the graph from being deleted
    * Timestamp modifications
    * Find out what the latest graph modifications that took place were
    * Write trigger-like functionality (which can actually be unit-tested!)
    * ... and much more
* need to compute something continuously in the background, writing the results back to the graph. For example, you might want to:
    * delete expired data
    * compute PageRank
    * compute maximum flow between points in the network
    * pre-compute similarities between people
    * pre-compute recommendations to people
    * ... and much more

To achieve any of the above, developers need to create a GraphAware Runtime Module. There are two types:
* [`TxDrivenModule`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/runtime/module/TxDrivenModule.html) driven by ongoing transactions
* [`TimerDrivenModule`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/runtime/module/TimerDrivenModule.html) driven by a timer (periodic)

A single module can implement both interfaces and thus be driven by both transactions and the timer.

The following Runtime Modules are developed and provided by GraphAware:
* [Relationship Count Module](https://github.com/graphaware/neo4j-relcount)
* [Change Feed Module](https://github.com/graphaware/neo4j-changefeed) (Work In Progress!)

### Getting the Runtime

GraphAware Runtime ships with the Framework. By [downloading](http://graphaware.com/downloads) the appropriate release
of the GraphAware Framework and placing it into the _plugins_ directory of your Neo4 server, you have the Runtime as well.
However, it is disabled by default and needs to be explicitly enabled (read on).

GraphAware Runtime can also be used for embedded Neo4j deployments. When using embedded Neo4j,
add the following snippet to your pom.xml:

```xml
<dependency>
    <groupId>com.graphaware.neo4j</groupId>
    <artifactId>runtime</artifactId>
    <version>2.1.2.9</version>
</dependency>
```

### Using GraphAware Runtime (Server Mode)

Using the GraphAware Runtime only makes sense when there is a GraphAware Runtime Module (or more) to go with it.
Each Module should provide a [`RuntimeModuleBootstrapper`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/runtime/module/RuntimeModuleBootstrapper.html)
for bootstrapping the module in server mode.

Let us assume we have a module called `FriendshipStrengthModule` with a corresponding `FriendshipStrengthModuleBootstrapper`
that we would like to use with the runtime (we will develop the module and the bootstrapper later). Provided that
the GraphAware Framework .jar file is present in the Neo4j _plugins_ directory, the following line needs to
be added to _neo4j.properties_ in order for the GraphAware Runtime to be enabled:

`com.graphaware.runtime.enabled=true`

Then, assuming the module bootstrapper lives in `com.graphaware.example.module` package, it must be registered
with the runtime using the following line in _neo4j.properties_:

`com.graphaware.module.FSM.1=com.graphaware.example.module.FriendshipStrengthModuleBootstrapper`

which means that the `FriendshipStrengthModule` bootsrapped by `FriendshipStrengthModuleBootstrapper` will be the first
Module registered with the Runtime with ID equal to "FSM".

### Using GraphAware Runtime (Embedded Mode)

To use the runtime and modules programmatically, all we need to do is instantiate the runtime and register the module with it:

```java
GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase(); //replace with a real DB
GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
runtime.registerModule(new FriendshipStrengthModule("FSM", database));

//optionally start the runtime - it will start automatically if this isn't done
runtime.start();
```

It is, however, also possible to pass a _neo4j.properties_ file to the database. Same rules as in the server mode
 apply. For example, if we have a neo4j-friendship.properties file with the following lines

```
# GraphAware Config
com.graphaware.runtime.enabled=true
com.graphaware.module.FSM.1=com.graphaware.example.module.FriendshipStrengthModuleBootstrapper
```

the runtime and modules will be configured correctly by just doing

```java
database = new TestGraphDatabaseFactory()
              .newImpermanentDatabaseBuilder()
              .loadPropertiesFromFile("neo4j-friendship.properties")
              .newGraphDatabase();
```

**NOTE:** Modules are presented with the about-to-be-committed transaction data or asked to do work on scheduled basis
in the order in which they've been registered.

### Building a Transaction-Driven GraphAware Runtime Module

**Example:** An example is provided in [examples/friendship-strength-counter-module](../examples/friendship-strength-counter-module).

To get started quickly, use the provided Maven archetype by typing:

    mvn archetype:generate -DarchetypeGroupId=com.graphaware.neo4j -DarchetypeArtifactId=graphaware-runtime-module-maven-archetype -DarchetypeVersion=2.1.2.9

To start from scratch, you will need the following dependencies in your pom.xml

```xml
<dependencies>
    ...
    <!-- needed if the module exposes an API -->
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>api</artifactId>
        <version>2.1.2.9</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>common</artifactId>
        <version>2.1.2.9</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>runtime</artifactId>
        <version>2.1.2.9</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>tests</artifactId>
        <version>2.1.2.9</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>tx-api</artifactId>
        <version>2.1.2.9</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>tx-executor</artifactId>
        <version>2.1.2.9</version>
        <scope>provided</scope>
    </dependency>

    ...
</dependencies>
```

Again, if using other dependencies, you need to make sure the resulting .jar file includes all the dependencies. [See here](../server#alldependencies).

Your module then needs to be built by implementing the [`TxDrivenModule`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/runtime/module/TxDrivenModule.html) interface.
An example is provided in [examples/friendship-strength-counter-module](../examples/friendship-strength-counter-module). This computes the sum of all `strength` properties
on `FRIEND_OF` relationships and keeps it up to data, written to a special node created for that purpose. It also has
a REST API that can be queried for the total friendship strength value.

### Building a Timer-Driven GraphAware Runtime Module

Similarly, your module can implement the the [`TimerDrivenModule`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/runtime/module/TimerDrivenModule.html) interface
in order to be able to perform computations on the graph that are automatically scheduled. The framework will detect quiet
periods in your database and increase the rate at which modules perform behind-the-scenes computations. During busy periods, naturally,
the rate is decreased.

Each unit of work, implemented by the `doSomeWork` method on `TimerDrivenModule`, should be a short computation that
writes some results back to the graph. This is very useful for iterative algorithms like PageRank, which are too expensive
to compute in real-time.

### Building a Module Bootstrapper

GraphAware Runtime Modules can be registered in server mode only if there is an implementation of [`RuntimeModuleBootstrapper`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/runtime/module/RuntimeModuleBootstrapper.html)
It is simple to create one:

```java
/**
 * {@link com.graphaware.runtime.module.RuntimeModuleBootstrapper} for {@link FriendshipStrengthModule}.
 */
public class FriendshipStrengthModuleBootstrapper implements RuntimeModuleBootstrapper {

    /**
     * {@inheritDoc}
     */
    @Override
    public RuntimeModule bootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database) {
        return new FriendshipStrengthModule(moduleId, database);
    }
}
```

License
-------

Copyright (c) 2014 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
