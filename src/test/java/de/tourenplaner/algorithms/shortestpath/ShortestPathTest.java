/*
 * Copyright 2012 ToureNPlaner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.tourenplaner.algorithms.shortestpath;

import com.carrotsearch.hppc.IntArrayList;
import de.tourenplaner.algorithms.ComputeException;
import de.tourenplaner.algorithms.GraphAlgorithmFactory;
import de.tourenplaner.computecore.RequestPoint;
import de.tourenplaner.computecore.RequestPoints;
import de.tourenplaner.computecore.Way;
import de.tourenplaner.graphrep.GraphRep;
import de.tourenplaner.graphrep.TestGraphReader;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static junit.framework.Assert.assertEquals;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class ShortestPathTest {

    private Random random = new Random(1337);
    private int nodeCount = 0;
    private GraphRep graph = null;

    private int testCaseNumber = 0;

    private void runTestCase(ShortestPath shortestPath, int numPoints, boolean tour) throws ComputeException, IllegalAccessException {
        RequestPoints points = new RequestPoints();
        for (int i = 0; i < numPoints; i++) {
            int randomId = random.nextInt(nodeCount);
            points.addPoint(graph.getLat(randomId), graph.getLon(randomId));
        }

        // TODO if we do not add here constraints hash map, the test fails. Maybe bad behavior of ShortestPath
        for (RequestPoint point : points.getStore()) {
            point.setConstraints(new HashMap<String, Object>());
        }

        testCaseNumber++;
        List<Way> wayList = new ArrayList<Way>();
        points.setIdsFromGraph(graph);

        int totalDist = shortestPath.shortestPath(points, wayList, tour);


        int expectedTotalEuclidianDist = 0;
        int expectedTotalDist = 0;
        // pointNumber is the number of the start points within points 
        for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {

            int[] dist = new int[nodeCount];
            int[] preds = new int[nodeCount];
            for (int nodeId = 0; nodeId < nodeCount; nodeId++) {
                dist[nodeId] = Integer.MAX_VALUE;
            }
            int sourcePointId = points.getPointId(pointIndex);
            int targetPointId;
            if (pointIndex < points.size() - 1) {
                targetPointId = points.getPointId(pointIndex + 1);
            } else if (tour) {
                targetPointId = points.getPointId(0);
            } else {
                break;
            }
            dist[sourcePointId] = 0;
            preds[sourcePointId] = 0;
            IntArrayList queue = new IntArrayList();
            queue.add(sourcePointId);
            while(!queue.isEmpty()){
                int sourceId = queue.remove(0);
                for (int edgeNum = 0; edgeNum < graph.getOutEdgeCount(sourceId); edgeNum++) {
                    int edge = graph.getOutEdgeId(sourceId, edgeNum);
                    int targetId = graph.getTarget(edge);
                    // Ignore shortcuts and adding to "infinity"
                    if(graph.getFirstShortcuttedEdge(edge) >= 0 || dist[sourceId] == Integer.MAX_VALUE){
                        continue;
                    }

                    int tempDist = dist[sourceId] + graph.getDist(edge);
                    if (tempDist < dist[targetId]) {
                        preds[targetId] = edge;
                        dist[targetId] = tempDist;
                        queue.add(targetId);
                    }
                }
            }
            int euclidianDist = 0;
            int currNode = targetPointId;
            while (currNode != sourcePointId) {
                int currEdge = preds[currNode];
                euclidianDist += graph.getEuclidianDist(currEdge);
                currNode = graph.getSource(currEdge);
            }
            System.err.println("Imtermediate edist: "+euclidianDist+ " dist: "+dist[targetPointId]);

            expectedTotalEuclidianDist += euclidianDist;
            expectedTotalDist += dist[targetPointId];
        }


        assertEquals("The distance of test case " + testCaseNumber + " seems wrong. ", expectedTotalEuclidianDist, totalDist);
        System.err.println("Test case " + testCaseNumber + " finished.");
    }


    private void prepareTestRun() {
        random = new Random(1337);
        graph = (new TestGraphReader()).readTestGraph();
        nodeCount = graph.getNodeCount();
    }

    private void runTestCases(ShortestPath shortestPath) throws ComputeException, IllegalAccessException {
        for (int i = 0; i < 10; i++) {
            runTestCase(shortestPath, i, false);
            runTestCase(shortestPath, i, true);
            runTestCase(shortestPath, i, true);
            runTestCase(shortestPath, i, false);
        }

    }

    @Test
    public void testShortestPathNoCH() throws Exception {
        prepareTestRun();
        GraphAlgorithmFactory fac = new ShortestPathFactory(graph);
        ShortestPath shortestPath = (ShortestPath) fac.createAlgorithm();
        runTestCases(shortestPath);

    }


    @Test
    public void testShortestPathCH() throws Exception {
        prepareTestRun();
        GraphAlgorithmFactory fac = new ShortestPathCHFactory(graph);
        ShortestPath shortestPathCH = (ShortestPath) fac.createAlgorithm();

        runTestCases(shortestPathCH);
    }

    @Test
    public void testShortestPathBDCH() throws Exception {
        prepareTestRun();
        GraphAlgorithmFactory fac = new ShortestPathBDCHFactory(graph);
        ShortestPath shortestPathCH = (ShortestPath) fac.createAlgorithm();

        runTestCases(shortestPathCH);
    }
}
                            