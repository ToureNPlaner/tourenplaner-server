package de.tourenplaner.algorithms;

import com.carrotsearch.hppc.IntArrayList;
import de.tourenplaner.computecore.RequestData;

/**
 * @author Niklas Schnelle
 * RequestData that stores a list of nodeIds, used by WayByNodeIds
 */
public class NodeIdsRequestData extends RequestData{
    private final IntArrayList nodeIds;

    public NodeIdsRequestData(String algSuffix, IntArrayList nodeIds) {
        super(algSuffix);
        this.nodeIds = nodeIds;
    }

    public IntArrayList getNodeIds(){
        return nodeIds;
    }

}
