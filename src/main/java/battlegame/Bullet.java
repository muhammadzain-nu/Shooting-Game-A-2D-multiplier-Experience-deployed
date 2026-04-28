package battlegame;

import java.util.List;

/**
 * Bullet — A single projectile fired by the player.
 * Responsibility: Travel in a fixed direction; report when spent or blocked.
 *
 * Phase 3: isUsed() now also returns true when the bullet is inside an obstacle.
 */
public class Bullet {

    public static final double RADIUS = 5;
    private static final double SPEED = 500;

    public double x;
    public double y;

    private final double dx;
    private final double dy;

    // Flagged true the moment this bullet hits an enemy or an obstacle
    private boolean used = false;

    public Bullet(double startX, double startY, double targetX, double targetY) {
        this.x = startX;
        this.y = startY;

        double angle = Math.atan2(targetY - startY, targetX - startX);
        this.dx = Math.cos(angle) * SPEED;
        this.dy = Math.sin(angle) * SPEED;
    }

    public void update(double deltaTime) {
        x += dx * deltaTime;
        y += dy * deltaTime;
    }

    /** Called by GameEngine the moment a hit is detected. */
    public void markUsed() {
        used = true;
    }

    /**
     * Returns true when this bullet should be removed:
     *   - Already marked used (enemy hit)
     *   - Off-screen
     *   - Centre point is inside any obstacle (obstacle hit)
     */
    public boolean isUsed(double mapWidth, double mapHeight, List<Obstacle> obstacles) {
        if (used) return true;
        if (x < -RADIUS || x > mapWidth + RADIUS || y < -RADIUS || y > mapHeight + RADIUS) return true;

        // Bullet disappears as soon as its centre enters a rectangle
        for (Obstacle o : obstacles) {
            if (o.contains(x, y)) return true;
        }
        return false;
    }
}
