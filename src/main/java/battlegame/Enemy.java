package battlegame;

import java.util.List;
import java.util.Random;

/**
 * Enemy — A single hostile unit that chases the player.
 * Responsibility: Store position/health and move toward the player each frame.
 *
 * Phase 3: Movement uses the same wall-slide trick as the player so enemies
 *          don't clip through or freeze against obstacles.
 */
public class Enemy {

    public static final double RADIUS = 16;
    private static final double SPEED = 100;
    private static final Random RNG   = new Random();

    public double x;
    public double y;

    private int     hp;
    private boolean dead = false;

    public Enemy(double mapWidth, double mapHeight) {
        this.hp = 1 + RNG.nextInt(3); // 1–3 HP

        // Spawn at a random point on one of the four edges
        int edge = RNG.nextInt(4);
        switch (edge) {
            case 0 -> { x = RNG.nextDouble() * mapWidth;  y = -RADIUS; }
            case 1 -> { x = RNG.nextDouble() * mapWidth;  y = mapHeight + RADIUS; }
            case 2 -> { x = -RADIUS;                      y = RNG.nextDouble() * mapHeight; }
            case 3 -> { x = mapWidth + RADIUS;             y = RNG.nextDouble() * mapHeight; }
        }
    }

    /**
     * Move toward the player using wall-sliding obstacle avoidance.
     *
     * Same axis-split technique as Player.move():
     *   Try X step → accept if no collision.
     *   Try Y step → accept if no collision.
     * This makes enemies slide around obstacle corners without pathfinding.
     *
     * @param playerX   Player's X
     * @param playerY   Player's Y
     * @param deltaTime Seconds since last frame
     * @param mapWidth  Canvas width  for boundary clamping
     * @param mapHeight Canvas height for boundary clamping
     * @param obstacles Static obstacle list
     */
    public void moveTowardPlayer(double playerX, double playerY, double deltaTime,
                                 double mapWidth, double mapHeight,
                                 List<Obstacle> obstacles) {
        double dx = playerX - x;
        double dy = playerY - y;
        double dist = Math.hypot(dx, dy);

        if (dist == 0) return; // already on the player — nothing to do

        // Normalise direction then scale by speed
        double stepX = (dx / dist) * SPEED * deltaTime;
        double stepY = (dy / dist) * SPEED * deltaTime;

        // ── Try X movement ───────────────────────────────────────────────────
        double newX = Math.max(RADIUS, Math.min(mapWidth  - RADIUS, x + stepX));
        if (!collidesWithAny(newX, y, obstacles)) {
            x = newX;
        }

        // ── Try Y movement ───────────────────────────────────────────────────
        double newY = Math.max(RADIUS, Math.min(mapHeight - RADIUS, y + stepY));
        if (!collidesWithAny(x, newY, obstacles)) {
            y = newY;
        }
    }

    /** Returns true if the enemy circle at (cx, cy) overlaps any obstacle. */
    private boolean collidesWithAny(double cx, double cy, List<Obstacle> obstacles) {
        for (Obstacle o : obstacles) {
            if (o.overlapsCircle(cx, cy, RADIUS)) return true;
        }
        return false;
    }

    public void takeDamage() {
        hp--;
        if (hp <= 0) dead = true;
    }

    public void destroy() { dead = true; }

    public boolean isDead() { return dead;  }
    public int     getHp()  { return hp;    }
}
