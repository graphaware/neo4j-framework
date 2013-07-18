GraphAware Neo4j Framework
==========================

[![Build Status](https://travis-ci.org/graphaware/neo4j-framework.png)](https://travis-ci.org/graphaware/neo4j-framework)

The aim of the GraphAware Neo4j Framework is to speed-up development with Neo4j and provide useful generic and domain-specific
modules, analytical capabilities, graph algorithm libraries, etc.

The purpose of this specific project is to allow for convenient usage of GraphAware Modules as well as custom ones.
These modules are typically units that perform some (behind-the-scenes) mutations of the graph as transactions occur.

Download
--------

To use the latest development version, just clone this repository, run `mvn clean install` and put the produced .jar file (found in target) into your classpath.

Releases are synced to Maven Central repository. In order to use the latest release, include the following snippet in your pom.xml:

    <dependencies>
        ...
        <dependency>
            <groupId>com.graphaware</groupId>
            <artifactId>neo4j-framework</artifactId>
            <version>1.9-1.0</version>
        </dependency>
        ...
     </dependencies>

### Note on Versioning Scheme

The version number has two parts, separated by a dash. The first part indicates compatibility with a Neo4j version.
 The second part is the version of the framework. For example, version 1.9-1.2 is a 1.2 version of the framework
 compatible with Neo4j 1.9.x

Usage
-----

Using the framework is very easy. Instantiate it, register desired modules, and start it. For example:

```java
    GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase(); //replace with your own database, probably a permanent one

    GraphAwareFramework framework = new GraphAwareFramework(database);

    framework.registerModule(new SomeModule());
    framework.registerModule(new SomeOtherModule());

    framework.start();
```


License
-------

Copyright (c) 2013 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.