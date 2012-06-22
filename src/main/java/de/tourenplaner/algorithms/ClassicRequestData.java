package de.tourenplaner.algorithms;

import de.tourenplaner.computecore.RequestData;
import de.tourenplaner.computecore.RequestPoints;

import java.util.Map;

/**
 * @author Niklas Schnelle
 *
 * This class acts as the RequestData object for Algorithms
 * using the classic protocoll
 */
public class ClassicRequestData extends RequestData{
   private RequestPoints points;
   private Map<String, Object> constraints;

    /**
     * Returns the Points associated with this request
     *
     * @return RequestPoints
     */
    public RequestPoints getPoints() {
        return points;
    }

    /**
     * Returns the constraints associated with this request
     *
     * @return A Map representing the constraints
     */
    public Map<String, Object> getConstraints() {
        return constraints;
    }

    public ClassicRequestData(String algSuffix, RequestPoints points, Map<String, Object> constraints) {
        super(algSuffix);
        this.points = points;
        this.constraints = constraints;
    }
}
