import de.flywieeinairplane.drivingcar.Car;
import org.junit.jupiter.api.Test;
import processing.core.PVector;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LineIntersectTest {

    @Test
    public void intersectTestAllTheSamePoints() {
        PVector line1Start = new PVector(1, 1);
        PVector line1End = new PVector(1, 1);
        PVector line2Start = new PVector(1, 1);
        PVector line2End = new PVector(1, 1);
        boolean b = Car.doIntersect(line1Start, line1End, line2Start, line2End);
        assertTrue(b, "this should intersect");
    }

    @Test
    public void intersectTestSameAxis() {
        PVector line1Start = new PVector(1, 1);
        PVector line1End = new PVector(4, 1);
        PVector line2Start = new PVector(2, 1);
        PVector line2End = new PVector(7, 1);
        boolean b = Car.doIntersect(line1Start, line1End, line2Start, line2End);
        assertTrue(b, "this should intersect");
    }

    @Test
    public void intersectTestNormalIntersection() {
        PVector line1Start = new PVector(1, 1);
        PVector line1End = new PVector(3, 4);
        PVector line2Start = new PVector(1, 3);
        PVector line2End = new PVector(3, 3);
        boolean b = Car.doIntersect(line1Start, line1End, line2Start, line2End);
        assertTrue(b, "this should intersect");
    }

    @Test
    public void intersectTestTeeIntersection() {
        PVector line1Start = new PVector(0, 2);
        PVector line1End = new PVector(2, 2);
        PVector line2Start = new PVector(2, 0);
        PVector line2End = new PVector(2, 4);
        boolean b = Car.doIntersect(line1Start, line1End, line2Start, line2End);
        assertTrue(b, "this should intersect");
    }
    @Test
    public void intersectTestDiagonalTeeIntersection() {
        PVector line1Start = new PVector(1, 4);
        PVector line1End = new PVector(3, 2);
        PVector line2Start = new PVector(0, 0);
        PVector line2End = new PVector(2, 3);
        boolean b = Car.doIntersect(line1Start, line1End, line2Start, line2End);
        assertTrue(b, "this should intersect");
    }

    @Test
    public void intersectTestNoIntersection() {
        PVector line1Start = new PVector(0, 4);
        PVector line1End = new PVector(2, 4);
        PVector line2Start = new PVector(1, 3);
        PVector line2End = new PVector(3, 3);
        boolean b = Car.doIntersect(line1Start, line1End, line2Start, line2End);
        assertFalse(b, "this should not intersect");
    }

    @Test
    public void intersectTestNoIntersectionDiagonal() {
        PVector line1Start = new PVector(0, 3);
        PVector line1End = new PVector(2, 0);
        PVector line2Start = new PVector(1, 4);
        PVector line2End = new PVector(4, 1);
        boolean b = Car.doIntersect(line1Start, line1End, line2Start, line2End);
        assertFalse(b, "this should not intersect");
    }
}
