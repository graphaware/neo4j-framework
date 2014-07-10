GraphAware Server
=================

This (Maven) module is part of the [GraphAware Neo4j Framework](https://github.com/graphaware/neo4j-framework).

### Introduction

This module allows building APIs by deploying Spring MVC Controllers into the _plugins_ directory of the Neo4j server and thus provide
and alternative to server plugins and unmanaged server extensions.

There is no need to use this module directly. Just [download](http://graphaware.com/downloads) the appropriate release
of the GraphAware Framework and place it into the _plugins_ directory of Neo4j.

The following APIs are developed and provided by GraphAware:
* [TimeTree](https://github.com/graphaware/neo4j-timetree)

### Usage

**Example:** An example is provided in [examples/node-counter](../examples/node-counter).

With GraphAware Framework in the _plugins_ directory of your Neo4j server installation, it is possible to develop Spring
MVC controllers that have the Neo4j database wired in as `GraphDatabaseService`.

For example, to develop an API endpoint that counts all the nodes in the database using Spring MVC, create the following
controller:

```java
/**
 *  Sample REST API for counting all nodes in the database.
 */
@Controller
@RequestMapping("count")
@Transactional
public class NodeCountApi {

    private final GraphDatabaseService database;

    @Autowired
    public NodeCountApi(GraphDatabaseService database) {
        this.database = database;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public long count() {
        return Iterables.count(GlobalGraphOperations.at(database).getAllNodes());
    }
}
```

**WARNING** Your class must reside in a `com`, `net`, or `org` top-level
package and one of the package levels must be called `graphaware`. For example, `com.mycompany.graphaware.NodeCountApi`
 will do. Alternatively, if you do not want the class to reside in the specified package, you need to put the following
 class in a package that follows the specification:

```java
@Configuration
@ComponentScan(basePackages = {"com.yourdomain.**"})
public class GraphAwareIntegration {
}
```

Then your controllers can reside in any subpackage of `com.yourdomain`.

**WARNING END**

Compile this code into a .jar file (with dependencies, see below) and place it into the _plugins_ directory of your
Neo4j server installation. You will then be able to issue a `GET` request to `http://your-neo4j-url:7474/graphaware/count`
and receive the number of nodes in the database in the response body. Note that the `graphaware` part of the URL must be
there and cannot (yet) be configured.

To get started quickly, use the provided Maven archetype by typing:

    mvn archetype:generate -DarchetypeGroupId=com.graphaware.neo4j -DarchetypeArtifactId=graphaware-springmvc-maven-archetype -DarchetypeVersion=2.1.2.9

To get started manually, you will need the following dependencies:

```xml
<dependencies>

    <!-- GraphAware Framework -->
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>common</artifactId>
        <version>2.1.2.9</version>
        <scope>provided</scope>
    </dependency>

    <!-- optional -->
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>api</artifactId>
        <version>2.1.2.9</version>
        <scope>provided</scope>
    </dependency>

    <!-- Spring Framework -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webmvc</artifactId>
        <version>4.0.0.RELEASE</version>
        <scope>provided</scope>
    </dependency>

    <!-- optional if you want to use @Transactional -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-tx</artifactId>
        <version>${spring.version}</version>
        <scope>provided</scope>
    </dependency>

    <!-- Neo4j -->
    <dependency>
        <groupId>org.neo4j</groupId>
        <artifactId>neo4j</artifactId>
        <version>2.1.2</version>
        <scope>provided</scope>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>server-community</artifactId>
        <version>2.1.2.9</version>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <version>2.1.2.9</version>
        <artifactId>tests</artifactId>
        <scope>test</scope>
    </dependency>

</dependencies>
```

It is also a good idea to use make sure the resulting .jar file includes all the dependencies, if you use any external
ones that aren't listed above:
<a name="alldependencies"/>
```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-assembly-plugin</artifactId>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>attached</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <finalName>${project.name}-all-${project.version}</finalName>
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
                <appendAssemblyId>false</appendAssemblyId>
            </configuration>
        </plugin>
    </plugins>
</build>
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
