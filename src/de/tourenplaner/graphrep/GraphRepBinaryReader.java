package de.tourenplaner.graphrep;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.logging.Logger;

public class GraphRepBinaryReader implements GraphRepReader {
    private static Logger log = Logger.getLogger("de.tourenplaner.graphrep");

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


        DataInputStream din = new DataInputStream(in);
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

        // read all the nodes in a byte array, wrap this array in a bytebuffer, set the byte order to Big endian and
        // then look at this buffer as if it were an int buffer

        ByteBuffer temp = ByteBuffer.allocate(edgeCount * 6 * 4);
        din.read(temp.array(), 0, nodeCount * 4 * 4);
        IntBuffer tempib = temp.order(ByteOrder.BIG_ENDIAN).asIntBuffer();
        tempib.rewind();
        int lat, lon;
        int height;
        int rank;
        // Read nodes
        for (int i = 0; i < nodeCount; i++) {
            lat = tempib.get();
            lon = tempib.get();
            height = tempib.get();
            rank = tempib.get();
            graphRep.setNodeData(i, lat, lon, height);
            graphRep.setNodeRank(i, rank);
        }


        din.read(temp.array(), 0, edgeCount * 6 * 4);
        tempib.rewind();
        // temporary values for edges
        int src, trgt, dist, euclidianDist;
        int shortcuttedEdge1, shortcuttedEdge2;
        // Read edges
        for (int i = 0; i < edgeCount; i++) {
            src = tempib.get();
            trgt = tempib.get();
            dist = tempib.get();
            euclidianDist = tempib.get();
            shortcuttedEdge1 = tempib.get();
            shortcuttedEdge2 = tempib.get();
            graphRep.setEdgeData(i, src, trgt, dist, euclidianDist);
            graphRep.setShortcutData(i, shortcuttedEdge1, shortcuttedEdge2);
        }

        // Read mappingInToOut
        final int[] mappingInToOut = new int[edgeCount];
        din.read(temp.array(), 0, edgeCount  * 4);
        tempib.rewind();
        for (int i = 0; i < edgeCount; i++) {
            mappingInToOut[i] = tempib.get();
        }
        graphRep.setMappingInToOut(mappingInToOut);

        // Read offsetIn
        final int[] offsetIn = new int[nodeCount + 1];
        tempib.rewind();
        din.read(temp.array(), 0, (nodeCount+1)  * 4);
        for (int i = 0; i < nodeCount + 1; i++) {
            offsetIn[i] = tempib.get();
        }
        graphRep.setOffsetIn(offsetIn);

        // Read offsetOut
        final int[] offsetOut = new int[nodeCount + 1];
        tempib.rewind();
        din.read(temp.array(), 0, (nodeCount+1)  * 4);
        for (int i = 0; i < nodeCount + 1; i++) {
            offsetOut[i] = tempib.get();
        }
        graphRep.setOffsetOut(offsetOut);

        in.close();
        return graphRep;
    }
}
