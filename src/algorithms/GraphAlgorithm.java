package algorithms;

import graphrep.Graphrep;

public abstract class GraphAlgorithm implements Algorithm {
	private GraphAlgorithm() {
	}

	Graphrep graph;

	public GraphAlgorithm(Graphrep graph) {
		this.graph = graph;
	}

}
