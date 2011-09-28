package graphrep;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DumbNNTest {

	@Test
	public final void testGetIDForCoordinates() {

		GraphRep graphRep = new GraphRep();
		graphRep.lat = new double[3];
		graphRep.lon = new double[3];
		graphRep.lat[0] = 2.5F;
		graphRep.lat[1] = 5.0F;
		graphRep.lat[2] = 2.0F;
		graphRep.lon[0] = 1.5F;
		graphRep.lon[1] = 3.0F;
		graphRep.lon[2] = 4.0F;
		graphRep.nodeCount = 3;

		graphrep.DumbNN dumpNN = new DumbNN(graphRep);
		assertEquals("getIDForCoordinates", 0,
				dumpNN.getIDForCoordinates(1.5F, 2.0F));
	}
}
