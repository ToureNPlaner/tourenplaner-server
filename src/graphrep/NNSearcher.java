package graphrep;

import java.io.Serializable;

public interface NNSearcher extends Serializable {

	// it's best to use the protected lon[] and lat[] arrays in GraphRep for
	// accessing all nodes

	int getIDForCoordinates(double lat, double lon);
}
