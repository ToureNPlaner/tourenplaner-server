
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tourenplaner.graphrep;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.cursors.IntCursor;
import de.tourenplaner.algorithms.bbprioclassic.BBPrioResult;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author storandt
 */
public class EdgeExtractor {

	private static Logger log = Logger.getLogger("mapstuffzoom");

	final GraphRep graph;
	int[] marker;
	boolean[] visited;
	IntArrayList needClear;


	public EdgeExtractor(GraphRep graph) {
		this.graph = graph;
		marker = new int[graph.getNodeCount()];
		for (int i = 0; i < marker.length; i++) {
			marker[i] = -1;
		}
		visited = new boolean[graph.getEdgeCount()];
		for (int i = 0; i < visited.length; i++){
			visited[i] = false;
		}
		needClear = new IntArrayList();
	}


	public void unpack(int index, IntArrayList IDs, double minLen, double maxLen, double maxRatio) {
		if (visited[index])
			return;
		double edgeLen = ((double)graph.getEuclidianDist(index));

		int skipA = graph.getFirstShortcuttedEdge(index);

		if (skipA == -1 || edgeLen <= minLen) {
			visited[index] = true;
			needClear.add(index);
			IDs.add(index);
			return;
		}

		int skipB = graph.getSecondShortcuttedEdge(index);
		/*
		*              |x1 y1 1|
		* A = abs(1/2* |x2 y2 1|)= 1/2*Baseline*Height
		*              |x3 y3 1|
		*   = 1/2*((x1*y2+y1*x3+x2*y3) - (y2*x3 + y1*x2 + x1*y3))
		* Height = 2*A/Baseline
		* ratio = Height/Baseline
		* */
 		if (edgeLen <= maxLen) {
		    int middle = graph.getTarget(skipA);

			int srcId = graph.getSource(index);
		    double x1 = graph.getXPos(srcId);
		    double y1 = graph.getYPos(srcId);
		    double x2 = graph.getXPos(middle);
		    double y2 = graph.getYPos(middle);
			int trgtId = graph.getTarget(index);
		    double x3 = graph.getXPos(trgtId);
		    double y3 = graph.getYPos(trgtId);
		    double A = Math.abs(0.5 * ((x1 * y2 + y1 * x3 + x2 * y3) - (y2 * x3 + y1 * x2 + x1 * y3)));
		    double ratio = 2.0*A/(edgeLen*edgeLen);
		    if (ratio <= maxRatio) {
			    visited[index] = true;
			    needClear.add(index);
			    IDs.add(index);
			    return;
		    }
		}

		unpack(skipA, IDs, minLen, maxLen, maxRatio);
		unpack(skipB, IDs, minLen, maxLen, maxRatio);
	}

	public void unpack(int index, IntArrayList IDs, int P) {
		if (visited[index])
			return;
		int skipA = graph.getFirstShortcuttedEdge(index);
		int skipB = graph.getSecondShortcuttedEdge(index);

		if (skipA == -1) {
			visited[index] = true;
			needClear.add(index);
			IDs.add(index);
			return;
		}

		int middle = graph.getTarget(skipA);

		/*if (graph.getRank(middle) < P) {
			visited[index] = true;
			needClear.add(index);
			IDs.add(index);
			return;
		}*/
		unpack(skipA, IDs, P);
		unpack(skipB, IDs, P);
	}

	public void unpackShortcut(int index, IntArrayList newIDs, double minLen, double maxLen, double maxRatio, int P) {
		newIDs.clear();
		if (minLen < 0)
			unpack(index, newIDs, P);
		else
			unpack(index, newIDs, minLen, maxLen, maxRatio);
	}


	private BBPrioResult.Edge extractEdge(int edge_index, IntArrayList nodeIDs, IntArrayList extraIDs, double minLen, double maxLen, double maxRatio, int P) {
		BBPrioResult.Edge e = new BBPrioResult.Edge();
		e.edgeId = edge_index;
		IntArrayList unpackIDs = new IntArrayList();
		unpackShortcut(edge_index, unpackIDs, minLen, maxLen, maxRatio, P);

		for (int k = 0; k < unpackIDs.size(); k++) {
			int newEdge = unpackIDs.get(k);
			int s = graph.getSource(newEdge);
			int t = graph.getTarget(newEdge);
			int mark1 = marker[s];
			if (mark1 < 0) {
				marker[s] = nodeIDs.size() + extraIDs.size();
				extraIDs.add(s);
			}
			int mark2 = marker[t];
			if (mark2 < 0) {
				marker[t] = nodeIDs.size() + extraIDs.size();
				extraIDs.add(t);
			}
			int type = 0; // TOD we need better type info here;
			e.unpacked.add(graph.getXPos(s));
			e.unpacked.add(graph.getYPos(s));
			e.unpacked.add(graph.getXPos(t));
			e.unpacked.add(graph.getYPos(t));
			e.unpacked.add(type);
		}
		return e;
	}


	public void getPriorityEdges(IntArrayList nodeIDs, ArrayList<BBPrioResult.Edge> edges, double minLen, double maxLen, double maxRatio, int P) {
		for (int i = 0; i < nodeIDs.size(); i++) {
			marker[nodeIDs.get(i)] = i;
		}
		IntArrayList extraIDs = new IntArrayList();
		for (int i = 0; i < nodeIDs.size(); i++) {
			int curr_node = nodeIDs.get(i);
			int curr_prio = graph.getRank(curr_node);

			// Out Edges
			for (int j = graph.getOutEdgeCount(curr_node) - 1; j >= 0; --j) {
				int edgeId = graph.getOutEdgeId(curr_node, j);
				int trg = graph.getTarget(edgeId);
				int trgtPrio = graph.getRank(trg);
				if (trgtPrio < curr_prio || trgtPrio < P) { // Out edges are sorted by target prio ascending, we're descending so break instead of continue
					break;
				}
				int skipA = graph.getFirstShortcuttedEdge(edgeId);
				int skippedNode = -1;
				if (skipA >= 0) {
					skippedNode = graph.getTarget(skipA);

					if (graph.getRank(skippedNode) >= P) {
						continue;
					}

					edges.add(extractEdge(edgeId, nodeIDs, extraIDs, minLen, maxLen, maxRatio, P));

				} else if (!visited[edgeId]){
					edges.add(takeEdge(edgeId));
				}
			}

			// In Edges
			for (int j = 0 ; j < graph.getInEdgeCount(curr_node); ++j) {
				int edgeId = graph.getOutEdgeId(curr_node, j);
				int src = graph.getSource(edgeId);
				int srcPrio = graph.getRank(src);
				if (srcPrio < curr_prio || srcPrio < P) { // In edges are sorted by source prio descending, we're ascending so break instead of continue
					break;
				}
				int skipB = graph.getSecondShortcuttedEdge(edgeId);
				int skippedNode = -1;
				if (skipB >= 0) {
					skippedNode = graph.getSource(skipB);

					if (graph.getRank(skippedNode) >= P) {
						continue;
					}

					edges.add(extractEdge(edgeId, nodeIDs, extraIDs, minLen, maxLen, maxRatio, P));

				} else if (!visited[edgeId]){
					edges.add(takeEdge(edgeId));
				}
			}

		}
		log.info("Edges: "+edges.size());
		//unpackShortcut(edge_index, unpackIDs, types, Integer.MAX_VALUE);
		nodeIDs.addAll(extraIDs);

		for (int i = 0; i < nodeIDs.size(); i++) {
			marker[nodeIDs.get(i)] = -1;
		}

		for(IntCursor index : needClear){
			visited[index.value] = false;
		}
		needClear.clear();
	}

	private BBPrioResult.Edge takeEdge(int edge_index) {
		visited[edge_index] = true;
		needClear.add(edge_index);
		int s = graph.getSource(edge_index);
		int t = graph.getTarget(edge_index);
		int type = 0; // We need better type info
		BBPrioResult.Edge e = new BBPrioResult.Edge();
		e.edgeId = edge_index;
		e.unpacked.add(graph.getXPos(s));
		e.unpacked.add(graph.getYPos(s));
		e.unpacked.add(graph.getXPos(t));
		e.unpacked.add(graph.getYPos(t));
		e.unpacked.add(type);
		return e;
	}


}
