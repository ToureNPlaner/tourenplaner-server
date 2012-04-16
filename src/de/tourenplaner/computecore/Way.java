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

import com.carrotsearch.hppc.IntArrayList;

/**
 * Class used to store the route between two
 * requested points
 */
public class Way {
	private final IntArrayList points;
    private int distance;
    private double travelTime;

	public Way() {
		points = new IntArrayList();
	}

    /**
     * Adds a point to this way
     *
     * @param lat the latitude of the point as deg*10^7
     * @param lon the longitude of the point as deg*10^7
     */
	public void addPoint(int lat, int lon) {
		points.add(lat);
		points.add(lon);
	}

    /**
     * Adds num points to this way so that they can be modified with
     * setPointLat/Lon without resizing at every point
     *
     * @param num number of points to add
     */
	public void addEmptyPoints(int num) {
		points.resize(points.size() + num * 2);
	}

    /**
     * Gets the latitude of a point
     *
     * @param index the index of the queried point (starting at 0)
     * @return the latitude in deg*10^7
     */
	public int getPointLat(int index) {
		return points.get(index * 2);
	}

    /**
     * Gets the longitude of a point
     *
     * @param index the index of the queried point (starting at 0)
     * @return the longitude in deg*10^7
     */
	public int getPointLon(int index) {
		return points.get(index * 2 + 1);
	}

    /**
     * Sets the latitude of a point
     *
     * @param index the index of the point to change
     * @param lat the latitude to set in deg*10^7
     */
	public void setPointLat(int index, int lat) {
		points.set(index * 2, lat);
	}

    /**
     * Sets the longitude of a point
     *
     * @param index the index of the point to change
     * @param lon the longitude to set in deg*10^7
     */
	public void setPointLon(int index, int lon) {
		points.set(index * 2 + 1, lon);
	}

    /**
     * Set the distance of this way, that is the length in meters
     *
     * @param dist the length of the way in meters
     */
    public void setDistance(int dist){
        distance = dist;
    }

    /**
     * Set the time it takes to drive the way
     *
     * @param time time to travel in seconds
     */
    public void setTravelTime(double time){
        travelTime = time;
    }

    /**
     * Gets the distance traveled on this way
     * @return the distance in meters
     */
    public int getDistance(){
        return  distance;
    }

    /**
     * Gets the time traveled on this way
     *
     * @return time traveled in seconds
     */
    public double getTravelTime(){
        return travelTime;
    }

    /**
     * Gets the size (number of stored points) of this way
     *
     * @return the number of stored points
     */
	public int size() {
		return points.size() / 2;
	}

}
