Transaction Executors
=====================

This (Maven) module is part of the [GraphAware Neo4j Framework](https://github.com/graphaware/neo4j-framework).

The target audience of this module are advanced Neo4j users, mostly Java developers.

### Getting the Module

Add the following snippet to your pom.xml:

```xml
<dependency>
    <groupId>com.graphaware.neo4j</groupId>
    <artifactId>tx-executor</artifactId>
    <version>3.2.1.50</version>
</dependency>
```

### Alternative Transactional Operations

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

GraphAware provides an alternative, callback-based API called [`TransactionExecutor`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/tx/executor/single/TransactionExecutor.html).
[`SimpleTransactionExecutor`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/tx/executor/single/SimpleTransactionExecutor.html) is a simple implementation thereof and can be used on an instance-per-database basis.
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

You have the option of selecting an [`ExceptionHandlingStrategy`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/tx/executor/single/ExceptionHandlingStrategy.html). By default, if an exception occurs, the transaction will be
 rolled back and the exception re-thrown. This is true for both application/business exceptions (i.e. the exception your
 code throws in the `doInTx` method above), and Neo4j exceptions (e.g. constraint violations). This default strategy is
 called [`RethrowException`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/tx/executor/single/RethrowException.html).

The other available implementation of `ExceptionHandlingStrategy` is [`KeepCalmAndCarryOn`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/tx/executor/single/KeepCalmAndCarryOn.html). It still rolls back the transaction
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

GraphAware can help here with [`BatchTransactionExecutor`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/tx/executor/batch/BatchTransactionExecutor.html)s. There are a few of them:

#### Input-Based Batch Operations

If you have some input, such as lines from a CSV file or a result of a Neo4j traversal, and you want to perform an operation
for each item of such input, use [`IterableInputBatchTransactionExecutor`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/tx/executor/batch/IterableInputBatchTransactionExecutor.html). As the name suggests, the input needs to be in the form
of an `Iterable`. Additionally, you need to define a [`UnitOfWork`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/tx/executor/batch/UnitOfWork.html), which will be executed against the database for each
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

In case the input itself is an `Iterable` read from the database, it will need to be read inside a transaction, so an
`Iterable`-returning [`TransactionalInput`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/tx/executor/input/TransactionalInput.html) should be passed in instead of a pure `Iterable`. For example, to assign a
 UUID to all existing nodes in the database, you would execute the following:

```java
GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase(); //only for demo, use your own persistent one!

BatchTransactionExecutor executor = new IterableInputBatchTransactionExecutor<>(
        database,
        1000,
        new TransactionalInput<>(database, 1000, new TransactionCallback<Iterable<Node>>() {
            @Override
            public Iterable<Node> doInTransaction(GraphDatabaseService database) throws Exception {
                return database.getAllNodes();
            }
        }),
        new UnitOfWork<Node>() {
            @Override
            public void execute(GraphDatabaseService database, Node node, int batchNumber, int stepNumber) {
                node.setProperty("uuid", UUID.randomUUID());
            }
        }
);

executor.execute();
```

which could be simplified by the provided `AllNodes` class to:

```java
GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase(); //only for demo, use your own persistent one!

BatchTransactionExecutor executor = new IterableInputBatchTransactionExecutor<>(
        database,
        1000,
        new AllNodes(database, 1000),
        new UnitOfWork<Node>() {
            @Override
            public void execute(GraphDatabaseService database, Node node, int batchNumber, int stepNumber) {
                node.setProperty("uuid", UUID.randomUUID());
            }
        }
);

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

Then, you would use it in [`NoInputBatchTransactionExecutor`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/tx/executor/batch/NoInputBatchTransactionExecutor.html):

```java
//create 100,000 nodes in batches of 1,000:
int batchSize = 1000;
int noNodes = 100000;
BatchTransactionExecutor batchExecutor = new NoInputBatchTransactionExecutor(database, batchSize, noNodes, CreateRandomNode.getInstance());
batchExecutor.execute();
```

#### Multi-Threaded Batch Operations

If you wish to execute any batch operation using more than one thread, you can use the [`MultiThreadedBatchTransactionExecutor`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/tx/executor/batch/MultiThreadedBatchTransactionExecutor.html)
 as a decorator of any `BatchTransactionExecutor`. For example, to execute the above example using 4 threads:

```java
int batchSize = 1000;
int noNodes = 100000;
BatchTransactionExecutor batchExecutor = new NoInputBatchTransactionExecutor(database, batchSize, noNodes, CreateRandomNode.getInstance());
BatchTransactionExecutor multiThreadedExecutor = new MultiThreadedBatchTransactionExecutor(batchExecutor, 4);
multiThreadedExecutor.execute();
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
