package graphrep;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Logger;

public class GraphRepBinaryReader implements GraphRepReader {
    private static Logger log = Logger.getLogger("tourenplaner");

    private static final int version = 2;

    // fileformat is:
    // nodecount
    // edgecount
    // (nodecount) * (lat lon height rank)
    // (edgecount) * (source trgt dist mult)
    // (edgecount) * (mappingInToOut)
    // (nodecount+1) * (offsetIn)
    // (nodecount+1) * (offsetOut)

    @Override
    public GraphRep createGraphRep(InputStream in) throws IOException {

        // 8388608 Bytes = 8 MB
        BufferedInputStream bin = new BufferedInputStream(in, 8388608);
        DataInputStream din = new DataInputStream(bin);
        // Check the first 4 bytes to see if it's a ToureNPlaner Graphfile
        byte[] magic = new byte[4];
        din.readFully(magic);
        String magicString = new String(magic, Charset.forName("UTF-8"));
        if (!magicString.equals("TPG\n")) {
            throw new IOException("The given stream does not contain a ToureNPlaner Graphfile");
        }
        // Check whether we got the same version
        int realVersion = din.readInt();
        if (realVersion != version) {
            throw new IOException("Wrong file format version, expected " + version + " got " + realVersion);
        }

        int nodeCount = din.readInt();
        int edgeCount = din.readInt();

        GraphRep graphRep = new GraphRep(nodeCount, edgeCount);


        log.info("Reading " + nodeCount + " nodes and " + edgeCount + " edges ...");
        int lat, lon;
        int height;
        int rank;
        // Read nodes
        for (int i = 0; i < nodeCount; i++) {
            lat = din.readInt();
            lon = din.readInt();
            height = din.readInt();
            rank = din.readInt();
            graphRep.setNodeData(i, lat, lon, height);
            graphRep.setNodeRank(i, rank);
        }

        // temporary values for edges
        int src, trgt, dist, euclidianDist;
        int shortcuttedEdge1, shortcuttedEdge2;
        // Read edges
        for (int i = 0; i < edgeCount; i++) {
            src = din.readInt();
            trgt = din.readInt();
            dist = din.readInt();
            euclidianDist = din.readInt();
            shortcuttedEdge1 = din.readInt();
            shortcuttedEdge2 = din.readInt();
            graphRep.setEdgeData(i, src, trgt, dist, euclidianDist);
            graphRep.setShortcutData(i, shortcuttedEdge1, shortcuttedEdge2);
        }

        // Read mappingInToOut
        final int[] mappingInToOut = new int[edgeCount];
        for (int i = 0; i < edgeCount; i++) {
            mappingInToOut[i] = din.readInt();
        }
        graphRep.setMappingInToOut(mappingInToOut);

        // Read offsetIn
        final int[] offsetIn = new int[nodeCount + 1];
        for (int i = 0; i < nodeCount + 1; i++) {
            offsetIn[i] = din.readInt();
        }
        graphRep.setOffsetIn(offsetIn);

        // Read offsetOut
        final int[] offsetOut = new int[nodeCount + 1];
        for (int i = 0; i < nodeCount + 1; i++) {
            offsetOut[i] = din.readInt();
        }
        graphRep.setOffsetOut(offsetOut);

        in.close();
        log.info("successfully created offset of InEdges");
        // choose the NNSearcher here
        // DumbNN uses linear search and is slow.
        // HashNN should be faster but needs more RAM
        log.info("Start creating NNSearcher");
        graphRep.searcher = new HashNN(graphRep);
        System.gc();
        log.info("Graph loaded");
        return graphRep;
    }
}
