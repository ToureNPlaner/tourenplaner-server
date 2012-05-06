package de.tourenplaner.utils;

import de.tourenplaner.graphrep.GraphRep;

/**
 * A GraphRep uses for ClientSideProcessing
 */
public class ClientSideGraphRep extends GraphRep{
    private final int[] origEdgeIds;
    private final int[] origNodeIds;

    public ClientSideGraphRep(int numNodes, int numEdges){
        super(numNodes, numEdges);
        origEdgeIds = new int[numEdges];
        origNodeIds = new int[numNodes];
    }

    final void setOrigEdgeId(int edgeId, int origId) {
        origEdgeIds[edgeId] = origId;
    }

    final int getOrigEdgeId(int edgeId) {
        return origEdgeIds[edgeId];
    }

    final void setOrigNodeId(int nodeId, int origId) {
        origNodeIds[nodeId] = origId;
    }

    final int getOrigNodeId(int nodeId) {
        return origNodeIds[nodeId];
    }

}
