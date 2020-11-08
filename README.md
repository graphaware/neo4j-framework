<a name="top"/>

GraphAware Neo4j Framework
==========================

[![Build Status](https://travis-ci.org/graphaware/neo4j-framework.png)](https://travis-ci.org/graphaware/neo4j-framework) | <a href="http://graphaware.com/downloads/" target="_blank">Downloads</a> |

GraphAware Framework provides a platform for building custom transaction-driven behaviour into <a href="http://neo4j.org" target="_blank">Neo4j</a>.

## Community vs Enterprise

This open-source (GPLv3) version of the GraphAware Framework is compatible with Neo4j Community Edition only. 
It *will not work* with Neo4j Enterprise Edition, which is a proprietary and commercial software product of Neo4j, Inc.

GraphAware offers a *paid* Enterprise version of the GraphAware Framework to licensed users of Neo4j Enterprise Edition.
Please [get in touch](mailto:info@graphaware.com) to receive access.

## Versioning

The Framework version number has two parts. The first three numbers indicate compatibility with a Neo4j version. The last number 
is the version of the Framework. For example, version 4.0.8.58 is version 58 of the Framework compatible with Neo4j 4.0.8.
**Please note that we will not address issues related to the usage of incompatible versions of the Framework and Neo4j.**

## Functionality

On a high level, the Framework is a Neo4j kernel extension that enables the use of GraphAware as well as custom Modules. 
These Modules typically extend the core functionality of the database by transparently enriching/modifying/preventing ongoing transactions in real-time.

Examples of popular Framework Modules are:
* GraphAware UUID
* GraphAware TimeTree
* GraphAware Neo2Elastic
* GraphAware Schema (Enterprise Only)
* GraphAware Audit (Enterprise Only)
 
You can also make use the Framework as a software library, taking advantage of its useful features, such as GraphUnit for testing code that talks to Neo4j.

## Using the Framework

Deploying the GraphAware Framework (as well as any modules) is a matter of :
* [downloading](#download) the appropriate .jar files
* copying them into the _plugins_ directory in your Neo4j installation
* restarting the server

The framework and modules are then used via Cypher calls to their procedures, if they provide any.

### Configuration

By default, the Framework reads its configuration from `graphaware.conf` located in the `conf` directory of Neo4j. You
can change the file name by setting `com.graphaware.config.file` to your desired value in `neo4j.conf`. 

#### Community Configuration

The Framework itself has no more configuration options but its Modules typically do.

#### Enterprise Configuration

You can selectively enable the GraphAware Framework for different databases running within the same Neo4j instance, by
setting `com.graphaware.runtime.enabled` in `graphaware.conf` to either:
- `*` for any database (except for the 'system' database), or
- name of a single database, or
- a comma-separated list of database names

For example, the configuration of GraphAware Enterprise on Neo4j Enterprise with the UUID module running for all databases
could look like this: 
```
com.graphaware.runtime.enabled=*
com.graphaware.module.*.UIDM.1=com.graphaware.module.uuid.UuidBootstrapper
``` 

For two specific databases ('db1' and 'db2'), it would look like this:
```
com.graphaware.runtime.enabled=db1,db2
com.graphaware.module.*.UIDM.1=com.graphaware.module.uuid.UuidBootstrapper
```

If you wanted a different configuration of a specific module for each database, you could do
```
com.graphaware.runtime.enabled=db1,db2
com.graphaware.module.db1.UIDM1.1=com.graphaware.module.uuid.UuidBootstrapper
com.graphaware.module.db2.UIDM2.1=com.graphaware.module.uuid.UuidBootstrapper
# ... more config for the UUID modules ...
```
Note that in this case, the UUID module must have a unique ID (UIDM1 vs UIDM2).

<a name="download"/>

## Getting GraphAware Framework

### Releases

To use the latest release, <a href="http://products.graphaware.com/" target="_blank">download</a> the appropriate version and put it
the _plugins_ directory in your Neo4j server installation and restart the server (server mode), or on the classpath (embedded mode).

Releases are synced to <a href="http://search.maven.org/#search%7Cga%7C1%7Ccom.graphaware.neo4j" target="_blank">Maven Central
repository</a>. When using Maven for dependency management, include one or more dependencies in your pom.xml. To find out
 which ones, read further by clicking on one of the sub-modules of this project.

### Snapshots

To use the latest development version, just clone this repository and run `mvn clean install`. This will produce {newVersion}.XX-SNAPSHOT jar files (eg. 4.2.6.66-SNAPSHOT). If you need standalone .jar files with all dependencies, look into the `target` folder in the `build` directory.

## Building Own Modules

Java developers that want to build own Modules should use the provided parent Maven module:
```
 <parent>
    <groupId>com.graphaware.neo4j</groupId>
    <artifactId>module-parent</artifactId>
    <version>{framework version}</version>
 </parent>
```

The easiest way to start is to look at the Friendship Strength Counter Module example, or the GraphAware TimeTree Module.

## License

Copyright (c) 2013-2020 GraphAware

GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
