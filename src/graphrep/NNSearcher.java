package graphrep;

public interface NNSearcher {

	// it's best to use the protected lon[] and lat[] arrays in Graphrep for
	// accessing all nodes

	int getIDForCoordinates(float lat, float lon);
}
