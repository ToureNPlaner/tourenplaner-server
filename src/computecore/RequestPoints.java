/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Niklas Schnelle
 */
public class RequestPoints {
    private List<RequestPoint> points;

    public RequestPoints() {
        super();
        points = new ArrayList<RequestPoint>();
    }
    
    public int size(){
        return points.size();
    }

    /**
     * Gets the latitude for point with index i
     *
     * @param i
     * @return
     */
    public int getPointLat(int i){
        return points.get(i).getLat();
    }

    /**
     * Gets the longitude for point with index i
     *
     * @param i
     * @return
     */
    public int getPointLon(int i) {
        return points.get(i).getLon();
    }

    /**
     * Sets the latitude for point with index i
     *
     * @param i
     */
    public void setPointLat(int i, int lat) {
        points.get(i).setLat(lat);
    }

    /**
     * Sets the longitude for point with index i
     *
     * @param i
     */
    public void setPointLon(int i, int lon) {
        points.get(i).setLon(lon);
    }

    public void addPoint(int lat, int lon, Map<String, Object> pconst) {
        points.add(new RequestPoint(lat, lon, pconst));
    }

    /**
     * Gets the Constraint with the name conName for point i
     *
     * @param i
     * @param conName
     * @return
     */
    public Object getConstraint(int i, String conName) {
        return (points.get(i).getConstraints() != null) ? points.get(i).getConstraints().get("conName") : null;
    }

    /**
     * Gets the Constraints of the point i
     *
     * @param i
     * @return
     */
    public Map<String, Object> getConstraints(int i) {
        return points.get(i).getConstraints();
    }

    /**
     * Adds a new point with null constraints
     *
     * @param lat
     * @param lon
     */
    public void addPoint(int lat, int lon) {
        this.addPoint(lat, lon, null);
    }

    /**
     * Gets the List of Points i.e. this RequestPoints backing store
     *
     * @return
     */
    public List<RequestPoint> getStore() {
        return points;
    }

    /**
     * Sets the List of Points i.e. this RequestPoints backing store
     *
     * @param newPoints
     */
    public void setStore(List<RequestPoint> newPoints){
        this.points = newPoints;
    }
}
