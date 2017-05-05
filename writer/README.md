GraphAware Writer
=================

This (Maven) module is part of the [GraphAware Neo4j Framework](https://github.com/graphaware/neo4j-framework).

The code in this module introduces an abstraction over writing to Neo4j, so that the mechanism can be switched. The main
purpose of this all is increasing the write throughput of Neo4j. The target audience of this module are advanced Neo4j
users, mostly Java developers.

### Getting the Module

Add the following snippet to your pom.xml:

```xml
<dependency>
    <groupId>com.graphaware.neo4j</groupId>
    <artifactId>writer-api</artifactId>
    <version>3.1.4.49</version>
</dependency>

<dependency>
    <groupId>com.graphaware.neo4j</groupId>
    <artifactId>writer</artifactId>
    <version>3.1.4.49</version>
</dependency>
```

### Writing to Neo4j

Normally, you would write to Neo4j using the following mechanism:

```java
final GraphDatabaseService database = ... //get or create a database

//Creating a node using vanilla Neo4j
try (Transaction tx = database.beginTx()) {
    database.createNode();
    tx.success();
}
```

#### Single Threaded Writes - Transaction per Task

In write-heavy applications, where multiple threads write to the same parts of the graph, it is sometimes the case that
frequent deadlocks occur. One of the transactions participating in the deadlock scenario has to be failed, which Neo4j
does automatically.

One way around this problem is to only use a single thread to write to the database. This guarantees no deadlocks will occur.
Using the GraphAware Writer module (which comes with the GraphAware Framework), the above example can be re-written as
follows:

```java
final GraphDatabaseService database = ... //get or create a database
int queueSize = 1000; //optional, default is 10,000
Neo4jWriter writer = new TxPerTaskWriter(database, queueSize); //only one of these per application!

writer.write(new Runnable() {
    @Override
    public void run() {
        database.createNode();
    }
});
```

In the example above, the call to `writer` returns immediately and the `Runnable` is submitted to a FIFO queue processed in
a single thread. When the queue is full, i.e. exceeds the configured capacity, submitted `Runnable`s are dropped and a
warning is logged.

It is possible to make the calling thread block until the write operation gets executed. This is useful in situations where
we want to prevent the queue from getting too large, trading off some write throughput. It is necessary to do this in
scenarios where the write operation should return a value. In that case, the operation must be presented as a `Callable`
rather than `Runnable`.

```java
Long nodeId = writer.write(new Callable<Long>() {
    @Override
    public Long call() throws Exception {
        return database.createNode().getId();
    }
}, Thread.currentThread().getName(), 500);
```

In the example above, we are creating a node and are interested in its ID. The first parameter to the `write` method
is the `Callable` that returns the ID upon successful execution. The second parameter is an identifier of the "task"
we're submitting, mainly for logging purposes. It could be the name of the Controller responsible for the task, or simply
a thread name as illustrated. Finally, the third parameter is the number of milliseconds the submitting thread will block
waiting for the submitted `Callable` to return a value. If this time is exceeded, `null` is returned, but the `Callable`
remains in the queue and will be eventually executed. It is also possible to wait for the execution of a `Runnable` to
finish using the exact same mechanism, by converting it to a `Callable` using `Executors.callable(Runnable task)`
(from `java.util.concurrent`).

#### Single Threaded Writes - Batching Tasks

In case the above approach doesn't provide high enough write throughput, it is possible to replace the "transaction
per task" strategy with a strategy that batches the submitted tasks and executes them in a single transaction. Naturally,
there is a risk that a single operation that causes a rollback (e.g. it deletes a node without deleting its relationships)
causes the entire batch to be rolled back.

```java
final GraphDatabaseService database = ... //get or create a database
int queueSize = 1000; //optional, default is 10,000
int batchSize = 1000; //optional, default is 1,000
Neo4jWriter writer = new BatchWriter(database, queueSize, batchSize); //only one of these per application!

Long nodeId = writer.write(new Callable<Long>() {
    @Override
    public Long call() throws Exception {
        return database.createNode().getId();
    }
}, Thread.currentThread().getName(), 500);
```

#### Multi Threaded Writes - Default Behaviour

Finally, if you would like to revert to the default writing strategy without changing much code, there is the `DefaultWriter`
that simply executes every task synchronously, in a separate transaction:

```java
final GraphDatabaseService database = ... //get or create a database
Neo4jWriter writer = new DefaultWriter(database); //only one of these per application!

Long nodeId = writer.write(new Callable<Long>() {
    @Override
    public Long call() throws Exception {
        return database.createNode().getId();
    }
}, Thread.currentThread().getName(), 0); //the number of ms to block is ignored
```

License
-------

Copyright (c) 2013-2017 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
