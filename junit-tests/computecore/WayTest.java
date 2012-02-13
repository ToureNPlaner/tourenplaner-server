package computecore;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class WayTest {

    @Test
    public final void test_addPoint_getPointLat_getPointLon() {
        Way p = new Way();
        p.addPoint(1,2);
        p.addPoint(2,3);
        p.addPoint(4,5);
        assertEquals(p.getPointLat(0), 1);
        assertEquals(p.getPointLat(1), 2);
        assertEquals(p.getPointLat(2), 4);
        assertEquals(p.getPointLon(0), 2);
        assertEquals(p.getPointLon(1), 3);
        assertEquals(p.getPointLon(2), 5);
    }

    @Test
    public final void testsetPointLat() {
        Way p = new Way();
        p.addPoint(1, 2);
        p.addPoint(2, 3);
        p.addPoint(4, 5);
        p.setPointLat(1, 6);
        assertEquals(p.getPointLat(1), 6);
        p.setPointLat(2, 7);
        p.setPointLat(0, 4);
        assertEquals(p.getPointLat(2), 7);
        assertEquals(p.getPointLat(0), 4);
    }

    @Test
    public final void testsetPointLon() {
        Way p = new Way();
        p.addPoint(1, 2);
        p.addPoint(2, 3);
        p.addPoint(4, 5);
        p.setPointLon(1, 6);
        assertEquals(p.getPointLon(1), 6);
        p.setPointLon(2, 7);
        p.setPointLon(0, 4);
        assertEquals(p.getPointLon(2), 7);
        assertEquals(p.getPointLon(0), 4);
    }
    
    @Test
    public final void testsize() {
        Way p = new Way();
        p.addPoint(1, 2);
        assertEquals(p.size(), 1);
        p.addPoint(2, 3);
        p.addPoint(4, 5);
        assertEquals(p.size(), 3);
    }
}
