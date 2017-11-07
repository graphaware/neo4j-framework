Improved Transaction Event API
==============================

This (Maven) module is part of the [GraphAware Neo4j Framework](https://github.com/graphaware/neo4j-framework).

### Introduction

This module provides a decorator of the Neo4j Transaction Event API (called [`TransactionData`](http://neo4j.com/docs/stable/javadocs/org/neo4j/graphdb/event/TransactionData.html)).
Before a transaction commits, the improved API allows users to traverse the new version of the graph (as it will be
after the transaction commits), as well as a "snapshot" of the old graph (as it was before the transaction started).
It provides a clean API to access information about changes performed by the transaction as well as the option to
perform additional changes or prevent the transaction from committing.

The least you can gain from using this functionality is avoiding `java.lang.IllegalStateException: Node/Relationship has
been deleted in this tx` when trying to access properties of nodes/relationships deleted in a transaction. You can also
easily access relationships/nodes that were changed and/or deleted in a transaction, again completely exception-free.

The target audience of this module are advanced Neo4j users, mostly Java developers developing Neo4j
[`TransactionEventHandler`](http://neo4j.com/docs/stable/javadocs/org/neo4j/graphdb/event/TransactionEventHandler.html)s.
The module is also one of the key components of [GraphAware Runtime](../runtime).

### Getting the Module

Add the following snippet to your pom.xml:

```xml
<dependency>
    <groupId>com.graphaware.neo4j</groupId>
    <artifactId>tx-api</artifactId>
    <version>3.3.0.51</version>
</dependency>
```

### Usage

**Example:** An example is provided in [examples/friendship-strength-counter](../examples/friendship-strength-counter).

To use the API, simply instantiate one of the [`ImprovedTransactionData`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/tx/event/improved/api/ImprovedTransactionData.html) implementations.
[`LazyTransactionData`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/tx/event/improved/api/LazyTransactionData.html) is recommended as it is the easiest one to use.

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

[`FilteredTransactionData`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/tx/event/improved/api/FilteredTransactionData.html) can be used instead.
They effectively hide portions of the graph, including any changes performed
on nodes and relationships that are not interesting. [`InclusionPolicies`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/common/policy/InclusionPolicies.html) are used to convey the information about
what is interesting and what is not. For example, of only nodes with name equal to "Two" and no relationships at all
are of interest, the example above could be modified as follows:

```java
GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();
database.registerTransactionEventHandler(new TransactionEventHandler.Adapter<Object>() {
    @Override
    public Object beforeCommit(TransactionData data) throws Exception {
        InclusionPolicies inclusionPolicies = InclusionPolicies.all()
                .with(new NodeInclusionPolicy() {
                    @Override
                    public boolean include(Node node) {
                        return node.getProperty("name", "default").equals("Two");
                    }
                })
                .with(IncludeNoRelationships.getInstance());

        ImprovedTransactionData improvedTransactionData
                = new FilteredTransactionData(new LazyTransactionData(data), inclusionPolicies);

        //have fun here with improvedTransactionData!

        return null;
    }
});
```

### Example Scenario

**Example:** The following example is provided in [examples/friendship-strength-counter](../examples/friendship-strength-counter).

Let's illustrate why this might be useful on a very simple example. Let's say we have a `FRIEND_OF` relationship in the
system and it has a `strength` property indicating the strength of the friendship from 1 to 3. Let's further assume that
we are interested in the total strength of all `FRIEND_OF` relationships in the entire system.

We'll achieve this by creating a custom transaction event handler that keeps track of the total strength. While not an
ideal choice from a system throughput perspective, let's say for the sake of simplicity that we are going to store the
total strength on a special node (with label `FriendshipCounter`) as a `totalFriendshipStrength` property.

```java
/**
 * Example of a Neo4j {@link org.neo4j.graphdb.event.TransactionEventHandler} that uses GraphAware {@link ImprovedTransactionData}
 * to do its job, which is counting the total strength of all friendships in the database and writing that to a special
 * node created for that purpose.
 */
public class FriendshipStrengthCounter extends TransactionEventHandler.Adapter<Void> {

    public static final RelationshipType FRIEND_OF = RelationshipType.withName("FRIEND_OF");
    public static final String STRENGTH = "strength";
    public static final String TOTAL_FRIENDSHIP_STRENGTH = "totalFriendshipStrength";
    public static final Label COUNTER_NODE_LABEL = Label.label("FriendshipCounter");

    private final GraphDatabaseService database;

    public FriendshipStrengthCounter(GraphDatabaseService database) {
        this.database = database;
        try (Transaction tx = database.beginTx()) {
            getCounterNode(database); //do this in constructor to prevent multiple threads creating multiple nodes
            tx.success();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void beforeCommit(TransactionData data) throws Exception {
        ImprovedTransactionData improvedTransactionData = new LazyTransactionData(data);

        long delta = 0;

        //handle new friendships
        for (Relationship newFriendship : improvedTransactionData.getAllCreatedRelationships()) {
            if (newFriendship.isType(FRIEND_OF)) {
                delta += (long) newFriendship.getProperty(STRENGTH, 0L);
            }
        }

        //handle changed friendships
        for (Change<Relationship> changedFriendship : improvedTransactionData.getAllChangedRelationships()) {
            if (changedFriendship.getPrevious().isType(FRIEND_OF)) {
                delta -= (long) changedFriendship.getPrevious().getProperty(STRENGTH, 0L);
                delta += (long) changedFriendship.getCurrent().getProperty(STRENGTH, 0L);
            }
        }

        //handle deleted friendships
        for (Relationship deletedFriendship : improvedTransactionData.getAllDeletedRelationships()) {
            if (deletedFriendship.isType(FRIEND_OF)) {
                delta -= (long) deletedFriendship.getProperty(STRENGTH, 0L);
            }
        }

        if (delta != 0) {
            Node counter = getCounterNode(database);

            try (Transaction tx = database.beginTx()) {
                tx.acquireWriteLock(counter);
                counter.setProperty(TOTAL_FRIENDSHIP_STRENGTH, (long) counter.getProperty(TOTAL_FRIENDSHIP_STRENGTH, 0L) + delta);
                tx.success();
            }
        }

        return null;
    }

    /**
     * Get the counter node, where the friendship strength is stored. Create it if it does not exist.
     *
     * @param database to find the node in.
     * @return counter node.
     */
    private static Node getCounterNode(GraphDatabaseService database) {
        Node result = IterableUtils.getSingleOrNull(database.findNodes(COUNTER_NODE_LABEL));

        if (result != null) {
            return result;
        }

        return database.createNode(COUNTER_NODE_LABEL);
    }

    /**
     * Get the counter value of the total friendship strength counter.
     *
     * @param database to find the counter in.
     * @return total friendship strength.
     */
    public static long getTotalFriendshipStrength(GraphDatabaseService database) {
        long result = 0;

        try (Transaction tx = database.beginTx()) {
            result = (long) getCounterNode(database).getProperty(TOTAL_FRIENDSHIP_STRENGTH, 0L);
            tx.success();
        }

        return result;
    }
}
```

All that remains is registering this event handler on the database:

```java
GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase(); //this will in reality be a real database (i.e. EmbeddedGraphDatabase)
database.registerTransactionEventHandler(new FriendshipStrengthCounter(database));
```

### Usage in Detail

Note: have a look at [`ImprovedTransactionData` Javadoc](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/tx/event/improved/api/ImprovedTransactionData.html).

The API categorizes `PropertyContainer`s, i.e. `Node`s and `Relationship`s into:

* created in this transaction
* deleted in this transaction
* changed in this transaction, i.e those with at least one property created, deleted, or changed. Nodes with assigned and removed labels also fall into this category.
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
Such traversal will only traverse `Relationship`s that existed before the transaction started and will return properties/labels
and their values as they were before the transaction started. This is achieved using `NodeSnapshot` and `RelationshipSnapshot`
 decorators.

One can even perform additional mutating operations on the previous version (snapshot) of the graph, provided that the
mutated objects have been changed in the transaction (as opposed to deleted). Mutating deleted `PropertyContainer`s and
properties does not make any sense and will cause exceptions.

To summarize, this API gives access to two versions of the same graph. Through created `PropertyContainer`s and/or their
 current versions, one can traverse the current version of the graph as it will be after the transaction commits.
 Through deleted and/or previous versions of `PropertyContainer`s, one can traverse the previous snapshot of the graph,
 as it was before the transaction started.

License
-------

Copyright (c) 2013-2017 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
