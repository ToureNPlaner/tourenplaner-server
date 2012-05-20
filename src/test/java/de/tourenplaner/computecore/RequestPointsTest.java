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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class RequestPointsTest {

    @Test
    public void test_addPoint_getPointLat_getPointLon() throws Exception {
        RequestPoints r = new RequestPoints();
        r.addPoint(1,2);
        r.addPoint(2, 3);
        r.addPoint(4, 5);
        assertEquals(r.getPointLat(0), 1);
        assertEquals(r.getPointLat(1), 2);
        assertEquals(r.getPointLat(2), 4);
        assertEquals(r.getPointLon(0), 2);
        assertEquals(r.getPointLon(1), 3);
        assertEquals(r.getPointLon(2), 5);
    }

    @Test
    public void test_addPoint_setPointLat_setPointLon_getPointLat_getPointLon() throws Exception {
        RequestPoints r = new RequestPoints();
        r.addPoint(1, 2);
        r.addPoint(2, 3);
        r.addPoint(4, 5);
        assertEquals(r.getPointLat(0), 1);
        assertEquals(r.getPointLat(1), 2);
        assertEquals(r.getPointLat(2), 4);
        assertEquals(r.getPointLon(0), 2);
        assertEquals(r.getPointLon(1), 3);
        assertEquals(r.getPointLon(2), 5);

        r.setPointLat(0,4);
        r.setPointLat(1,6);
        r.setPointLat(2,9);
        r.setPointLon(0, 5);
        r.setPointLon(1, 7);
        r.setPointLon(2, 10);

        assertEquals(r.getPointLat(0), 4);
        assertEquals(r.getPointLat(1), 6);
        assertEquals(r.getPointLat(2), 9);
        assertEquals(r.getPointLon(0), 5);
        assertEquals(r.getPointLon(1), 7);
        assertEquals(r.getPointLon(2), 10);
     }


    @Test
    public void test_addPoint_getPointLat_getPointLon_getConstraint() throws Exception {
        RequestPoints r = new RequestPoints();
        Map<String, Object> constr1 = new HashMap< String, Object>();
        constr1.put("constr1-1", 12);
        constr1.put("constr1-2", "string constraint wtf");
        Map<String, Object> constr2 = new HashMap<String, Object>();
        constr1.put("constr2-1", Boolean.TRUE);
        constr1.put("constr2-2", new Object());
        Map<String, Object> constr3 = new HashMap<String, Object>();
        constr1.put("constr3-1", 3.141);

        r.addPoint(1, 2, constr1);
        r.addPoint(2, 3, constr1);
        r.addPoint(4, 5, constr1);

        assertEquals(r.getPointLat(0), 1);
        assertEquals(r.getPointLat(1), 2);
        assertEquals(r.getPointLat(2), 4);
        assertEquals(r.getPointLon(0), 2);
        assertEquals(r.getPointLon(1), 3);
        assertEquals(r.getPointLon(2), 5);
        
        assertTrue(r.getConstraint(0, "constr1-1").equals(12));
        assertTrue(r.getConstraint(0, "constr1-2").equals("string constraint wtf"));

        assertTrue(r.getConstraint(1, "constr2-1").equals(true));
        assertTrue(r.getConstraint(1, "constr2-2") instanceof Object);

        assertTrue(r.getConstraint(2, "constr3-1").equals(3.141));
    }

    @Test
    public void testSize() throws Exception {
        RequestPoints r = new RequestPoints();
        assertEquals(r.size(), 0);
        r.addPoint(1, 2);
        assertEquals(r.size(), 1);
        r.addPoint(2, 3);
        assertEquals(r.size(), 2);
        r.addPoint(4, 5);
        assertEquals(r.size(), 3);
    }

   @Test
    public void testSetIdsFromGraph() throws Exception {
       //GraphRep g = (new TestGraphReader()).readTestGraph();
        //TODO
    }

    @Test
    public void test_setPointId_getPointId() throws Exception {
        RequestPoints r = new RequestPoints();
        r.addPoint(1, 2);
        r.addPoint(2, 3);
        r.addPoint(4, 5);
        r.setPointId(0, 1);
        r.setPointId(1, 12);
        r.setPointId(2, 100);
        assertEquals(r.getPointId(0), 1);
        assertEquals(r.getPointId(1), 12);
        assertEquals(r.getPointId(2), 100);
    }

    @Test
    public void testGetConstraints() throws Exception {
        RequestPoints r = new RequestPoints();
        Map<String, Object> constr1 = new HashMap<String, Object>();
        constr1.put("constr1-1", 12);
        constr1.put("constr1-2", "string constraint wtf");
        Map<String, Object> constr2 = new HashMap<String, Object>();
        constr1.put("constr2-1", Boolean.TRUE);
        constr1.put("constr2-2", new Object());
        Map<String, Object> constr3 = new HashMap<String, Object>();
        constr1.put("constr3-1", 3.141);

        r.addPoint(1, 2, constr1);
        r.addPoint(2, 3, constr1);
        r.addPoint(4, 5, constr1);

        Map<String, Object> test = r.getConstraints(0);
        assertTrue(test.get("constr1-1").equals(12));
        assertTrue(test.get("constr1-2").equals("string constraint wtf"));

        test = r.getConstraints(1);
        assertTrue(test.get("constr2-1").equals(true));
        assertTrue(test.get("constr2-2") instanceof Object);

        test = r.getConstraints(2);
        assertTrue(test.get("constr3-1").equals(3.141));    }

}
