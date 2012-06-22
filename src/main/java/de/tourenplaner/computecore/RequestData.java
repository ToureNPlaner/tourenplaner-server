package de.tourenplaner.computecore;

/**
 * @author Niklas Schnelle
 *
 * This class acts as an abstract base for data classes storing the
 * request information for a ComputeRequest
 *
 */
public abstract class RequestData {
    private String algSuffix;

    public RequestData(String algSuffix){
        this.algSuffix = algSuffix;
    }

    public String getAlgorithmURLSuffix() {
        return algSuffix;
    }
}
