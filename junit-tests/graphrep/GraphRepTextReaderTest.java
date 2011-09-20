package graphrep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class GraphRepTextReaderTest {

	@Test
	public final void testCreateGraphRep() {

		try {
			GraphRepTextReader graphRepTextReader = new GraphRepTextReader();
			GraphRep graphRep = graphRepTextReader
					.createGraphRep("GraphRepTextReaderTestGraph.txt");

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

			assertEquals(0, graphRep.source_out[0]);
			assertEquals(0, graphRep.source_out[1]);
			assertEquals(1, graphRep.source_out[2]);
			assertEquals(1, graphRep.source_out[3]);
			assertEquals(2, graphRep.source_out[4]);
			assertEquals(1, graphRep.dest_out[0]);
			assertEquals(2, graphRep.dest_out[1]);
			assertEquals(0, graphRep.dest_out[2]);
			assertEquals(2, graphRep.dest_out[3]);
			assertEquals(0, graphRep.dest_out[4]);
			assertEquals(0, graphRep.mult_out[0]);
			assertEquals(0, graphRep.mult_out[1]);
			assertEquals(0, graphRep.mult_out[2]);
			assertEquals(0, graphRep.mult_out[3]);
			assertEquals(0, graphRep.mult_out[4]);
			assertEquals(5, graphRep.dist_out[0]);
			assertEquals(2, graphRep.dist_out[1]);
			assertEquals(5, graphRep.dist_out[2]);
			assertEquals(3, graphRep.dist_out[3]);
			assertEquals(2, graphRep.dist_out[4]);

			assertEquals(0, graphRep.offsetOut[0]);
			assertEquals(2, graphRep.offsetOut[1]);
			assertEquals(3, graphRep.offsetOut[2]);

			for (int i = 0; i < 3; i++) {
				assertTrue(graphRep.dest_in[i] < graphRep.dest_in[i + 1]);
			}

			assertEquals(0, graphRep.offsetIn[0]);
			assertEquals(0, graphRep.offsetIn[2]);
			assertEquals(0, graphRep.offsetIn[3]);

			// has to be implemented
			// assertEquals(0.000, graphRep.elev_out[0], 0.01);
			// assertEquals(0.000, graphRep.elev_out[1], 0.01);
			// assertEquals(0.000, graphRep.elev_out[2], 0.01);
			// assertEquals(0.000, graphRep.elev_in[0], 0.01);
			// assertEquals(0.000, graphRep.elev_in[1], 0.01);
			// assertEquals(0.000, graphRep.elev_in[2], 0.01);

		} catch (IOException e) {

		}
	}
}
