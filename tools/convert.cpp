#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <limits>
#include <assert.h>
#include <stdint.h>

using namespace std;

struct NodeData {
    double lon;
    double lat;
    int32_t ele;
    uint32_t deg;
};

struct Edge {
    uint32_t targetId;
    uint32_t length;
    float mult;
    uint32_t shortedId;
    uint32_t outEdgeNumSource;
    uint32_t outEdgeNumShorted;
};

struct Node {
    NodeData data;
    uint32_t rank;
    Edge* outEdges;
};







int main(int args, char** argv) {
    // assert endianess
    int x = ('b'<<8)+'a';
    char* y = (char*)&x;
    assert(y[0]=='a' && y[1]=='b');
    // assert type sizes
    assert(sizeof(Edge) == 6*4);
    assert(sizeof(NodeData) == 2*8+2*4);
    string graph(args >=2?argv[1]:"graph.dat");
    string ranks(args >=3?argv[2]:"graph_ids");

    ifstream gin(graph.c_str(), ios::in | ios::binary);
    ifstream rin(ranks.c_str(), ios::out | ios::binary);
    
    if(!gin.good()) {
        cerr << "Could not open file for reading: "<< graph << endl;
        return 1;
    }
    
    if(!rin.good()) {
        cerr << "Could not open file reading: "<< ranks << endl;
        return 1;
    }
    
    unsigned int n,nr;
    // Number of nodes read from graph file
    gin.read((char*)&n,sizeof(uint32_t));
    // Number of nodes read from rank file
    rin.read((char*)&nr, sizeof(uint32_t));
    
    if(nr != n){
        cerr << "Number of nodes in rank file does not match graph file " << n << "vs"<<nr<<endl; 
        return 1;
    }
    
    Node* nodes = new Node[n];
    
    unsigned int edgeNum = 0;
    
    for(unsigned int i=0 ; i<n; ++i) {
        
        gin.read((char*)&nodes[i].data,sizeof(NodeData));
        // Read the rank of the node from rank file
        rin.read((char*)&nodes[i].rank, sizeof(uint32_t));
        
        nodes[i].outEdges = new Edge[nodes[i].data.deg];
        
        gin.read((char*)&nodes[i].outEdges[0],sizeof(Edge)*nodes[i].data.deg);
        edgeNum+=nodes[i].data.deg;

    }
    // Write num nodes, num edges
    cout << n << endl;
    cout << edgeNum<< endl;
    // Write Nodes
    for(unsigned int i=0 ; i<n; ++i) {
       cout << i << " ";
       cout << nodes[i].data.lat << " ";
       cout << nodes[i].data.lon << " ";
       cout << nodes[i].data.ele << " ";
       cout << ((nodes[i].rank < (uint32_t)numeric_limits<int32_t>::max())?(int32_t)nodes[i].rank:(int32_t)numeric_limits<int32_t>::max())<< endl;
    }
 
    
    // Write Edges
    for(unsigned int i=0 ; i<n; ++i) {
        for(unsigned int j=0; j < nodes[i].data.deg; j++){
            cout << i << " ";
            cout << nodes[i].outEdges[j].targetId << " " ;
            cout << nodes[i].outEdges[j].length << " ";
            cout << nodes[i].outEdges[j].mult << " ";
            cout << ((nodes[i].outEdges[j].shortedId < (uint32_t)numeric_limits<int32_t>::max())? (int32_t)nodes[i].outEdges[j].shortedId : -1)<< " ";
            cout << ((nodes[i].outEdges[j].outEdgeNumSource < (uint32_t) numeric_limits<int32_t>::max())? (int32_t)nodes[i].outEdges[j].outEdgeNumSource : -1)<< " ";
            cout << ((nodes[i].outEdges[j].outEdgeNumShorted < (uint32_t) numeric_limits<int32_t>::max())? (int32_t)nodes[i].outEdges[j].outEdgeNumShorted : -1)<< endl;
        
        }

    }
       
    gin.close();
    rin.close();
}

