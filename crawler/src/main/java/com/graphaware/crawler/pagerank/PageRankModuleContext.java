package com.graphaware.crawler.pagerank;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import com.graphaware.runtime.state.GraphPosition;
import com.graphaware.runtime.state.ModuleContext;

public class PageRankModuleContext implements ModuleContext<GraphPosition<Node>> {

	private Node node;

	public PageRankModuleContext(Node currentNode) {
		// FIXME: we really don't want to do this - the module should look up its reference each time
		// maybe we should impose limits on what should be stored?
		// maybe we should write a context that encourages authors to save things like nodes, r'ships "properly"
		this.node = currentNode;
	}

	@Override
	public GraphPosition<Node> getPosition() {
		return new GraphPosition<Node>() {
			@Override
			public Node find(GraphDatabaseService database) {
				return node; // XXX this seems very odd
			}
		};
	}

}
