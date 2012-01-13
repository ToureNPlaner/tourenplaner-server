package graphrep;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GraphRepDumpReaderTest {

    @Test
    public final void testCreateGraphRep() {

        try {
            GraphRep graphRep = (new TestGraphReader()).readTestGraph();

            // dump
            ByteArrayOutputStream byteArrayOutputStreamDump = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStreamDump);
            oos.writeObject(graphRep);
            oos.close();

            // load dumped
            GraphRepDumpReader graphRepDumpReader = new GraphRepDumpReader();
            ByteArrayInputStream
                    testDumpFileByteArrayStream =
                    new ByteArrayInputStream(byteArrayOutputStreamDump.toByteArray());
            GraphRep graphRepDump;
            graphRepDump = graphRepDumpReader.createGraphRep(testDumpFileByteArrayStream);

            assertEquals(14505, graphRep.getNodeCount());
            assertEquals(59600, graphRep.getEdgeCount());
            assertEquals(4.84878025E8, graphRep.lat[0], 0.01);
            assertEquals(4.84876093E8, graphRep.lat[1], 0.01);
            assertEquals(4.84870879E8, graphRep.lat[2], 0.01);
            assertEquals(9.1901961E7, graphRep.lon[0], 0.01);
            assertEquals(9.1902854E7, graphRep.lon[1], 0.01);
            assertEquals(9.1906707E7, graphRep.lon[2], 0.01);
            assertEquals(386, graphRep.height[0]);
            assertEquals(386, graphRep.height[1]);
            assertEquals(377, graphRep.height[2]);

            assertEquals(0, graphRep.src[0]);
            assertEquals(0, graphRep.src[1]);
            assertEquals(0, graphRep.src[2]);
            assertEquals(0, graphRep.src[3]);
            assertEquals(0, graphRep.src[4]);
            assertEquals(1, graphRep.trgt[0]);
            assertEquals(1823, graphRep.trgt[1]);
            assertEquals(1824, graphRep.trgt[2]);
            assertEquals(1825, graphRep.trgt[3]);
            assertEquals(10463, graphRep.trgt[4]);

            assertEquals(22, graphRep.getEuclidianDist(0));
            assertEquals(99, graphRep.getEuclidianDist(1));
            assertEquals(66, graphRep.getEuclidianDist(2));
            assertEquals(110, graphRep.getEuclidianDist(3));
            assertEquals(30, graphRep.getEuclidianDist(4));

            assertEquals(22, graphRep.dist[0]);
            assertEquals(124, graphRep.dist[1]);
            assertEquals(66, graphRep.dist[2]);
            assertEquals(165, graphRep.dist[3]);
            assertEquals(30, graphRep.dist[4]);

            assertEquals(0, graphRep.offsetOut[0]);
            assertEquals(5, graphRep.offsetOut[1]);
            assertEquals(14, graphRep.offsetOut[2]);

            assertEquals(0, graphRep.offsetIn[0]);
            assertEquals(5, graphRep.offsetIn[1]);
            assertEquals(14, graphRep.offsetIn[2]);
        } catch (IOException e) {
            fail(e.getMessage());
        }

    }

}
