GraphAware Runtime
==================

This (Maven) module is part of the [GraphAware Neo4j Framework](https://github.com/graphaware/neo4j-framework).

### Introduction

GraphAware Runtime is a runtime component of the GraphAware Framework.

GraphAware Runtime is useful when you:
* require functionality that transparently alters transactions or prevents them from happening at all. For example, you might want to:
    * Enforce specific constraints on the graph schema
    * Maintain an in-graph index (like the [GraphAware TimeTree](https://github.com/graphaware/neo4j-timetree))
    * Use optimistic locking to prevent updates of out-of-date data
    * Improve performance by building (and keeping in sync) in-graph indices
    * Improve performance of supernodes
    * Prevent certain parts of the graph from being deleted
    * Timestamp modifications
    * Find out what the latest graph modifications that took place were
    * Write trigger-like functionality (which can actually be unit-tested!)
    * Integrate with third-party systems (see [`WriterBasedThirdPartyIntegrationModule`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/runtime/module/thirdparty/WriterBasedThirdPartyIntegrationModule.html)
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
* [`TimerDrivenModule`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/runtime/module/TimerDrivenModule.html) driven by a clever adaptive timer

A single module can implement both interfaces and thus be driven by both transactions and the timer.

The following Runtime Modules are developed and provided by GraphAware. They are useful in their own right, but also serve as reference implementations:
* [Node Rank Module](https://github.com/graphaware/neo4j-noderank)
* [UUID Module](https://github.com/graphaware/neo4j-uuid)
* [TimeTree Module](https://github.com/graphaware/neo4j-timetree) (the Runtime part is optional for the TimeTree)
* [Relationship Count Module](https://github.com/graphaware/neo4j-relcount) (retired)
* [Change Feed Module](https://github.com/graphaware/neo4j-changefeed) (retired)


### Getting the Runtime

GraphAware Runtime ships with the Framework. By [downloading](http://products.graphaware.com) the appropriate release
of the GraphAware Framework and placing it into the _plugins_ directory of your Neo4 server, you have the Runtime as well.
However, it is disabled by default and needs to be explicitly enabled (read on).

GraphAware Runtime can also be used for embedded Neo4j deployments. When using embedded Neo4j,
add the following snippet to your pom.xml:

```xml
<dependency>
    <groupId>com.graphaware.neo4j</groupId>
    <artifactId>runtime-api</artifactId>
    <version>3.3.0.51</version>
</dependency>
<dependency>
    <groupId>com.graphaware.neo4j</groupId>
    <artifactId>runtime</artifactId>
    <version>3.3.0.51</version>
</dependency>
```

### Using GraphAware Runtime (Server Mode)

Using the GraphAware Runtime only makes sense when there is a GraphAware Runtime Module (or more) to go with it.
Each Module should provide a [`RuntimeModuleBootstrapper`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/runtime/module/RuntimeModuleBootstrapper.html)
for bootstrapping the module in server mode.

Let us assume we have a module called `FriendshipStrengthModule` with a corresponding `FriendshipStrengthModuleBootstrapper`
that we would like to use with the runtime (we will develop the module and the bootstrapper later). Provided that
the GraphAware Framework .jar file is present in the Neo4j _plugins_ directory, the following line needs to
be added to _neo4j.conf_ in order for the GraphAware Runtime to be enabled:

`com.graphaware.runtime.enabled=true`

Then, assuming the module bootstrapper lives in `com.graphaware.example.module` package, it must be registered
with the runtime using the following line in _neo4j.conf_:

`com.graphaware.module.FSM.1=com.graphaware.example.module.FriendshipStrengthModuleBootstrapper`

which means that the `FriendshipStrengthModule` bootsrapped by `FriendshipStrengthModuleBootstrapper` will be the first
Module registered with the Runtime with ID equal to "FSM".

### Using GraphAware Runtime (Embedded Mode)

To use the runtime and modules programmatically, all we need to do is instantiate the runtime, register the module with it,
and start the runtime:

```java
GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase(); //replace with a real DB
GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
runtime.registerModule(new FriendshipStrengthModule("FSM", database));
runtime.start();
```

It is, however, also possible to pass a _neo4j.conf_ file to the database. Same rules as in the server mode
 apply. For example, if we have a neo4j-friendship.conf file with the following lines

```
# GraphAware Config
com.graphaware.runtime.enabled=true
com.graphaware.module.FSM.1=com.graphaware.example.module.FriendshipStrengthModuleBootstrapper
```

the runtime and modules will be configured correctly by just doing

```java
database = new TestGraphDatabaseFactory()
              .newImpermanentDatabaseBuilder()
              .loadPropertiesFromFile("neo4j-friendship.conf")
              .newGraphDatabase();

RuntimeRegistry.getStartedRuntime(database);  //this line is needed when configuring with properties file
```

**NOTE:** Modules are presented with the about-to-be-committed transaction data or asked to do work on scheduled basis
in the order in which they've been registered.

### Building a Transaction-Driven GraphAware Runtime Module

**Example:** An example is provided in [examples/friendship-strength-counter-module](../examples/friendship-strength-counter-module).

To get started quickly, copy the example above and modify to your needs.

To start from scratch, you will need the following dependencies in your pom.xml

```xml
<dependencies>
    ...
    <!-- needed if the module exposes an API -->
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>api</artifactId>
        <version>3.3.0.51</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>common</artifactId>
        <version>3.3.0.51</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>runtime-api</artifactId>
        <version>3.3.0.51</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>runtime</artifactId>
        <version>3.3.0.51</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>tests</artifactId>
        <version>3.3.0.51</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>tx-api</artifactId>
        <version>3.3.0.51</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>tx-executor</artifactId>
        <version>3.3.0.51</version>
        <scope>provided</scope>
    </dependency>
     <!-- needed if the module wants to use the Writer API -->
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>writer-api</artifactId>
        <version>3.3.0.51</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>writer</artifactId>
        <version>3.3.0.51</version>
        <scope>provided</scope>
    </dependency>
    ...
</dependencies>
```

If using other dependencies, you need to make sure the resulting .jar file includes all the dependencies. [See here](../server#alldependencies).

Your module then needs to be built by implementing the [`TxDrivenModule`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/runtime/module/TxDrivenModule.html) interface.
An example is provided in [examples/friendship-strength-counter-module](../examples/friendship-strength-counter-module). This computes the sum of all `strength` properties
on `FRIEND_OF` relationships and keeps it up to data, written to a special node created for that purpose. It also has
a REST API that can be queried for the total friendship strength value.

### Building a Timer-Driven GraphAware Runtime Module

Similarly, your module can implement the the [`TimerDrivenModule`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/runtime/module/TimerDrivenModule.html) interface
in order to be able to perform computations on the graph that are automatically scheduled. 

Each unit of work, implemented by the `doSomeWork` method on `TimerDrivenModule`, should be a short computation that
writes some results back to the graph. This is very useful for iterative algorithms like PageRank, which are too expensive
to compute in real-time.

The frequency with which the timer-driven modules have their `doSomeWork` method invoked is managed by the GraphAware
runtime internally.  In order to remain unintrusive, it's designed to recognise busy periods of database activity and
throttle back the regularity with which timer-driven modules are invoked.  Similarly, if the runtime realises that the
database is less busy, it will increase the invocation rate so that background processing performed by these scheduled
modules isn't delayed unnecessarily.

As of GraphAware Framework version 2.1.3.10, the following configuration properties can be added to _neo4j.conf_ in
order to configure the scheduling of these timer-driven modules.  The default values for each setting are also shown below.

```
# Timer-Driven Module Scheduling Configuration Settings

# Strategy used for timing - the options are "adaptive" (default) or "fixed". Fixed strategy uses the same delay all the
# time, whilst the adaptive one takes into account how busy the database is, as described above
com.graphaware.runtime.timing.strategy=adaptive

# The default number of milliseconds to wait between timer-driven module invocations (default = 2000)
com.graphaware.runtime.timing.delay=50

# The maximum number of milliseconds to wait between timer-driven module invocations (default = 5000)
com.graphaware.runtime.timing.maxDelay=100

# The minimum number of milliseconds to wait between timer-driven module invocations (default = 5)
com.graphaware.runtime.timing.minDelay=10

# The number of transactions per second that must be executed before the database is deemed to be busy (default = 100)
com.graphaware.runtime.timing.busyThreshold=100

# The maximum number of samples based on which the busyness of the database is determined (default = 200)
com.graphaware.runtime.timing.maxSamples=200

# The maximum number of milliseconds over which to measure the average busyness of the database (default = 2000)
com.graphaware.runtime.timing.maxTime=2000
```

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
### Configuration

`RuntimeModuleBootstrapper` gets its configuration passed into the `bootstrapModule` method as `Map` of String-String
key-value pairs. It is up to the developer to make sense of this, because it is specific to the module being bootstrapped.

There is, however, built-in support for parsing configuration the is meant to become an `InclusionPolicy`. For example,
let's we're building some indexing module that should only index *some* nodes. The user can provide the specification
in the property file in the form of a `NodeInclusionPolicy`. To convert a String, such as `hasLabel('MyLabel')` to
 a policy, use `com.graphaware.runtime.config.function.StringTo*InclusionPolicy` classes.

For instance,
` NodeInclusionPolicy policy = StringToNodeInclusionPolicy.getInstance().apply(config.get("nodes"));`
where config had an entry `com.graphaware.module.MY_MODULE.nodes=hasLabel('MyLabel')` will produce a `NodeInclusionPolicy`
that only includes nodes labelled `MyLabel`.

For more information on how to express `InclusionPolicies` using String, see [InclusionPolicies](https://github.com/graphaware/neo4j-framework/tree/master/common#inclusion-policies)

### Logging

Logging uses standard Neo4j logging infrastructure. When writing modules, use `com.graphaware.common.log.LoggerFactory` to
obtain `com.graphaware.common.log.Log`.

License
-------

Copyright (c) 2013-2017 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
