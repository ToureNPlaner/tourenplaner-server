package graphrep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Test;

public class GraphRepDumpReaderTest {

	@Test
	public final void testCreateGraphRep() {

		try {
			// makes somthing to dump
			GraphRepTextReader graphRepTextReader = new GraphRepTextReader();
			String testFile = new String(
					"3\n5\n0 2.5 1.5 5\n 5.0 3.0 4\n2 2.0 4.0 3\n0 1 5 0.5\n0 2 2 0.5\n1 0 5 0.5\n1 2 3 0.5\n2 0 2 0.5");
			byte[] testFileBytes = testFile.getBytes();
			ByteArrayInputStream testFileByteArrayStream = new ByteArrayInputStream(
					testFileBytes);
			GraphRep graphRep = graphRepTextReader
					.createGraphRep(testFileByteArrayStream);

			// dump
			ByteArrayOutputStream byteArrayOutputStreamDump = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(
					byteArrayOutputStreamDump);
			oos.writeObject(graphRep);
			oos.close();

			// load dumped
			GraphRepDumpReader graphRepDumpReader = new GraphRepDumpReader();

			ByteArrayInputStream testDumpFileByteArrayStream = new ByteArrayInputStream(
					byteArrayOutputStreamDump.toByteArray());

			GraphRep graphRepDump;

			graphRepDump = graphRepDumpReader
					.createGraphRep(testDumpFileByteArrayStream);
			assertEquals(3, graphRepDump.nodeCount);
			assertEquals(5, graphRepDump.edgeCount);

			assertEquals(2.5, graphRepDump.lat[0], 0.01);
			assertEquals(5.0, graphRepDump.lat[1], 0.01);
			assertEquals(2.0, graphRepDump.lat[2], 0.01);
			assertEquals(1.5, graphRepDump.lon[0], 0.01);
			assertEquals(3.0, graphRepDump.lon[1], 0.01);
			assertEquals(4.0, graphRepDump.lon[2], 0.01);
			assertEquals(5, graphRepDump.height[0]);
			assertEquals(4, graphRepDump.height[1]);
			assertEquals(3, graphRepDump.height[2]);

			assertEquals(0, graphRepDump.src_out[0]);
			assertEquals(0, graphRepDump.src_out[1]);
			assertEquals(1, graphRepDump.src_out[2]);
			assertEquals(1, graphRepDump.src_out[3]);
			assertEquals(2, graphRepDump.src_out[4]);
			assertEquals(1, graphRepDump.dest_out[0]);
			assertEquals(2, graphRepDump.dest_out[1]);
			assertEquals(0, graphRepDump.dest_out[2]);
			assertEquals(2, graphRepDump.dest_out[3]);
			assertEquals(0, graphRepDump.dest_out[4]);
			assertEquals(13, graphRepDump.multipliedDist_out[0]);
			assertEquals(5, graphRepDump.multipliedDist_out[1]);
			assertEquals(13, graphRepDump.multipliedDist_out[2]);
			assertEquals(8, graphRepDump.multipliedDist_out[3]);
			assertEquals(5, graphRepDump.multipliedDist_out[4]);
			assertEquals(5, graphRepDump.dist_out[0]);
			assertEquals(2, graphRepDump.dist_out[1]);
			assertEquals(5, graphRepDump.dist_out[2]);
			assertEquals(3, graphRepDump.dist_out[3]);
			assertEquals(2, graphRepDump.dist_out[4]);

			assertEquals(0, graphRepDump.offsetOut[0]);
			assertEquals(2, graphRepDump.offsetOut[1]);
			assertEquals(4, graphRepDump.offsetOut[2]);

			for (int i = 0; i < 3; i++) {
				assertTrue(graphRepDump.dest_in[i] <= graphRepDump.dest_in[i + 1]);
			}

			assertEquals(0, graphRepDump.offsetIn[0]);
			assertEquals(2, graphRepDump.offsetIn[1]);
			assertEquals(3, graphRepDump.offsetIn[2]);

			// has to be implemented
			// assertEquals(0.000, graphRep.elev_out[0], 0.01);
			// assertEquals(0.000, graphRep.elev_out[1], 0.01);
			// assertEquals(0.000, graphRep.elev_out[2], 0.01);
			// assertEquals(0.000, graphRep.elev_in[0], 0.01);
			// assertEquals(0.000, graphRep.elev_in[1], 0.01);
			// assertEquals(0.000, graphRep.elev_in[2], 0.01);
		} catch (IOException e) {
			fail(e.getMessage());
		}

	}

}
