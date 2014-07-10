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
    <version>2.1.2.9</version>
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

... and more, please see [Javadoc](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/common/util/package-summary.html).

License
-------

Copyright (c) 2014 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
