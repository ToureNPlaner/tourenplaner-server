package main

import (
	"fmt"
	//"bufio"
	"os"
	"flag"
	binary "encoding/binary"
	"math"
)

type OutEdge struct {
	TargetId          uint32
	Length            uint32
	Mult              float32
	ShortedId         uint32
	OutEdgeSourceNum  uint32
	OutEdgeShortedNum uint32
}

type Node struct {
	Lon       float64
	Lat       float64
	Elevation uint32
	Deg       uint32
	Rank      uint32
	OutEdges  []OutEdge
}

func (n *Node) initFromBytes(b []byte, order binary.ByteOrder) {
	n.Lon = math.Float64frombits(order.Uint64(b[0:8]))
	n.Lat = math.Float64frombits(order.Uint64(b[8:16]))
	n.Elevation = order.Uint32(b[16:20])
	n.Deg = order.Uint32(b[20:24])
}

func (e *OutEdge) initFromBytes(b []byte, order binary.ByteOrder) {
	e.TargetId = order.Uint32(b[0:4])
	e.Length = order.Uint32(b[4:8])
	e.Mult = math.Float32frombits(order.Uint32(b[8:12]))
	e.ShortedId = order.Uint32(b[12:16])
	e.OutEdgeSourceNum = order.Uint32(b[16:20])
	e.OutEdgeShortedNum = order.Uint32(b[20:24])
}

var (
	graphFilename string
	rankFilename  string
)

func init() {
	flag.StringVar(&graphFilename, "graph", "graph.dat", "The graph file to load")
	flag.StringVar(&rankFilename, "ranks", "graph_ids", "The rank file to load")
	flag.Parse()
}

func main() {

	fmt.Fprintln(os.Stderr, "Starting Graphconvert")
	graphFile, err := os.Open(graphFilename)

	if err != nil {
		fmt.Fprintln(os.Stderr, "An Error occured opening the graph file", graphFilename, " ", err)
		return
	}

	var rankFile *os.File = nil
	if rankFilename != "" {
		rankFile, err = os.Open(rankFilename)

		if err != nil {
			fmt.Fprintln(os.Stderr, "An Error occured opening the rank file", rankFilename, " ", err)
			return
		}
	}
	var (
		bufGraph []byte
		bufRank  []byte
		numNodes uint32
		numEdges uint32
	)

	bufGraph = make([]byte, 24)

	_, err = graphFile.Read(bufGraph[0:4])
	if rankFile != nil {

		bufRank = make([]byte, 4)
		_, err = rankFile.Read(bufRank)
		if err != nil {
			fmt.Fprintln(os.Stderr, err)
		}
	}
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		return
	}
	numNodes = binary.LittleEndian.Uint32(bufGraph[0:4])
	numNodesRank := binary.LittleEndian.Uint32(bufRank)

	if numNodes != numNodesRank {
		fmt.Fprintln(os.Stderr, "The number of nodes in the graph file does not match the number of nodes in the rank file", numNodes, "vs", numNodesRank)
		return

	}

	nodes := make([]Node, numNodes)

	for i := uint32(0); i < numNodes; i++ {

		_, err = graphFile.Read(bufGraph)
		if err != nil {
			fmt.Fprintln(os.Stderr, err)
			return
		}

		nodes[i].initFromBytes(bufGraph, binary.LittleEndian)
		if rankFile != nil {
			_, err = rankFile.Read(bufRank)
			if err != nil {
				fmt.Fprintln(os.Stderr, err)
				return
			}
			nodes[i].Rank = binary.LittleEndian.Uint32(bufRank)

		} else {
			nodes[i].Rank = math.MaxInt32
		}

		numEdges += nodes[i].Deg

		nodes[i].OutEdges = make([]OutEdge, nodes[i].Deg)

		for j := uint32(0); j < nodes[i].Deg; j++ {
			_, err = graphFile.Read(bufGraph)
			if err != nil {
				fmt.Fprintln(os.Stderr, "An IO Error occured", err)
				return
			}
			nodes[i].OutEdges[j].initFromBytes(bufGraph, binary.LittleEndian)
		}
	}

	// Print numNodes, numEdges
	fmt.Println(numNodes)
	fmt.Println(numEdges)

	for i := uint32(0); i < numNodes; i++ {
		// Need to scale rank to int32
		rank := nodes[i].Rank
		if rank > math.MaxInt32 {
			rank = math.MaxInt32
		}
		fmt.Println(i, nodes[i].Lat, nodes[i].Lon, nodes[i].Elevation)
	}

	for i := uint32(0); i < numNodes; i++ {
		for j := uint32(0); j < nodes[i].Deg; j++ {
			// Retrieve values and scale to int32 use -1 as special value
			targetId := int64(nodes[i].OutEdges[j].TargetId)
			length := int64(nodes[i].OutEdges[j].Length)
			mult := int64(nodes[i].OutEdges[j].Mult)
			shortedId := int64(nodes[i].OutEdges[j].ShortedId)
			outEdgeSourceNum := int64(nodes[i].OutEdges[j].OutEdgeSourceNum)
			outEdgeShortedNum := int64(nodes[i].OutEdges[j].OutEdgeShortedNum)
			if shortedId > math.MaxInt32 {
				shortedId = -1
				outEdgeSourceNum = -1
				outEdgeShortedNum = -1
			}

			fmt.Println(i, targetId, length, mult, shortedId, outEdgeSourceNum, outEdgeShortedNum)
		}
	}

}
