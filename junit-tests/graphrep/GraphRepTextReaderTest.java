package graphrep;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GraphRepTextReaderTest {

	@Test
	public final void testCreateGraphRep() {
		GraphRepTextReader graphRepTextReader = new GraphRepTextReader();
		GraphRep graphRep;
		try {
            String path_15k;
            path_15k = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() +
                       "../data/test/15k_ch.txt";
            System.out.println("Loading graph from " + path_15k);
			graphRep = graphRepTextReader
					.createGraphRep(new FileInputStream(path_15k));

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
			
			// TODO wait for changed graphrep
			
			// assertEquals(13, graphRep.euklidianDist[0]);
			// assertEquals(5, graphRep.euklidianDist[1]);
			// assertEquals(13, graphRep.euklidianDist[2]);
			// assertEquals(8, graphRep.euklidianDist[3]);
			// assertEquals(5, graphRep.euklidianDist[4]);
			
			
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
