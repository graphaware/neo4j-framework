GraphAware Server
=================

This (Maven) module is part of the [GraphAware Neo4j Framework](https://github.com/graphaware/neo4j-framework).

### Introduction

This module allows building APIs by deploying Spring MVC Controllers into the _plugins_ directory of the Neo4j server and thus provide
and alternative to server plugins and unmanaged server extensions.

There is no need to use this module directly. Just [download](http://products.graphaware.com) the appropriate release
of the GraphAware Framework and place it into the _plugins_ directory of Neo4j.

The following APIs are developed and provided by GraphAware:
* [Algorithms](https://github.com/graphaware/neo4j-algorithms)
* [NodeRank](https://github.com/graphaware/neo4j-noderank) (primarily a [runtime module](../runtime))
* [RestTest](https://github.com/graphaware/neo4j-resttest)
* [TimeTree](https://github.com/graphaware/neo4j-timetree) (optionally also a [runtime module](../runtime))


### Usage

**Example:** An example is provided in [examples/node-counter](../examples/node-counter).

With GraphAware Framework in the _plugins_ directory of your Neo4j server installation and
 `dbms.unmanaged_extension_classes=com.graphaware.server=/graphaware` in `neo4j.conf`, it is possible to develop Spring
MVC controllers that have the Neo4j database wired in as `GraphDatabaseService`.

For example, to develop an API endpoint that counts all the nodes in the database using Spring MVC, create the following
controller:

```java
/**
 *  Sample REST API for counting all nodes in the database.
 */
@Controller
@RequestMapping("count")
public class NodeCountApi {

    private final GraphDatabaseService database;

    @Autowired
    public NodeCountApi(GraphDatabaseService database) {
        this.database = database;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public long count() {
        try (Transaction tx = database.beginTx()) {
            return Iterables.count(database.getAllNodes());
        }
    }
}
```

**WARNING** By default Your class must reside in a `com`, `net`, or `org` top-level
package and one of the package levels must be called `graphaware`. For example, `com.mycompany.graphaware.NodeCountApi`
will do.

Alternatively, if you do not want the class to reside in the specified package, you need to put the following
class in a package that follows the specification, for instance `com.mycompany.graphaware`:

```java
@Configuration
@ComponentScan(basePackages = {"com.your_domain_here.**"})
public class GraphAwareIntegration {
}
```

Then your controllers can reside in any subpackage of `com.yourdomain`.

Yet another alternative is to add the following line to `neo4j.conf`:
```
com.graphaware.server.api.scan=com.your_domain_here.**
```

The value part of the property is a Spring expression for which packages to scan, typically provided to the `base-package` XML attribute of `<context:component-scan />`. Please refer to the [Spring Documentation](http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#beans-java-instantiating-container-scan) for more details.

**WARNING END**

Compile this code into a .jar file (with dependencies, see below) and place it into the _plugins_ directory of your
Neo4j server installation. You will then be able to issue a `GET` request to `http://your-neo4j-url:7474/graphaware/count`
and receive the number of nodes in the database in the response body. Note that the `graphaware` part of the URL comes from `neo4j.conf`
and can be change, e.g.:
```
`dbms.unmanaged_extension_classes=com.graphaware.server=/your_uri_here`
```

To get started quickly, start with the [pom file from the example above](https://github.com/graphaware/neo4j-framework/blob/master/examples/node-counter/pom.xml) and modify to your needs.

### Long-Running Transactions

It is possible for extensions to participate in long-running transactions that are created using the [Transactional Cypher HTTP Endpoint](http://neo4j.com/docs/stable/rest-api-transactional.html).

In order for an extension (MVC controller etc) to participate in long-running transactions, the client needs to set an HTTP request header
called `_GA_TX_ID` with value equal to the transaction ID, which you have received from the endpoint when creating the transaction.

License
-------

Copyright (c) 2013-2017 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
