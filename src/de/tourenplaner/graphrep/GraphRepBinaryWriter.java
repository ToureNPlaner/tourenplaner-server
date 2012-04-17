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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Logger;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 *
 */
public class GraphRepBinaryWriter implements GraphRepWriter {
    private static Logger log = Logger.getLogger("de.tourenplaner.graphrep");

    private static final int version = 2;

    @Override
    public void writeGraphRep(OutputStream out, GraphRep graphRep) throws IOException {
        // 8388608 Bytes = 8 MB
        BufferedOutputStream bout = new BufferedOutputStream(out, 8388608);
        DataOutputStream dout = new DataOutputStream(bout);
        // Write magic bytes so we can identify the ToureNPlaner Graphfileformat
        dout.write("TPG\n".getBytes(Charset.forName("UTF-8")));
        // Write format version
        dout.writeInt(version);

        // Write number of nodes and edges
        int numNodes = graphRep.getNodeCount();
        int numEdges = graphRep.getEdgeCount();
        dout.writeInt(numNodes);
        dout.writeInt(numEdges);

        // Write nodes
        for (int i = 0; i < numNodes; i++) {
            dout.writeInt(graphRep.getNodeLat(i));
            dout.writeInt(graphRep.getNodeLon(i));
            dout.writeInt(graphRep.getNodeHeight(i));
            dout.writeInt(graphRep.getRank(i));
        }

        // Write edges
        for (int i = 0; i < numEdges; i++) {
            dout.writeInt(graphRep.getSource(i));
            dout.writeInt(graphRep.getTarget(i));
            dout.writeInt(graphRep.getDist(i));
            dout.writeInt(graphRep.getEuclidianDist(i));
            dout.writeInt(graphRep.getFirstShortcuttedEdge(i));
            dout.writeInt(graphRep.getSecondShortcuttedEdge(i));
        }

        // Write mapping
        int[] mappingInToOut = graphRep.getMappingInToOut();

        for (int aMappingInToOut : mappingInToOut) {
            dout.writeInt(aMappingInToOut);
        }

        // Write offsetIn
        int[] offsetIn = graphRep.getOffsetIn();

        for (int anOffsetIn : offsetIn) {
            dout.writeInt(anOffsetIn);
        }

        // Write offsetOut
        int[] offsetOut = graphRep.getOffsetOut();

        for (int anOffsetOut : offsetOut) {
            dout.writeInt(anOffsetOut);
        }

        bout.flush();
        dout.flush();
        dout.close();
        log.info("Successfully wrote graph");
    }
}
