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
};

struct Edge {
    uint32_t sourceId;
    uint32_t targetId;
    uint32_t length;
    uint32_t shortcuttedEdge1;
    uint32_t shortcuttedEdge2;
    
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
    
    uint32_t n,nr,ne;
    // Number of nodes read from graph file
    gin.read((char*)&n, sizeof(uint32_t));
    // Number of edges from graph file
    gin.read((char*)&ne, sizeof(uint32_t));
    
    // Number of nodes read from rank file
    rin.read((char*)&nr, sizeof(uint32_t));
    
    if(nr != n){
        cerr << "Number of nodes in rank file does not match graph file " << n << "vs"<<nr<<endl; 
        return 1;
    }
    
    Node* nodes = new Node[n];
    Edge* edges = new Edge[ne];
    // Read nodes
    for(unsigned int i=0 ; i<n; ++i) {        
        gin.read((char*)&nodes[i].data.lon,sizeof(double));
        gin.read((char*)&nodes[i].data.lat,sizeof(double));
        gin.read((char*)&nodes[i].data.ele,sizeof(int32_t));
        // Read the rank of the node from rank file
        rin.read((char*)&nodes[i].rank, sizeof(uint32_t));
    }
    
    // Read edges
    for(unsigned int i=0; i<ne; ++i){
        //gin.read((char*)&edges[i], sizeof(Edge));
        gin.read((char*)&edges[i].sourceId, sizeof(uint32_t));
        gin.read((char*)&edges[i].targetId, sizeof(uint32_t));
        gin.read((char*)&edges[i].length, sizeof(uint32_t));
        gin.read((char*)&edges[i].shortcuttedEdge1, sizeof(uint32_t));
        gin.read((char*)&edges[i].shortcuttedEdge2, sizeof(uint32_t));
    }
    
    // Write num nodes, num edges
    cout << n << endl;
    cout << ne << endl;
    // Write Nodes
    for(unsigned int i=0 ; i<n; ++i) {
       cout << i << " ";
       cout << ((int32_t) (nodes[i].data.lat*10000000.0)) << " ";
       cout << ((int32_t) (nodes[i].data.lon*10000000.0)) << " ";
       cout << nodes[i].data.ele << " ";
       cout << ((nodes[i].rank < (uint32_t)numeric_limits<int32_t>::max())?(int32_t)nodes[i].rank:(int32_t)numeric_limits<int32_t>::max())<< endl;
    }
 
    
    // Write Edges
    for(unsigned int i=0 ; i<ne; ++i) {
            cout << edges[i].sourceId << " " ;
            cout << edges[i].targetId << " " ;
            cout << edges[i].length << " ";
            cout << ((edges[i].shortcuttedEdge1< (uint32_t) numeric_limits<int32_t>::max())? (int32_t)edges[i].shortcuttedEdge1 : -1)<< " ";
            cout << ((edges[i].shortcuttedEdge2 < (uint32_t) numeric_limits<int32_t>::max())? (int32_t)edges[i].shortcuttedEdge2 : -1)<< endl;
    }
       
    gin.close();
    rin.close();
}

