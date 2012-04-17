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

package de.tourenplaner.graphrep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class GraphRepTextReader implements GraphRepReader {
    private static Logger log = Logger.getLogger("de.tourenplaner.graphrep");
    private static final Pattern COMPILE = Pattern.compile(" ");

    // see file format specification for a description of the format
    @Override
    public GraphRep createGraphRep(InputStream in) throws IOException {

        BufferedReader inb = new BufferedReader(new InputStreamReader(in));

        String line;

        // exception should happen when file format is wrong
        line = inb.readLine();
        int edgeCount;
        int nodeCount;

        while (line != null && line.trim().startsWith("#")) {
            line = inb.readLine();
        }
        nodeCount = line != null ? Integer.parseInt(line) : 0;

        line = inb.readLine();
        edgeCount = line != null ? Integer.parseInt(line) : 0;

        GraphRep graphRep = new GraphRep(nodeCount, edgeCount);

        // used for splitted lines in 1. nodes 2. edges
        String[] splittedLine;

        log.info("Reading " + nodeCount + " nodes and " + edgeCount + " edges ...");
        int lat, lon;
        int height;

        for (int i = 0; i < nodeCount; i++) {
            splittedLine = COMPILE.split(inb.readLine());
            lat = Integer.parseInt(splittedLine[0]);
            lon = Integer.parseInt(splittedLine[1]);
            height = Integer.parseInt(splittedLine[2]);
            graphRep.setNodeData(i, lat, lon, height);

            if (splittedLine.length == 4) {
                graphRep.setNodeRank(i, Integer.parseInt(splittedLine[3]));
            }

        }

        // temporary values for edges
        int src, dest, dist, euclidianDist;
        int shortcuttedEdge1, shortcuttedEdge2;
        for (int i = 0; i < edgeCount; i++) {

            splittedLine = COMPILE.split(inb.readLine());

            src = Integer.parseInt(splittedLine[0]);
            dest = Integer.parseInt(splittedLine[1]);
            dist = Integer.parseInt(splittedLine[2]);
            euclidianDist = Integer.parseInt(splittedLine[3]);

            graphRep.setEdgeData(i, src, dest, dist, euclidianDist);

            if (splittedLine.length == 6) {
                shortcuttedEdge1 = Integer.parseInt(splittedLine[4]);
                shortcuttedEdge2 = Integer.parseInt(splittedLine[5]);

                graphRep.setShortcutData(i, shortcuttedEdge1, shortcuttedEdge2);
            }

        }
        in.close();
        log.info("Start generating offsets");
        graphRep.generateOffsets();
        log.info("successfully created offset of InEdges");

        return graphRep;
    }
}
