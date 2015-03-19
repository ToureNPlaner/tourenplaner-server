package de.tourenplaner.algorithms.bbbundle;

import com.carrotsearch.hppc.IntArrayList;
import de.tourenplaner.algorithms.ComputeException;
import de.tourenplaner.algorithms.PrioAlgorithm;
import de.tourenplaner.algorithms.bbprioclassic.BBPrioResult;
import de.tourenplaner.algorithms.bbprioclassic.BoundingBox;
import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.graphrep.GraphRep;
import de.tourenplaner.graphrep.PrioDings;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by niklas on 19.03.15.
 */
public class BBBundle  extends PrioAlgorithm {
    private static Logger log = Logger.getLogger("de.tourenplaner.algorithms");

    public BBBundle(GraphRep graph, PrioDings prioDings) {
        super(graph, prioDings);
    }



    @Override
    public void compute(ComputeRequest request) throws ComputeException, Exception {
        BBBundleRequestData req = (BBBundleRequestData) request.getRequestData();
        BoundingBox bbox = req.getBbox();


        long start = System.nanoTime();
        int currNodeCount;
        int level;
        IntArrayList bboxNodes;
        if (req.mode == BBBundleRequestData.LevelMode.AUTO) {

            level = graph.getMaxRank();
            do {
                level = level - 10;
                bboxNodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
                currNodeCount = bboxNodes.size();
            } while (level > 0 && currNodeCount < req.nodeCount);

        } else if (req.mode == BBBundleRequestData.LevelMode.HINTED){

            level = graph.getMaxRank();
            do {
                level = level - 10;
                bboxNodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
                currNodeCount = bboxNodes.size();
            } while (level > 0 && currNodeCount < req.nodeCount);
            log.info("AutoLevel was: "+level);
            level = (req.hintLevel+level)/2;
            bboxNodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
            currNodeCount = bboxNodes.size();

        } else { // else if (req.mode == BBPrioLimitedRequestData.LevelMode.EXACT){
            level = req.getHintLevel();
            bboxNodes = prioDings.getNodeSelection(new Rectangle2D.Double(bbox.x, bbox.y, bbox.width, bbox.height), level);
            currNodeCount = bboxNodes.size();
        }
        log.info("Level was: "+level);
        log.info("Nodes found: "+currNodeCount);



        ArrayList<BBPrioResult.Edge> edges = new ArrayList<>();

        log.info("Took " + (double)(System.nanoTime() - start) / 1000000.0+" ms");
        request.setResultObject(new BBPrioResult(graph, bboxNodes, edges));
    }
}
