package battlegame;

/**
 * Obstacle — An axis-aligned solid rectangle on the map.
 * Responsibility: Store geometry and provide circle-vs-AABB collision helpers.
 *
 * Uses AABB (Axis-Aligned Bounding Box) vs circle collision:
 *   Find the closest point on the rectangle to the circle centre,
 *   then compare the distance to the circle's radius.
 */
public class Obstacle {

    // Rectangle geometry in world-space pixels
    public final double x;  // left edge
    public final double y;  // top edge
    public final double w;  // width
    public final double h;  // height

    public Obstacle(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    /**
     * Returns true if a circle (cx, cy, radius) overlaps this rectangle.
     *
     * Algorithm: clamp the circle centre to the rectangle bounds to find
     * the nearest point on the rect, then check if that point is inside
     * the circle.
     */
    public boolean overlapsCircle(double cx, double cy, double radius) {
        // Closest point on the rect to the circle centre
        double closestX = Math.max(x, Math.min(cx, x + w));
        double closestY = Math.max(y, Math.min(cy, y + h));

        double dist = Math.hypot(cx - closestX, cy - closestY);
        return dist < radius;
    }

    /**
     * Returns true if a point (px, py) lies inside this rectangle.
     * Used for fast bullet-inside-obstacle checks.
     */
    public boolean contains(double px, double py) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }
}
