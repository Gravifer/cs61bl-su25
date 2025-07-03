import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
// import org.junit.After;
// import org.junit.rules.ExpectedException;
import static com.google.common.truth.Truth.*;
public abstract class TriangleTest {

    /** For autograding purposes; do not change this line. */
    abstract Triangle getNewTriangle();

    /* ***** TESTS ***** */

    // FIXED: Add additional tests for Triangle.java here that pass on a
    //  correct Triangle implementation and fail on buggy Triangle implementations.

    private Triangle t;
    static record DataTriplet<T>(T a, T b, T c) {}
    static record DataTestCase<T, E>(DataTriplet<T> t, E e) {}
    static record SideTestCase(int a, int b, int c, boolean isValid) {}
    static record PointTestCase(int x1, int y1, int x2, int y2, int x3, int y3, boolean isValid) {}
    static record TypeTestCase(int a, int b, int c, String type) {}

    @Before
    public void setUp() {
        t = getNewTriangle();
    }

    @Test
    public void testSidesFormTriangle() {
        // DONE: stub for first test
        // // Triangle t = getNewTriangle();
        // remember that you'll have to call on Triangle methods like
        // t.functionName(arguments), where t is a Triangle object
        int[][] testCases = {
            {0, 0, 0, 0}, // invalid triangle (degenerate case)
            {0, 0, 1, 0}, // invalid triangle (degenerate case)
            {0, 1, 1, 0}, // invalid triangle (degenerate case)
            {1, 1, 1, 1}, // valid triangle (equilateral)
            {3, 4, 5, 1}, // valid triangle (Pythagorean triple)
            {2, 2, 3, 1}, // valid triangle (isosceles)
            {1, 2, 3, 0}, // invalid triangle (1 + 2 <= 3)
            {5, 12, 13, 1}, // valid triangle (Pythagorean triple)
            {8, 15, 17, 1}, // valid triangle (Pythagorean triple)
            {10, 6, 8, 1}, // valid triangle
            {7, 24, 25, 1}, // valid triangle (Pythagorean triple)
            {2, 2, 4, 0} // invalid triangle (2 + 2 <= 4)
        };
        for (int[] testCase : testCases) {
            int a = testCase[0];
            int b = testCase[1];
            int c = testCase[2];
            boolean expected = testCase[3] != 0;

            int[][] perms = {
                {a, b, c},
                {a, c, b},
                {b, a, c},
                {b, c, a},
                {c, a, b},
                {c, b, a}
            };

            for (int[] perm : perms) {
                boolean result = t.sidesFormTriangle(perm[0], perm[1], perm[2]);
                assertWithMessage("sidesFormTriangle(%d, %d, %d) should be %s"
                        .formatted(perm[0], perm[1], perm[2], expected))
                        .that(result).isEqualTo(expected);
            }
        }
    }

    @Test
    public void testPointsFormTriangle() {
        int[][] testCases = {
            {0, 0, 0, 0, 0, 0, 0}, // invalid triangle (degenerate case)
            {0, 0, 0, 0, 1, 1, 0}, // invalid triangle (degenerate case)
            {0, 0, 1, 1, 1, 1, 0}, // invalid triangle (degenerate case)
            {0, 0, 1, 1, 2, 2, 0}, // invalid triangle (collinear points)
            {0, 0, 1, 1, 2, 3, 1}, // valid triangle
            {1, 2, 3, 4, 5, 6, 0}, // invalid triangle (collinear points))
            {1, 1, 2, 2, 3, 3, 0}, // invalid triangle (collinear points)
            {3, 4, 5, -5, -4, -3, 1} // valid triangle
        };
        for (int[] testCase : testCases) {
            int x1 = testCase[0];
            int y1 = testCase[1];
            int x2 = testCase[2];
            int y2 = testCase[3];
            int x3 = testCase[4];
            int y3 = testCase[5];
            boolean expected = testCase[6] != 0;

            boolean result = t.pointsFormTriangle(x1, y1, x2, y2, x3, y3);
            assertWithMessage("pointsFormTriangle(%d,%d,%d,%d,%d,%d) should be %s"
                    .formatted(x1,y1,x2,y2,x3,y3, expected))
                    .that(result).isEqualTo(expected);
        }
    }

    @Test
    public void testTriangleType() {
        String[] triangleTypes = {"Equilateral", "Isosceles", "Scalene"};
        int[][] testCases = {
            {1, 1, 1, 0}, // Equilateral
            {2, 2, 3, 1}, // Isosceles
            {3, 4, 5, 2}, // Scalene
            {5, 5, 8, 1}, // Isosceles
            {7, 24, 25, 2}, // Scalene
            {10, 10, 10, 0}, // Equilateral
            {6, 8, 10, 2}, // Scalene
            {1, 1, 2, 1} // Isosceles (degenerate case)
        };
        for (int[] testCase : testCases) {
            int a = testCase[0];
            int b = testCase[1];
            int c = testCase[2];
            String expected = triangleTypes[testCase[3]];

            String result = t.triangleType(a, b, c);
            assertWithMessage("triangleType(%d,%d,%d) should be %s"
                    .formatted(a,b,c, expected))
                    .that(result).isEqualTo(expected);
        }
    }

    /** A concrete subclass for testing. You may include it at the bottom of this file. */
    public static class DefaultImplTest extends TriangleTest {
        @Override
        Triangle getNewTriangle() {
            return new Triangle() {
                @Override
                public boolean sidesFormTriangle(int a, int b, int c) {
                    return a + b > c && a + c > b && b + c > a;
                }

                @Override
                public boolean pointsFormTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
                    return (x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) != 0;
                }

                @Override
                public String triangleType(int a, int b, int c) {
                    if (a == b && b == c) return "Equilateral";
                    if (a == b || b == c || a == c) return "Isosceles";
                    return "Scalene";
                }

                @Override
                public int squaredHypotenuse(int a, int b) {
                    return a * a + b * b;
                }
            };
        }
    }
}
