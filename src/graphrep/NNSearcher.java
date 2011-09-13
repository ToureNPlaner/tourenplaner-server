package graphrep;

import java.io.Serializable;

public interface NNSearcher extends Serializable {

	// it's best to use the protected lon[] and lat[] arrays in Graphrep for
	// accessing all nodes

	int getIDForCoordinates(float lat, float lon);
}
