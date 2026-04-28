package battlegame;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * GameEngine — The heart of the game.
 *
 * Phase 5 additions:
 *   - Accepts a nullable NetworkManager (null = single-player)
 *   - Sends local state each frame when connected
 *   - Renders remote player (orange) and remote bullets (orange-red)
 *   - Remote bullets damage local player
 *   - Opponent death or disconnect ends the game on this screen
 */
public class GameEngine {

    // ── Canvas ───────────────────────────────────────────────────────────────
    private final Canvas          canvas;
    private final GraphicsContext gc;
    private final double          width;
    private final double          height;

    // ── Input ────────────────────────────────────────────────────────────────
    private final Set<KeyCode> keysPressed = new HashSet<>();
    private double mouseX, mouseY;

    // ── Game objects ─────────────────────────────────────────────────────────
    private Player         player;
    private List<Bullet>   bullets   = new ArrayList<>();
    private List<Enemy>    enemies   = new ArrayList<>();
    private final List<Obstacle> obstacles = new ArrayList<>();

    // ── Networking (null in single-player mode) ───────────────────────────────
    private final NetworkManager net;
    private boolean disconnectHandled = false;

    // ── HUD ──────────────────────────────────────────────────────────────────
    private final HUD hud = new HUD();

    // ── Timing / FPS ─────────────────────────────────────────────────────────
    private long lastFrameTime = -1;
    private long fpsLastTime   = -1;
    private int  fpsCount      = 0;
    private int  fpsDisplay    = 0;

    // ── Enemy spawning ────────────────────────────────────────────────────────
    private static final double BASE_SPAWN_INTERVAL = 3.0;
    private double spawnInterval = BASE_SPAWN_INTERVAL;
    private double spawnTimer    = 0;

    // ── Score / wave ──────────────────────────────────────────────────────────
    private int score         = 0;
    private int wave          = 1;
    private int lastWaveScore = 0;

    // ── Game state ────────────────────────────────────────────────────────────
    private GameState      gameState   = GameState.PLAYING;
    private boolean        stopPending = false;
    private AnimationTimer loop;

    private static final int  GRID_SIZE           = 50;
    private static final int  REMOTE_BULLET_RADIUS = 5;
    private static final int  REMOTE_BULLET_DAMAGE = 10;

    // ── Constructor ───────────────────────────────────────────────────────────

    public GameEngine(Canvas canvas, Scene scene,
                      double width, double height,
                      NetworkManager net) {
        this.canvas = canvas;
        this.gc     = canvas.getGraphicsContext2D();
        this.width  = width;
        this.height = height;
        this.net    = net;

        buildObstacles();
        this.player = new Player(width / 2, height / 2, 1);
        setupInput(scene);
    }

    // ── Obstacle layout ───────────────────────────────────────────────────────

    private void buildObstacles() {
        obstacles.add(new Obstacle( 80,  80, 120,  20));
        obstacles.add(new Obstacle( 80,  80,  20, 120));
        obstacles.add(new Obstacle(600,  80, 120,  20));
        obstacles.add(new Obstacle(680,  80,  20, 120));
        obstacles.add(new Obstacle( 80, 450, 120,  20));
        obstacles.add(new Obstacle( 80, 350,  20, 120));
        obstacles.add(new Obstacle(300, 180,  20, 140));
        obstacles.add(new Obstacle(480, 280, 140,  20));
        obstacles.add(new Obstacle(600, 420, 120,  20));
        obstacles.add(new Obstacle(680, 330,  20, 110));
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    private void setupInput(Scene scene) {
        scene.setOnKeyPressed(e -> {
            keysPressed.add(e.getCode());
            if (e.getCode() == KeyCode.ESCAPE) {
                if      (gameState == GameState.PLAYING) gameState = GameState.PAUSED;
                else if (gameState == GameState.PAUSED)  gameState = GameState.PLAYING;
            }
            if (e.getCode() == KeyCode.R &&
                    (gameState == GameState.GAME_OVER || gameState == GameState.PAUSED)) {
                reset();
            }
        });
        scene.setOnKeyReleased(e -> keysPressed.remove(e.getCode()));
        scene.setOnMouseMoved  (e -> { mouseX = e.getX(); mouseY = e.getY(); });
        scene.setOnMouseDragged(e -> { mouseX = e.getX(); mouseY = e.getY(); });
        scene.setOnMouseClicked(e -> {
            if (gameState == GameState.PLAYING)
                bullets.add(new Bullet(player.x, player.y, mouseX, mouseY));
        });
    }

    // ── Reset ─────────────────────────────────────────────────────────────────

    private void reset() {
        player        = new Player(width / 2, height / 2, 1);
        bullets       = new ArrayList<>();
        enemies       = new ArrayList<>();
        score         = 0;
        wave          = 1;
        lastWaveScore = 0;
        spawnInterval = BASE_SPAWN_INTERVAL;
        spawnTimer    = 0;
        gameState     = GameState.PLAYING;
        stopPending   = false;
        disconnectHandled = false;
    }

    // ── Game loop ─────────────────────────────────────────────────────────────

    public void start() {
        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastFrameTime < 0) {
                    lastFrameTime = now;
                    fpsLastTime   = now;
                    return;
                }
                double deltaTime = Math.min((now - lastFrameTime) / 1_000_000_000.0, 0.05);
                lastFrameTime = now;

                // FPS counter
                fpsCount++;
                if (now - fpsLastTime >= 1_000_000_000L) {
                    fpsDisplay = fpsCount;
                    fpsCount   = 0;
                    fpsLastTime = now;
                }

                try {
                    if (gameState == GameState.PLAYING) update(deltaTime);
                    render();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (stopPending) { loop.stop(); stopPending = false; }
            }
        };
        loop.start();
    }

    // ── Update ────────────────────────────────────────────────────────────────

    private void update(double deltaTime) {

        // 1. Player movement
        double dx = 0, dy = 0;
        if (keysPressed.contains(KeyCode.W) || keysPressed.contains(KeyCode.UP))    dy -= 1;
        if (keysPressed.contains(KeyCode.S) || keysPressed.contains(KeyCode.DOWN))  dy += 1;
        if (keysPressed.contains(KeyCode.A) || keysPressed.contains(KeyCode.LEFT))  dx -= 1;
        if (keysPressed.contains(KeyCode.D) || keysPressed.contains(KeyCode.RIGHT)) dx += 1;
        player.move(dx, dy, deltaTime, width, height, obstacles);

        // 2. Bullet movement
        for (Bullet b : bullets) b.update(deltaTime);

        // 3. Enemy spawn
        spawnTimer += deltaTime;
        if (spawnTimer >= spawnInterval) {
            enemies.add(new Enemy(width, height));
            spawnTimer = 0;
        }

        // 4. Enemy AI
        for (Enemy e : enemies)
            e.moveTowardPlayer(player.x, player.y, deltaTime, width, height, obstacles);

        // 5. Bullet vs Enemy
        for (int bi = 0; bi < bullets.size(); bi++) {
            Bullet b = bullets.get(bi);
            if (b.isUsed(width, height, obstacles)) continue;
            for (int ei = 0; ei < enemies.size(); ei++) {
                Enemy e = enemies.get(ei);
                if (e.isDead()) continue;
                if (Math.hypot(b.x - e.x, b.y - e.y) < Bullet.RADIUS + Enemy.RADIUS) {
                    b.markUsed();
                    e.takeDamage();
                    if (e.isDead()) { score++; checkWave(); }
                    break;
                }
            }
        }

        // 6. Enemy vs Player
        for (int ei = 0; ei < enemies.size(); ei++) {
            Enemy e = enemies.get(ei);
            if (e.isDead()) continue;
            if (Math.hypot(e.x - player.x, e.y - player.y) < Enemy.RADIUS + Player.RADIUS) {
                player.takeDamage(20);
                e.destroy();
                System.out.println("Player hit by enemy! HP=" + player.getHp());
            }
        }

        // 7. Network: send local state + check remote events
        if (net != null) {
            net.sendState(player, bullets);
            handleNetworkEvents();
        }

        // 8. Cleanup
        final double w = width, h = height;
        final List<Obstacle> obs = obstacles;
        bullets.removeIf(b -> b.isUsed(w, h, obs));
        enemies.removeIf(Enemy::isDead);

        // 9. Local death
        if (player.isDead() && gameState == GameState.PLAYING) {
            gameState = GameState.GAME_OVER;
            if (net != null) net.sendGameOver();
            System.out.println("GAME OVER — Score=" + score);
        }
    }

    /**
     * Check remote bullets vs local player, opponent death, and disconnects.
     * Called each frame when networking is active.
     */
    private void handleNetworkEvents() {
        // Remote bullets hit local player
        for (double[] rb : net.getRemoteBullets()) {
            double dist = Math.hypot(rb[0] - player.x, rb[1] - player.y);
            if (dist < REMOTE_BULLET_RADIUS + Player.RADIUS) {
                player.takeDamage(REMOTE_BULLET_DAMAGE);
                System.out.println("Hit by remote bullet! HP=" + player.getHp());
                // Note: we can't remove the remote bullet here (owned by opponent)
                // Damage is applied once per proximity check — acceptable for LAN latency
            }
        }

        // Opponent died → show game over on our screen too
        if (net.opponentDied && gameState == GameState.PLAYING) {
            gameState = GameState.GAME_OVER;
            System.out.println("Opponent died — GAME OVER.");
        }

        // Opponent disconnected
        if (!net.isConnected() && !disconnectHandled && gameState == GameState.PLAYING) {
            disconnectHandled = true;
            gameState = GameState.GAME_OVER;
            System.out.println("Opponent disconnected.");
        }
    }

    private void checkWave() {
        if (score > 0 && score % 10 == 0 && score != lastWaveScore) {
            wave++;
            lastWaveScore = score;
            spawnInterval = Math.max(0.5, spawnInterval * 0.9);
            System.out.println("Wave " + wave + " — spawn=" + String.format("%.2f", spawnInterval) + "s");
        }
    }

    // ── Render ────────────────────────────────────────────────────────────────

    private void render() {
        // Background
        gc.setFill(Color.rgb(15, 15, 25));
        gc.fillRect(0, 0, width, height);
        drawGrid();

        // Obstacles
        for (Obstacle o : obstacles) {
            gc.setFill(Color.rgb(55, 58, 70));
            gc.fillRect(o.x, o.y, o.w, o.h);
            gc.setStroke(Color.rgb(90, 95, 115));
            gc.setLineWidth(1.5);
            gc.strokeRect(o.x, o.y, o.w, o.h);
            gc.setStroke(Color.rgb(110, 115, 135, 0.6));
            gc.setLineWidth(1);
            gc.strokeLine(o.x+2, o.y+2, o.x+o.w-2, o.y+2);
            gc.strokeLine(o.x+2, o.y+2, o.x+2, o.y+o.h-2);
        }

        // Remote player and remote bullets (drawn before local so local is on top)
        if (net != null && net.connected && net.remoteX > -9000) {
            double rx = net.remoteX, ry = net.remoteY;

            // Remote bullets (orange-red)
            gc.setFill(Color.ORANGERED);
            for (double[] rb : net.getRemoteBullets()) {
                gc.fillOval(rb[0] - REMOTE_BULLET_RADIUS, rb[1] - REMOTE_BULLET_RADIUS,
                            REMOTE_BULLET_RADIUS * 2, REMOTE_BULLET_RADIUS * 2);
            }

            // Remote player circle (orange)
            gc.setFill(Color.ORANGE);
            gc.fillOval(rx - Player.RADIUS, ry - Player.RADIUS,
                        Player.RADIUS * 2, Player.RADIUS * 2);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(rx - Player.RADIUS, ry - Player.RADIUS,
                          Player.RADIUS * 2, Player.RADIUS * 2);

            // Opponent HP label above their circle
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
            gc.fillText("P2 HP:" + net.remoteHp, rx - 26, ry - Player.RADIUS - 5);
        }

        // Disconnected badge (top-centre, only in multiplayer)
        if (net != null && !net.connected && gameState != GameState.GAME_OVER) {
            gc.setFill(Color.rgb(220, 80, 80, 0.9));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
            gc.fillText("⚠ Opponent disconnected", width / 2 - 105, 18);
        }

        // Local bullets (yellow)
        gc.setFill(Color.YELLOW);
        for (Bullet b : bullets)
            gc.fillOval(b.x - Bullet.RADIUS, b.y - Bullet.RADIUS,
                        Bullet.RADIUS * 2, Bullet.RADIUS * 2);

        // Enemies (red)
        for (Enemy e : enemies) {
            gc.setFill(Color.rgb(220, 50, 50));
            gc.fillOval(e.x - Enemy.RADIUS, e.y - Enemy.RADIUS,
                        Enemy.RADIUS * 2, Enemy.RADIUS * 2);
            gc.setStroke(Color.WHITE); gc.setLineWidth(1.5);
            gc.strokeOval(e.x - Enemy.RADIUS, e.y - Enemy.RADIUS,
                          Enemy.RADIUS * 2, Enemy.RADIUS * 2);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
            gc.fillText(String.valueOf(e.getHp()), e.x - 4, e.y - Enemy.RADIUS - 4);
        }

        // Local player (blue)
        gc.setFill(Color.DODGERBLUE);
        gc.fillOval(player.x - Player.RADIUS, player.y - Player.RADIUS,
                    Player.RADIUS * 2, Player.RADIUS * 2);
        gc.setStroke(Color.WHITE); gc.setLineWidth(2);
        gc.strokeOval(player.x - Player.RADIUS, player.y - Player.RADIUS,
                      Player.RADIUS * 2, Player.RADIUS * 2);

        // Controls hint
        if (gameState == GameState.PLAYING) {
            gc.setFill(Color.rgb(160, 160, 160, 0.45));
            gc.setFont(Font.font("Monospaced", 12));
            gc.fillText("WASD — Move   |   Click — Shoot   |   ESC — Pause", 10, height - 12);
        }

        // HUD (HP bar, score, wave, FPS, overlays)
        hud.draw(gc, player.getHp(), Player.MAX_HP, score, wave, gameState, fpsDisplay, width, height);
    }

    private void drawGrid() {
        gc.setStroke(Color.rgb(40, 40, 60));
        gc.setLineWidth(0.5);
        for (double x = 0; x < width;  x += GRID_SIZE) gc.strokeLine(x, 0, x, height);
        for (double y = 0; y < height; y += GRID_SIZE) gc.strokeLine(0, y, width, y);
    }
}
