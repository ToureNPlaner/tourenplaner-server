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

import fmi.graph.chgraph.Edge;
import fmi.graph.chgraph.Node;
import fmi.graph.chgraph.Reader;
import fmi.graph.definition.GraphException;
import fmi.graph.metaio.MetaData;

import java.io.*;
import java.util.logging.Logger;

/**
 * @author Niklas Schnelle
 */
public class GraphRepStandardReader implements GraphRepReader{
	private static Logger log = Logger.getLogger("de.tourenplaner.graphrep");

	private boolean binary;

	public GraphRepStandardReader(boolean binary){
		this.binary = binary;
	}

	// see file format specification for a description of the format
	@Override
	public GraphRep createGraphRep(InputStream in) throws IOException {

		BufferedInputStream inb = new BufferedInputStream(in);
		Reader r = new Reader(false, true);
		Node n;
		Edge e;
		int edgeCount, edge;
		int nodeCount;
		try {
			System.currentTimeMillis();
			MetaData meta;
			if (binary){
				meta = r.readBin(inb);
			} else {
				meta = r.read(inb);
			}


			nodeCount = r.getNodeCount();
			edgeCount = r.getEdgeCount();
			GraphRep graphRep = new GraphRep(nodeCount, edgeCount);

			log.info("Graph: "+meta.get("Id")+" created at " + meta.get("Timestamp").asDate()+
					"\nfrom source graph " + meta.get("OriginId")+
					"\ncreated at "+ meta.get("OriginTimestamp").asDate());

			int lat, lon;
			while (r.hasNextNode()) {
				n = r.nextNode();
				lat = (int) (n.getLat()*1e7);
				lon = (int) (n.getLon()*1e7);
				graphRep.setNodeData(n.getId(), lat, lon, n.getElevation());
				graphRep.setRank(n.getId(), n.getLevel());
			}

			edge = 0;
			while (r.hasNextEdge()) {
				e = r.nextEdge();
				graphRep.setEdgeData(edge, e.getSource(), e.getTarget(), e.getWeight(), e.getEuclidianDist());
				graphRep.setShortcutData(edge, e.getSkippedEdgeA(), e.getSkippedEdgeB());
				edge++;
			}
			r.close();

			log.info("Setting up graph");
			graphRep.setup();
			log.info("graph setup");
			return graphRep;
		} catch (GraphException ex) {
			throw new IOException(ex);
		}
	}
}
