package computecore;

import java.util.Map;

/**
 * Author: Niklas Schnelle
 * Date: 12/26/11
 * Time: 4:46 PM
 */
public class RequestPoint {
    private int lat;
    private int lon;
    private Map<String, Object> constraints;
    private int id;

    public RequestPoint(int lat, int lon, Map<String, Object> constraints) {
        this.lat = lat;
        this.lon = lon;
        this.constraints = constraints;
        this.setId(-1);
    }

    public int getLat() {
        return lat;
    }

    public void setLat(int lat) {
        this.lat = lat;
    }

    public int getLon() {
        return lon;
    }

    public void setLon(int lon) {
        this.lon = lon;
    }

    public Map<String, Object> getConstraints() {
        return constraints;
    }

    public void setConstraints(Map<String, Object> constraints) {
        this.constraints = constraints;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
