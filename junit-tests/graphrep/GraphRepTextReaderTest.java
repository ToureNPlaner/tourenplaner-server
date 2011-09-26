package graphrep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

public class GraphRepTextReaderTest {

	@Test
	public final void testCreateGraphRep() {
		GraphRepTextReader graphRepTextReader = new GraphRepTextReader();
		String testFile = new String(
				"3\n5\n0 2.5 1.5 5\n 5.0 3.0 4\n2 2.0 4.0 3\n0 1 5 0.5\n0 2 2 0.5\n1 0 5 0.5\n1 2 3 0.5\n2 0 2 0.5");
		byte[] testFileBytes = testFile.getBytes();
		ByteArrayInputStream testFileByteArrayStream = new ByteArrayInputStream(
				testFileBytes);
		GraphRep graphRep;
		try {

			graphRep = graphRepTextReader
					.createGraphRep(testFileByteArrayStream);

			assertEquals(3, graphRep.nodeCount);
			assertEquals(5, graphRep.edgeCount);

			assertEquals(2.5, graphRep.lat[0], 0.01);
			assertEquals(5.0, graphRep.lat[1], 0.01);
			assertEquals(2.0, graphRep.lat[2], 0.01);
			assertEquals(1.5, graphRep.lon[0], 0.01);
			assertEquals(3.0, graphRep.lon[1], 0.01);
			assertEquals(4.0, graphRep.lon[2], 0.01);
			assertEquals(5, graphRep.height[0]);
			assertEquals(4, graphRep.height[1]);
			assertEquals(3, graphRep.height[2]);

			assertEquals(0, graphRep.src_out[0]);
			assertEquals(0, graphRep.src_out[1]);
			assertEquals(1, graphRep.src_out[2]);
			assertEquals(1, graphRep.src_out[3]);
			assertEquals(2, graphRep.src_out[4]);
			assertEquals(1, graphRep.dest_out[0]);
			assertEquals(2, graphRep.dest_out[1]);
			assertEquals(0, graphRep.dest_out[2]);
			assertEquals(2, graphRep.dest_out[3]);
			assertEquals(0, graphRep.dest_out[4]);
			assertEquals(13, graphRep.multipliedDist_out[0]);
			assertEquals(5, graphRep.multipliedDist_out[1]);
			assertEquals(13, graphRep.multipliedDist_out[2]);
			assertEquals(8, graphRep.multipliedDist_out[3]);
			assertEquals(5, graphRep.multipliedDist_out[4]);
			assertEquals(5, graphRep.dist_out[0]);
			assertEquals(2, graphRep.dist_out[1]);
			assertEquals(5, graphRep.dist_out[2]);
			assertEquals(3, graphRep.dist_out[3]);
			assertEquals(2, graphRep.dist_out[4]);

			assertEquals(0, graphRep.offsetOut[0]);
			assertEquals(2, graphRep.offsetOut[1]);
			assertEquals(4, graphRep.offsetOut[2]);

			for (int i = 0; i < 3; i++) {
				assertTrue(graphRep.dest_in[i] <= graphRep.dest_in[i + 1]);
			}

			assertEquals(0, graphRep.offsetIn[0]);
			assertEquals(2, graphRep.offsetIn[1]);
			assertEquals(3, graphRep.offsetIn[2]);

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
