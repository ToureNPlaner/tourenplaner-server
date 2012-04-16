/*
 * Copyright 2012 ToureNPlaner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.tourenplaner.computecore;

import de.tourenplaner.graphrep.GraphRep;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Niklas Schnelle
 */
public class RequestPoints {
    private List<RequestPoint> points;

    public RequestPoints() {
        points = new ArrayList<RequestPoint>();
    }
    
    public int size(){
        return points.size();
    }

    /**
     * Sets the ids for all points from the given graph
     * please only call this method in the compute thread
     * as depending on the NNSearcher used it can be quite expensive
     *
     * @param graph
     */
    public void setIdsFromGraph(GraphRep graph){
        for (RequestPoint p: points){
            p.setId(graph.getIdForCoordinates(p.getLat(), p.getLon()));
        }
    }

    /** grap
     * 
     * Gets the id for point with index i
     *
     * @param i
     * @return
     */
    public int getPointId(int i){
        return points.get(i).getId();
    }

    /**
     * Sets the id for point with index i
     *
     * @param i
     * @param id
     */
    public void setPointId(int i, int id){
        points.get(i).setId(id);
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
        return (points.get(i).getConstraints() != null) ? points.get(i).getConstraints().get(conName) : null;
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
     * Gets the List of Way i.e. this RequestPoints backing store
     *
     * @return
     */
    public List<RequestPoint> getStore() {
        return points;
    }

    /**
     * Sets the List of Way i.e. this RequestPoints backing store
     *
     * @param newPoints
     */
    public void setStore(List<RequestPoint> newPoints){
        this.points = newPoints;
    }
}
