/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.server.plugin.algo.path;

import com.graphaware.algo.path.*;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.StandardExpander;
import org.neo4j.kernel.Traversal;
import org.neo4j.server.plugins.*;

import java.util.Collections;
import java.util.List;

/**
 * A managed plugin that finds finds a given number of shortest paths between two nodes. It is different from
 * standard shortest path finding, because it allows to specify the desired number of results.
 * Provided that there are enough paths between the two nodes in the graph, this path finder will first return all the
 * shortest paths, then all the paths one hop longer, then two hops longer, etc., until enough paths have been returned.
 * <p/>
 * Please note that nodes that are on a path with certain length will not be considered for paths with greater lengths.
 * For example, given the following graph:
 * <p/>
 * (1)->(2)->(3)
 * (1)->(4)->(5)->(3)
 * (4)->(2)
 * <p/>
 * the shortest path from (1) to (3) is (1)->(2)->(3) and has a length of 2. If more paths are needed, the next path
 * returned will be (1)->(4)->(5)->(3) with a length of 3. Note that there is another path of length 3:
 * (1)->(4)->(2)->(3), but it is not returned, since (2)->(3) is contained in a shorter path.
 */
public class NumberOfShortestPaths extends ServerPlugin {

    @Description("Find a number shortest path between two nodes, with increasing path length and optionally path cost.")
    @PluginTarget(Node.class)
    public Iterable<Path> paths(
            @Source Node source,
            @Description("The node to find the shortest paths to.")
            @Parameter(name = "target") Node target,
            @Description("The relationship types to follow when searching for the shortest paths. If omitted all types are followed.")
            @Parameter(name = "types", optional = true) String[] types,
            @Description("The maximum path length to search for, default value (if omitted) is 3.")
            @Parameter(name = "depth", optional = true) Integer depth,
            @Description("The desired number of results, default value (if omitted) is 10. The actual number of results can be smaller (if no other paths exist).")
            @Parameter(name = "noResults", optional = true) Integer noResults,
            @Description("The name of relationship property indicating its weight/cost. If omitted, result isn't sorted by total path weights. If the property is missing for a relationship, Integer.MAX_VALUE is used.")
            @Parameter(name = "weight", optional = true) String weightProperty) {

        PathExpander expander;
        if (types == null) {
            expander = Traversal.pathExpanderForAllTypes();
        } else {
            Expander relationshipExpander = Traversal.emptyExpander();
            for (String type : types) {
                relationshipExpander = relationshipExpander.add(DynamicRelationshipType.withName(type));
            }
            expander = StandardExpander.toPathExpander(relationshipExpander);
        }

        noResults = noResults == null ? 10 : noResults;

        List<Path> paths = new NumberOfShortestPathsFinder(depth == null ? 3 : depth, noResults, expander).findPaths(source, target);

        if (weightProperty != null) {
            Collections.sort(paths, new LengthThenCostPathComparator(weightProperty));
        }

        return paths.subList(0, Math.min(noResults, paths.size()));
    }
}
