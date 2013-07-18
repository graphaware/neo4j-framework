GraphAware Neo4j Framework
==========================

[![Build Status](https://travis-ci.org/graphaware/neo4j-framework.png)](https://travis-ci.org/graphaware/neo4j-framework)


Features
--------


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

Usage
-----



License
-------

Copyright (c) 2013 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.