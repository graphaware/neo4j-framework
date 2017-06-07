Utilities
=========

This (Maven) module is part of the [GraphAware Neo4j Framework](https://github.com/graphaware/neo4j-framework).

Whether or not you use the code in this repository as a framework or runtime, you can always use this module as a library
of useful tested code that we found ourselves writing over and over when developing with Neo4j.

The target audience of this module are Neo4j users, mostly Java developers.

### Getting the Module

Add the following snippet to your pom.xml:

```xml
<dependency>
    <groupId>com.graphaware.neo4j</groupId>
    <artifactId>common</artifactId>
    <version>3.2.1.50</version>
</dependency>
```

### Usage

Besides some frameworky internal code, the following functionality is provided by this module:

* Arrays (see [`ArrayUtils`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/common/util/ArrayUtils.html))
    * Determine if an object is a primitive array
    * Convert an array to a String representation
    * Check equality of two `Object`s which may or may not be arrays
    * Check equality of two `Map<String, Object>` instances, where the `Object`-typed values may or may not be arrays

* Relationship Directions (see [`DirectionUtils`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/common/util/DirectionUtils.html))
    * The need to determine the direction of a relationship is quite common. The `Relationship` object does not provide the
      functionality for the obvious reason that it depends on "who's point of view we're talking about". In order to resolve
      a direction from a specific Node's point of view, use `DirectionUtils.resolveDirection(Relationship relationship, Node pointOfView);`

* Iterables (see [`IterableUtils`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/common/util/IterableUtils.html))
    * Count iterables by iterating over them, unless they're a collection in which case just return `size()`
    * Randomize iterables by iterating over them and shuffling them, unless they're a collection in which case just shuffle
    * Convert iterables to lists
    * Check if iterable contains an object by iterating over the iterable, unless it's a collection in which case just return `contains(..)`
    * Get a single item from an `Iterable`, throw an exception if there isn't exactly one
    * Get a single item from an `Iterable` or null, throw an exception if there is more than one

* Property Containers (see [`PropertyContainerUtils`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/common/util/PropertyContainerUtils.html))
    * Convert a `PropertyContainer` to a Map of properties
    * Delete nodes with all their relationships automatically, avoiding a `org.neo4j.kernel.impl.nioneo.store.ConstraintViolationException: Node record Node[xxx] still has relationships`, using `DeleteUtils.deleteNodeAndRelationships(node);`
    * Get IDs of property containers
    * Convert Nodes and Relationships to a human-readable String representation
    * Get a number-valued property without knowing whether it is stored as byte, int, or long

* In-graph key-value store (see [`KeyValueStore`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/common/kv/KeyValueStore.html))

... and more, please see [Javadoc](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/common/util/package-summary.html).

### Inclusion Policies

Throughout the framework, a class hierarchy stemming from `com.graphaware.common.policy.inclusion.InclusionPolicy` is used. Its
subtypes, such as `NodeInclusionPolicy`, `RelationshipInclusionPolicy`, `NodePropertyInclusionPolicy`, etc. are used
in various places to determine, whether to use certain nodes, relationships, or properties for something.

It is of course possible to create custom implementations of these interfaces in Java. For example, a `NodeInclusionPolicy`
that tells the framework to only use nodes with label _Person_ for something would look like this:

```java
public class IncludeAllPeople implements NodeInclusionPolicy {

    @Override
    public boolean include(Node node) {
        return node.hasLabel(Label.label("Person"));
    }
}
```

Many GraphAware Framework Modules, however, can be used without writing any code, especially when using Neo4j in server mode.
When these modules need to be configured (in _neo4j.conf_), one can use expressions to define `InclusionPolicies`.
For those familiar with the Spring Framework, the expressions are SPeL expressions and are parsed using GraphAware's
`InclusionPolicy` implementation, for example `SpelNodeInclusionPolicy`, `SpelRelationshipInclusionPolicy`, etc.

In practice, the configuration could look as follows:

```
# Example of a Recommendation Engine configuration expressing which nodes recommendations should be computed for:
com.graphaware.module.reco.node=hasLabel('Person')
```

The following expressions can be used:
* For all Property Containers (Nodes and Relationships):
    * `true` - include all
    * `false` - include none
    * `hasProperty('propertyName')` - returns boolean. Example: `hasProperty('name')`
    * `getProperty('propertyName','defaultValue')` - returns Object. Example: `getProperty('name','unknown') == 'Michal'`
* For Nodes only:
    * `getDegree()` or `degree` - returns int. Examples: `degree > 1`
    * `getDegree('typeOrDirection')` - returns int. Examples: `getDegree('OUTGOING') == 0` or `getDegree('FRIEND_OF') > 1000`
    * `getDegree('type', 'direction')` - returns int. Examples: `getDegree('FRIEND_OF','OUTGOING') > 0`
    * `hasLabel('label')` - returns boolean. Example: `hasLabel('Person')`
* For Relationships only:
    * `startNode` - returns Node. Example: `startNode.hasProperty('name')`
    * `endNode` - returns Node. Example: `endNode.getDegree() > 0`
    * `type` - returns String. Example: `type == 'WORKS_FOR'`
    * `isType('type')` - returns boolean. Example: `isType('WORKS_FOR')`
* For Relationships only when one of the participating nodes "looking" at the relationship is provided:
    * `isOutgoing()` - returns boolean. Example: `isOutgoing()`
    * `isIncoming()` - returns boolean. Example: `isIncoming()`
    * `otherNode` - returns Node. Example: `otherNode.hasProperty('name')`
* For all Property Container Properties:
    * `key` - returns String. Example: `key != 'name'`

Of course, the expressions can be combined with logical operators, for instance:
* `isType('LIVES_IN') && isIncoming()`
* `hasLabel('Employee') || hasProperty('form') || getProperty('age', 0) > 20`
* ...


License
-------

Copyright (c) 2013-2017 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
