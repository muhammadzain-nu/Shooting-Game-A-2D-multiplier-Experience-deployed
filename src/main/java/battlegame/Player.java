package battlegame;

/**
 * Player — Phase 5: added playerId to distinguish local vs remote.
 */
public class Player {

    public static final double RADIUS  = 18;
    public static final int    MAX_HP  = 100;
    private static final double SPEED  = 220;

    public final int    playerId; // 1 = local player, 2 = remote player
    public double x;
    public double y;

    private int     hp;
    private boolean dead = false;

    public Player(double startX, double startY, int playerId) {
        this.x        = startX;
        this.y        = startY;
        this.hp       = MAX_HP;
        this.playerId = playerId;
    }

    public void move(double dx, double dy, double deltaTime,
                     double mapWidth, double mapHeight,
                     java.util.List<Obstacle> obstacles) {
        if (dx != 0 && dy != 0) { dx /= Math.sqrt(2); dy /= Math.sqrt(2); }

        double stepX = dx * SPEED * deltaTime;
        double stepY = dy * SPEED * deltaTime;

        double newX = Math.max(RADIUS, Math.min(mapWidth  - RADIUS, x + stepX));
        if (!collidesWithAny(newX, y, obstacles)) x = newX;

        double newY = Math.max(RADIUS, Math.min(mapHeight - RADIUS, y + stepY));
        if (!collidesWithAny(x, newY, obstacles)) y = newY;
    }

    private boolean collidesWithAny(double cx, double cy, java.util.List<Obstacle> obstacles) {
        for (Obstacle o : obstacles) if (o.overlapsCircle(cx, cy, RADIUS)) return true;
        return false;
    }

    public void takeDamage(int amount) {
        hp = Math.max(0, hp - amount);
        if (hp <= 0) dead = true;
    }

    public boolean isDead() { return dead; }
    public int     getHp()  { return hp;   }
}
