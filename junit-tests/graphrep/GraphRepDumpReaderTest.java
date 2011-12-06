package graphrep;

import static org.junit.Assert.assertEquals;
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
					"3\n5\n0 25 15 5\n 50 30 4\n2 20 40 3\n0 1 5 \n0 2 2 \n1 0 5 \n1 2 3 \n2 0 2 ");
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
			assertEquals(3, graphRepDump.getNodeCount());
			assertEquals(5, graphRepDump.getEdgeCount());

			assertEquals(25, graphRepDump.lat[0], 0.01);
			assertEquals(50, graphRepDump.lat[1], 0.01);
			assertEquals(20, graphRepDump.lat[2], 0.01);
			assertEquals(15, graphRepDump.lon[0], 0.01);
			assertEquals(30, graphRepDump.lon[1], 0.01);
			assertEquals(40, graphRepDump.lon[2], 0.01);
			assertEquals(5, graphRepDump.height[0]);
			assertEquals(4, graphRepDump.height[1]);
			assertEquals(3, graphRepDump.height[2]);

			assertEquals(0, graphRepDump.src[0]);
			assertEquals(0, graphRepDump.src[1]);
			assertEquals(1, graphRepDump.src[2]);
			assertEquals(1, graphRepDump.src[3]);
			assertEquals(2, graphRepDump.src[4]);
			assertEquals(1, graphRepDump.trgt[0]);
			assertEquals(2, graphRepDump.trgt[1]);
			assertEquals(0, graphRepDump.trgt[2]);
			assertEquals(2, graphRepDump.trgt[3]);
			assertEquals(0, graphRepDump.trgt[4]);
			assertEquals(5, graphRepDump.dist[0]);
			assertEquals(2, graphRepDump.dist[1]);
			assertEquals(5, graphRepDump.dist[2]);
			assertEquals(3, graphRepDump.dist[3]);
			assertEquals(2, graphRepDump.dist[4]);

			assertEquals(0, graphRepDump.offsetOut[0]);
			assertEquals(2, graphRepDump.offsetOut[1]);
			assertEquals(4, graphRepDump.offsetOut[2]);

			assertEquals(0, graphRepDump.offsetIn[0]);
			assertEquals(2, graphRepDump.offsetIn[1]);
			assertEquals(3, graphRepDump.offsetIn[2]);

		} catch (IOException e) {
			fail(e.getMessage());
		}

	}

}
