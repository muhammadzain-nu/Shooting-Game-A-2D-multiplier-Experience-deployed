# 🎮 Java 2D Battle Game — AI Prompt Guide (All Phases)

> **How to use this file:**
> This is a structured prompt guide for building a Java 2D battle game step by step using any AI assistant (Claude, ChatGPT, Gemini, etc.).
> Each phase is a self-contained prompt. Copy → Paste → Build → Repeat.

---

## 🧭 HOW TO USE THIS GUIDE WITH AN AI

### Step-by-Step Instructions

1. **Start a fresh conversation** with your AI of choice (Claude, ChatGPT, etc.)
2. **Copy the "Master System Prompt"** below and paste it first — this sets the AI's behavior for the whole session
3. **Then paste Phase 1 prompt** and say: `"Build Phase 1"`
4. **Wait for complete output** — don't interrupt mid-response
5. **Copy the generated code** into your IDE and run it
6. **Fix any errors** by pasting the error back to the AI and saying: `"Fix this error: [paste error]"`
7. **Only move to the next phase** after the current one runs without bugs
8. **Paste the next phase prompt** and say: `"Build Phase 2"`
9. Repeat until Phase 5

---

### ✅ DO's and ❌ DON'Ts When Prompting

| ✅ DO | ❌ DON'T |
|-------|----------|
| Paste one phase at a time | Paste all phases at once |
| Say "Build Phase N" clearly | Say "continue" without context |
| Paste exact error messages | Describe errors vaguely |
| Ask for one fix at a time | Ask for multiple changes at once |
| Say "don't change other files" when fixing a bug | Let the AI rewrite everything |
| Confirm it works before next phase | Skip testing |

---

### 💬 Useful Prompts to Keep Handy

```
"Build Phase 1"
"This error occurred: [paste error]. Fix only the relevant file."
"The player movement feels laggy. Fix only Player.java"
"Phase 1 is working. Now build Phase 2."
"Do not change any other files. Only fix GameEngine.java"
"Explain what delta time does in this code"
"Give me the full file again, not just the changed part"
```

---

## 🧠 MASTER SYSTEM PROMPT
> Paste this FIRST in every new conversation before any phase prompt.

```
You are a senior Java game developer and educator specializing in JavaFX 2D games.
You write clean, beginner-readable code with zero unnecessary complexity.

ABSOLUTE RULES:
- Deliver ONLY the phase I ask for — never output future phases
- Every file must be COMPLETE — no snippets, no "// same as before", no TODOs
- Code must compile and run on the first try
- Use clean OOP: one responsibility per class
- No frameworks, no over-engineering, no premature patterns
- Comment non-obvious logic with clear variable names
- Each phase EXTENDS the previous — never rewrite from scratch
- After each phase, end with: "✅ Phase N complete. Say 'Build Phase N+1' when ready."

PROJECT CONTEXT:
- Language: Java JDK 17+
- Framework: JavaFX with Canvas + GraphicsContext
- Build: Maven
- Game: 2D top-down battle game, single then multiplayer
```

---

## 📦 PHASE 1 — Core Single-Player Engine

> **Goal:** A working window with player movement and shooting.
> **Say to AI:** `"Build Phase 1"`

```
PHASE 1 SPECIFICATION — Core Single-Player Engine

CLASS STRUCTURE:
src/main/java/battlegame/
├── GameApp.java       ← JavaFX entry point (extends Application)
├── GameEngine.java    ← Game loop, input handling, update + render
├── Player.java        ← Position, speed, movement logic
└── Bullet.java        ← Projectile logic

FEATURES TO BUILD:
1. JavaFX window — 800x600, titled "Battle Game – Phase 1"
2. Canvas — all rendering on GraphicsContext
3. Player — blue circle, spawns at center
4. WASD movement — smooth, continuous, delta-time based
5. Diagonal normalization — consistent speed at all angles
6. Mouse-click shooting — bullet fires toward cursor
7. Bullet travel — straight line, removed when off-screen
8. Game loop — AnimationTimer with proper delta time

TECHNICAL REQUIREMENTS:

Input Tracking:
  Set<KeyCode> keysPressed = new HashSet<>();
  scene.setOnKeyPressed(e -> keysPressed.add(e.getCode()));
  scene.setOnKeyReleased(e -> keysPressed.remove(e.getCode()));

Delta Time:
  double deltaTime = (now - lastTime) / 1_000_000_000.0;
  lastTime = now;

Diagonal Normalization:
  if (dx != 0 && dy != 0) { dx /= Math.sqrt(2); dy /= Math.sqrt(2); }

Bullet Direction:
  double angle = Math.atan2(mouseY - player.y, mouseX - player.x);
  double dx = Math.cos(angle) * BULLET_SPEED;
  double dy = Math.sin(angle) * BULLET_SPEED;

Bullet Cleanup:
  bullets.removeIf(b -> b.isOffScreen(WIDTH, HEIGHT));

OUTPUT FORMAT:
1. Full project/folder structure
2. One complete code block per file (no TODOs)
3. How it works in 5 bullets max

OUT OF SCOPE: Enemies, Health, Obstacles, HUD, Networking

DEFINITION OF DONE:
- Window opens without errors
- Player moves smoothly with WASD
- Clicking fires bullet toward cursor
- Bullets disappear off-screen
- No exceptions during normal play
```

---

## 📦 PHASE 2 — Combat System (Enemies + Health + Damage)

> **Goal:** Add enemies, health, damage, and death handling.
> **Say to AI:** `"Phase 1 is working. Now build Phase 2"`

```
PHASE 2 SPECIFICATION — Combat System

NEW / MODIFIED FILES:
├── GameEngine.java    ← add enemy list, collision, health checks
├── Player.java        ← add health, takeDamage(), isDead()
├── Bullet.java        ← add hit flag for removal on enemy contact
└── Enemy.java         ← NEW: position, speed, AI movement, health

FEATURES TO BUILD:
1. Enemy class — red circle, spawns at random screen edge
2. Enemy AI — moves directly toward player each frame
3. Enemy spawning — one enemy every 3 seconds via delta timer
4. Bullet vs Enemy collision — bullet removed, enemy takes damage
5. Enemy vs Player collision — player takes damage, enemy destroyed
6. Player health — starts at 100, printed to console
7. Enemy health — 1 to 3 HP per enemy
8. Player death — game loop stops, "GAME OVER" printed to console
9. Score counter — increments per kill, printed to console

TECHNICAL REQUIREMENTS:

Circle Collision:
  double dist = Math.hypot(a.x - b.x, a.y - b.y);
  boolean colliding = dist < (a.radius + b.radius);

Spawn Timer:
  spawnTimer += deltaTime;
  if (spawnTimer >= SPAWN_INTERVAL) {
      enemies.add(spawnEnemy());
      spawnTimer = 0;
  }

Safe Cleanup:
  bullets.removeIf(Bullet::isUsed);
  enemies.removeIf(Enemy::isDead);

OUT OF SCOPE: Visual health bar, Score on screen, Networking

DEFINITION OF DONE:
- Enemies spawn at screen edges every 3 seconds
- Enemies move toward player
- Bullets reduce enemy HP correctly
- Player loses health on enemy contact
- Game stops when player HP reaches 0
- Score increments on kill (console OK)
```

---

## 📦 PHASE 3 — World (Obstacles + Collision + Boundaries)

> **Goal:** Add solid obstacles, wall sliding, and map boundaries.
> **Say to AI:** `"Phase 2 is working. Now build Phase 3"`

```
PHASE 3 SPECIFICATION — World & Obstacles

NEW / MODIFIED FILES:
├── GameEngine.java    ← add obstacle list, boundary checks
├── Player.java        ← add boundary + obstacle collision response
├── Enemy.java         ← add obstacle sliding
├── Bullet.java        ← destroyed on obstacle hit
└── Obstacle.java      ← NEW: Rectangle data + collision helper

FEATURES TO BUILD:
1. Obstacle class — filled grey rectangles
2. Static layout — 5 to 8 hand-placed obstacles at game start
3. Player vs Obstacle — slide along wall, no clipping
4. Bullet vs Obstacle — bullet removed on contact
5. Enemy vs Obstacle — slide around (no pathfinding needed)
6. Map boundary — nothing leaves 800x600 canvas
7. Player clamping — clamp to [radius, WIDTH-radius]

TECHNICAL REQUIREMENTS:

AABB vs Circle:
  double closestX = Math.max(rect.x, Math.min(circle.x, rect.x + rect.w));
  double closestY = Math.max(rect.y, Math.min(circle.y, rect.y + rect.h));
  double dist = Math.hypot(circle.x - closestX, circle.y - closestY);
  boolean hit = dist < circle.radius;

Wall Slide:
  Try X movement alone, then Y movement alone.
  Accept whichever axes don't cause collision.

Bullet vs Obstacle:
  bullets.removeIf(b -> obstacles.stream().anyMatch(o -> o.contains(b.x, b.y)));

Clamping:
  player.x = Math.clamp(player.x, player.radius, WIDTH - player.radius);
  player.y = Math.clamp(player.y, player.radius, HEIGHT - player.radius);

OUT OF SCOPE: Destructible obstacles, Pathfinding AI, HUD, Networking

DEFINITION OF DONE:
- Obstacles render as solid grey rectangles
- Player cannot walk through obstacles or off screen
- Bullets disappear on obstacle hit
- Enemies slide around obstacles
- Nothing escapes the 800x600 boundary
```

---

## 📦 PHASE 4 — UI & Polish (HUD, Health Bar, Score, Game Over)

> **Goal:** Add a full visual HUD, wave system, and game over screen.
> **Say to AI:** `"Phase 3 is working. Now build Phase 4"`

```
PHASE 4 SPECIFICATION — UI & Polish

NEW / MODIFIED FILES:
├── GameEngine.java    ← add GameState enum usage, wave logic
├── HUD.java           ← NEW: renders all UI elements on Canvas
└── GameState.java     ← NEW: enum (PLAYING, PAUSED, GAME_OVER)

FEATURES TO BUILD:
1. HUD class — draws all overlays using method params (decoupled)
2. Health bar — top-left, green to yellow to red by HP percentage
3. Score display — top-right, white text, live update
4. Wave counter — center-top, "Wave 3" label style
5. Wave system — every 10 kills = new wave, spawn rate +10% faster
6. Game Over screen — dark overlay, final score, "Press R to Restart"
7. Restart mechanic — R key resets all state cleanly
8. Pause — ESC pauses loop, shows "PAUSED" overlay
9. FPS counter — small text bottom-right (optional)

TECHNICAL REQUIREMENTS:

GameState Enum:
  public enum GameState { PLAYING, PAUSED, GAME_OVER }

Health Bar:
  double pct = (double) player.health / player.maxHealth;
  gc.setFill(pct > 0.5 ? Color.GREEN : pct > 0.25 ? Color.YELLOW : Color.RED);
  gc.fillRect(20, 20, 200 * pct, 20);

Wave Progression:
  if (score % 10 == 0 && score > 0) {
      wave++;
      spawnInterval = Math.max(0.5, spawnInterval * 0.9);
  }

Restart Reset:
  void reset() {
      player = new Player(WIDTH / 2, HEIGHT / 2);
      bullets.clear(); enemies.clear();
      score = 0; wave = 1;
      gameState = GameState.PLAYING;
  }

HUD Signature (decoupled):
  public void draw(GraphicsContext gc, int health, int maxHealth,
                   int score, int wave, GameState state) { ... }

OUT OF SCOPE: Animated effects, Sound, Leaderboard, Networking

DEFINITION OF DONE:
- Health bar visible and changes color correctly
- Score updates live on screen
- Wave increments every 10 kills
- Game Over screen shows with final score
- R key restarts the game cleanly
- ESC pauses and resumes
```

---

## 📦 PHASE 5 — Multiplayer (Java Sockets, Two Players, LAN)

> **Goal:** Real-time two-player multiplayer over local network.
> **Say to AI:** `"Phase 4 is working. Now build Phase 5"`

```
PHASE 5 SPECIFICATION — Multiplayer via Java Sockets

NEW / MODIFIED FILES:
├── GameEngine.java        ← handle local + remote player, merge net updates
├── Player.java            ← add playerId field
├── NetworkManager.java    ← NEW: owns socket, reader/writer threads
├── GameServer.java        ← NEW: accepts 1 client, relays state
├── GameClient.java        ← NEW: connects to server, sends/receives
└── GameState.java (dto)   ← repurposed as serializable snapshot

FEATURES TO BUILD:
1. Launch menu — "Host Game" or "Join Game" (simple JavaFX dialog)
2. Server mode — ServerSocket on port 5555, waits for 1 client
3. Client mode — connects to server IP entered by user
4. State sharing — each player sends position + bullets every frame
5. Remote player — shown as different color circle
6. Remote bullets — rendered and checked for local collision
7. Damage sync — remote bullet hits local player → notify server
8. Game over sync — one player dies → both screens show game over
9. Disconnect handling — show "Opponent Disconnected", stop game

TECHNICAL REQUIREMENTS:

Message Protocol (plain text, newline-delimited):
  FORMAT:  PLAYER|x|y|health|b1x,b1y,b1dx,b1dy
  EXAMPLE: PLAYER|400.0|300.0|80|210.5,150.0,3.0,-1.5

Sending (background thread):
  out.println(buildStateString(localPlayer, localBullets));
  out.flush();

Receiving (background thread):
  String line;
  while ((line = in.readLine()) != null) {
      parseAndUpdateRemoteState(line);
  }

Thread Safety:
  volatile double remoteX, remoteY;
  CopyOnWriteArrayList<Bullet> remoteBullets = new CopyOnWriteArrayList<>();

Server = Pure Relay (no game logic on server):
  Read from Player 1 → forward to Player 2, and vice versa.

Connection Flow:
  Host clicks "Host"  →  ServerSocket.accept() blocks
  Client enters IP    →  new Socket(ip, 5555)
  Both connected      →  game starts simultaneously

OUT OF SCOPE: 3+ players, Internet play, Lag compensation, Encryption

DEFINITION OF DONE:
- Host opens and waits for connection
- Client connects via localhost or LAN IP
- Both players see each other in real time
- Bullets from either player damage the other
- One player dying ends the game on both screens
- Disconnect handled without crash

TO TEST LOCALLY:
  Run two instances of the game on the same machine.
  Host on one instance, join with "localhost" on the other.
```

---

## 🛠️ TROUBLESHOOTING PROMPTS

Use these when things go wrong:

### ❌ Code won't compile
```
This compile error occurred in [FileName.java]:
[paste full error]
Fix only that file. Do not change any other files.
```

### ❌ Game runs but behavior is wrong
```
The game runs but [describe problem].
The issue seems to be in [FileName.java].
Fix only that file. Show me the complete corrected file.
```

### ❌ AI changed too many files
```
You changed files I didn't ask you to change.
Please revert everything except [FileName.java] and only fix the original problem.
```

### ❌ AI forgot previous context
```
We are building a Java 2D battle game with JavaFX.
We are currently on Phase [N]. All previous phases are working.
[Re-paste the phase prompt]
Now fix this specific issue: [describe issue]
```

### ❌ Want to add a small feature without breaking things
```
Phase [N] is fully working. I want to add [small feature].
Do NOT change the game loop or any other class.
Only add/modify what is necessary. Show complete files only.
```

---

## 📋 PHASE CHECKLIST

Track your progress here:

- [ ] **Phase 1** — Window opens, WASD works, clicking shoots bullets
- [ ] **Phase 2** — Enemies spawn, bullets kill enemies, player can die
- [ ] **Phase 3** — Obstacles block movement, bullets, and enemies
- [ ] **Phase 4** — HUD visible, waves work, ESC pauses, R restarts
- [ ] **Phase 5** — Two players connected on LAN, real-time combat
PHASE 6 — Game Feel & Replayability
🧠 ROLE
You are a senior Java game developer specializing in JavaFX 2D games. You are now enhancing an already working 5-phase battle game. You extend existing code cleanly — you never rewrite what already works.

⚠️ ABSOLUTE RULES
RuleDetail🔁 Extend onlyNever rewrite working Phase 1–5 code from scratch💻 Full files onlyOutput every modified file in full — no snippets✅ Compile-readyCode must work on first paste, zero TODOs🧼 Clean OOPNew features go in new classes where possible📖 Beginner-friendlyComment every non-obvious block🐛 No regressionsAll Phase 1–5 features must still work after changes

🎯 PHASE 6 GOAL
Transform the working tech demo into a genuinely fun, replayable game by adding visual polish, audio, enemy variety, an upgrade system, and persistent high score — all without touching the core architecture.

🏗️ UPDATED CLASS STRUCTURE
src/main/java/battlegame/
│
├── GameApp.java              ← unchanged
├── GameEngine.java           ← modified: integrate all new systems
├── GameState.java            ← modified: add UPGRADE_SCREEN state
├── Player.java               ← modified: add flash, screen shake trigger, upgrades
├── Bullet.java               ← modified: add trail history list
├── Enemy.java                ← modified: make abstract base class
├── HUD.java                  ← modified: add upgrade screen, high score display
├── NetworkManager.java       ← unchanged
├── GameServer.java           ← unchanged
├── GameClient.java           ← unchanged
│
├── enemies/
│   ├── FastEnemy.java        ← NEW: small, fast, 1 HP
│   └── TankEnemy.java        ← NEW: large, slow, 5 HP
│
├── effects/
│   ├── Particle.java         ← NEW: single particle for explosions
│   ├── ParticleSystem.java   ← NEW: manages all active particles
│   └── ScreenShake.java      ← NEW: tracks shake offset + decay
│
├── upgrades/
│   ├── Upgrade.java          ← NEW: enum or record for upgrade type + label
│   └── UpgradeManager.java   ← NEW: picks 3 random upgrades, applies chosen
│
└── util/
    └── ScoreManager.java     ← NEW: read/write high score to file

✅ FEATURES TO BUILD
1. 💥 Particle Explosion System
When any enemy dies, spawn a burst of particles at its position.
Particle.java
javapublic class Particle {
    public double x, y;        // position
    public double dx, dy;      // velocity
    public double life;        // seconds remaining (start at 0.5)
    public double maxLife;     // for alpha calculation
    public Color color;

    public void update(double deltaTime) {
        x += dx * deltaTime;
        y += dy * deltaTime;
        life -= deltaTime;
    }

    public boolean isDead() { return life <= 0; }

    public void draw(GraphicsContext gc) {
        double alpha = life / maxLife; // fade out
        gc.setGlobalAlpha(alpha);
        gc.setFill(color);
        gc.fillOval(x - 3, y - 3, 6, 6);
        gc.setGlobalAlpha(1.0); // always reset after drawing
    }
}
ParticleSystem.java
javapublic class ParticleSystem {
    private List<Particle> particles = new ArrayList<>();

    public void spawnExplosion(double x, double y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 60 + Math.random() * 120;
            Particle p = new Particle();
            p.x = x; p.y = y;
            p.dx = Math.cos(angle) * speed;
            p.dy = Math.sin(angle) * speed;
            p.life = p.maxLife = 0.3 + Math.random() * 0.4;
            p.color = color;
            particles.add(p);
        }
    }

    public void update(double deltaTime) {
        particles.removeIf(Particle::isDead);
        particles.forEach(p -> p.update(deltaTime));
    }

    public void draw(GraphicsContext gc) {
        particles.forEach(p -> p.draw(gc));
    }
}
Integration in GameEngine:
java// When enemy dies:
particleSystem.spawnExplosion(enemy.x, enemy.y, enemy.getParticleColor(), 12);

2. 📳 Screen Shake
ScreenShake.java
javapublic class ScreenShake {
    private double intensity = 0;
    private double duration  = 0;

    public void trigger(double intensity, double duration) {
        this.intensity = intensity;
        this.duration  = duration;
    }

    public void update(double deltaTime) {
        if (duration > 0) {
            duration -= deltaTime;
            if (duration <= 0) intensity = 0;
        }
    }

    public double getOffsetX() {
        return duration > 0 ? (Math.random() * 2 - 1) * intensity : 0;
    }

    public double getOffsetY() {
        return duration > 0 ? (Math.random() * 2 - 1) * intensity : 0;
    }
}
Integration in GameEngine.render():
javadouble shakeX = screenShake.getOffsetX();
double shakeY = screenShake.getOffsetY();
gc.save();
gc.translate(shakeX, shakeY);
// ... draw everything ...
gc.restore();
Trigger on player hit:
javascreenShake.trigger(8, 0.25); // 8px intensity, 0.25 seconds

3. ❤️ Player Hit Flash
In Player.java, add a brief red tint when taking damage:
javaprivate double flashTimer = 0;
private static final double FLASH_DURATION = 0.15; // seconds

public void takeDamage(int amount) {
    health -= amount;
    flashTimer = FLASH_DURATION;
}

public void update(double deltaTime, Set<KeyCode> keys) {
    // existing movement code...
    if (flashTimer > 0) flashTimer -= deltaTime;
}

public void draw(GraphicsContext gc) {
    if (flashTimer > 0) {
        gc.setFill(Color.RED);
    } else {
        gc.setFill(Color.DODGERBLUE);
    }
    gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
}

4. 🔫 Bullet Trail Effect
In Bullet.java, keep a short history of past positions:
javaprivate final int TRAIL_LENGTH = 6;
private List<double[]> trail = new ArrayList<>(); // each entry: [x, y]

public void update(double deltaTime) {
    trail.add(new double[]{x, y});
    if (trail.size() > TRAIL_LENGTH) trail.remove(0);
    x += dx * deltaTime;
    y += dy * deltaTime;
}

public void draw(GraphicsContext gc) {
    // Draw trail (fading)
    for (int i = 0; i < trail.size(); i++) {
        double alpha = (double) i / trail.size();
        double size  = radius * alpha;
        gc.setGlobalAlpha(alpha * 0.5);
        gc.setFill(Color.YELLOW);
        gc.fillOval(trail.get(i)[0] - size, trail.get(i)[1] - size, size * 2, size * 2);
    }
    gc.setGlobalAlpha(1.0);

    // Draw bullet itself
    gc.setFill(Color.WHITE);
    gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
}

5. 🔊 Audio System (JavaFX AudioClip)
Place .wav or .mp3 files inside src/main/resources/sounds/:
resources/sounds/
├── shoot.wav
├── enemy_death.wav
├── player_hurt.wav
└── background.mp3
In GameEngine.java:
java// Load once at startup
AudioClip shootSound      = new AudioClip(getClass().getResource("/sounds/shoot.wav").toExternalForm());
AudioClip deathSound      = new AudioClip(getClass().getResource("/sounds/enemy_death.wav").toExternalForm());
AudioClip hurtSound       = new AudioClip(getClass().getResource("/sounds/player_hurt.wav").toExternalForm());
MediaPlayer bgMusic       = new MediaPlayer(new Media(getClass().getResource("/sounds/background.mp3").toExternalForm()));

// Start background music
bgMusic.setCycleCount(MediaPlayer.INDEFINITE);
bgMusic.setVolume(0.3);
bgMusic.play();

// Play on events:
shootSound.play();       // on mouse click
deathSound.play();       // on enemy death
hurtSound.play();        // on player damage

📝 Note: Free sound effects can be found at freesound.org. Provide your own .wav files and place them in the resources folder. The game will still run without them if you wrap audio calls in a null check.

Null-safe audio helper (add to GameEngine):
javaprivate void playSound(AudioClip clip) {
    if (clip != null) clip.play();
}

6. 👾 Enemy Variety
Refactor Enemy.java into an abstract base class, then create two subtypes:
Enemy.java (abstract base)
javapublic abstract class Enemy {
    public double x, y, radius, speed;
    public int health, maxHealth;

    public abstract Color getColor();
    public abstract Color getParticleColor();

    public void moveToward(double targetX, double targetY, double deltaTime) {
        double angle = Math.atan2(targetY - y, targetX - x);
        x += Math.cos(angle) * speed * deltaTime;
        y += Math.sin(angle) * speed * deltaTime;
    }

    public boolean isDead()  { return health <= 0; }
    public void takeDamage() { health--; }

    public void draw(GraphicsContext gc) {
        gc.setFill(getColor());
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        drawHealthBar(gc);
    }

    private void drawHealthBar(GraphicsContext gc) {
        double barW = radius * 2;
        double pct  = (double) health / maxHealth;
        gc.setFill(Color.DARKRED);
        gc.fillRect(x - radius, y - radius - 8, barW, 4);
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(x - radius, y - radius - 8, barW * pct, 4);
    }
}
FastEnemy.java
javapublic class FastEnemy extends Enemy {
    public FastEnemy(double x, double y) {
        this.x = x; this.y = y;
        this.radius = 10;
        this.speed  = 180; // fast
        this.health = this.maxHealth = 1;
    }

    @Override public Color getColor()         { return Color.ORANGE; }
    @Override public Color getParticleColor() { return Color.ORANGERED; }
}
TankEnemy.java
javapublic class TankEnemy extends Enemy {
    public TankEnemy(double x, double y) {
        this.x = x; this.y = y;
        this.radius = 28;
        this.speed  = 55; // slow
        this.health = this.maxHealth = 5;
    }

    @Override public Color getColor()         { return Color.DARKVIOLET; }
    @Override public Color getParticleColor() { return Color.VIOLET; }
}
Wave-based spawn logic in GameEngine:
javaprivate Enemy spawnEnemy() {
    // Edges: pick random edge position
    double x, y;
    // ... edge spawn logic (same as Phase 2) ...

    // Wave 1-2: only fast. Wave 3+: mix. Wave 5+: include tanks
    if (wave >= 5 && Math.random() < 0.3) return new TankEnemy(x, y);
    if (wave >= 3 && Math.random() < 0.5) return new FastEnemy(x, y);
    return new FastEnemy(x, y);
}

7. ⬆️ Upgrade System
After every 10 kills (wave clear), pause the game and show 3 random upgrade cards.
Upgrade.java
javapublic enum Upgrade {
    BULLET_SPEED ("🔫 Faster Bullets",  "Bullet speed +30%"),
    HEALTH_RESTORE("❤️ Field Medic",    "+25 HP restored"),
    MOVE_SPEED   ("💨 Speed Boost",     "Movement speed +20%"),
    MULTI_SHOT   ("🌀 Double Shot",     "Fire 2 bullets per click"),
    MAX_HEALTH   ("🛡️ Fortify",        "Max HP +25");

    public final String title;
    public final String description;

    Upgrade(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
UpgradeManager.java
javapublic class UpgradeManager {

    public List<Upgrade> getRandomUpgrades(int count) {
        List<Upgrade> all = new ArrayList<>(Arrays.asList(Upgrade.values()));
        Collections.shuffle(all);
        return all.subList(0, Math.min(count, all.size()));
    }

    public void apply(Upgrade upgrade, Player player) {
        switch (upgrade) {
            case BULLET_SPEED   -> player.bulletSpeed  *= 1.3;
            case HEALTH_RESTORE -> player.health = Math.min(player.health + 25, player.maxHealth);
            case MOVE_SPEED     -> player.speed  *= 1.2;
            case MULTI_SHOT     -> player.multiShot = true;
            case MAX_HEALTH     -> { player.maxHealth += 25; player.health += 25; }
        }
    }
}
Upgrade screen rendered in HUD.java:
javapublic void drawUpgradeScreen(GraphicsContext gc, List<Upgrade> options) {
    // Dark overlay
    gc.setFill(Color.color(0, 0, 0, 0.75));
    gc.fillRect(0, 0, WIDTH, HEIGHT);

    gc.setFill(Color.WHITE);
    gc.setFont(Font.font("Arial", FontWeight.BOLD, 32));
    gc.fillText("⬆ CHOOSE AN UPGRADE", WIDTH / 2 - 200, 160);

    // Draw 3 cards side by side
    for (int i = 0; i < options.size(); i++) {
        double cardX = 150 + i * 220;
        double cardY = 220;

        // Card background
        gc.setFill(Color.color(0.1, 0.1, 0.3, 0.9));
        gc.fillRoundRect(cardX, cardY, 190, 140, 16, 16);

        // Card border
        gc.setStroke(Color.CORNFLOWERBLUE);
        gc.setLineWidth(2);
        gc.strokeRoundRect(cardX, cardY, 190, 140, 16, 16);

        // Card text
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText(options.get(i).title, cardX + 12, cardY + 40);

        gc.setFont(Font.font("Arial", 13));
        gc.fillText(options.get(i).description, cardX + 12, cardY + 70);

        // Key hint
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Arial", 12));
        gc.fillText("Press " + (i + 1), cardX + 12, cardY + 110);
    }
}
Input handling for upgrade screen in GameEngine:
java// In keyPressed handler:
if (gameState == GameState.UPGRADE_SCREEN) {
    if (e.getCode() == KeyCode.DIGIT1) selectUpgrade(0);
    if (e.getCode() == KeyCode.DIGIT2) selectUpgrade(1);
    if (e.getCode() == KeyCode.DIGIT3) selectUpgrade(2);
}

private void selectUpgrade(int index) {
    if (index < currentUpgradeOptions.size()) {
        upgradeManager.apply(currentUpgradeOptions.get(index), player);
        gameState = GameState.PLAYING;
    }
}

8. 🏆 Persistent High Score
ScoreManager.java
javapublic class ScoreManager {
    private static final String FILE_PATH = "highscore.txt";

    public int loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            return Integer.parseInt(reader.readLine().trim());
        } catch (Exception e) {
            return 0; // file doesn't exist yet or is empty
        }
    }

    public void saveHighScore(int score) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(String.valueOf(score));
        } catch (IOException e) {
            System.err.println("Could not save high score: " + e.getMessage());
        }
    }

    public int updateIfHigher(int currentScore) {
        int saved = loadHighScore();
        if (currentScore > saved) {
            saveHighScore(currentScore);
            return currentScore;
        }
        return saved;
    }
}
Integration in GameEngine:
java// On game over:
int highScore = scoreManager.updateIfHigher(score);

// Pass highScore to HUD for display on game over screen
hud.drawGameOver(gc, score, highScore);
Updated Game Over screen in HUD.java:
javapublic void drawGameOver(GraphicsContext gc, int score, int highScore) {
    // dark overlay
    gc.setFill(Color.color(0, 0, 0, 0.8));
    gc.fillRect(0, 0, WIDTH, HEIGHT);

    gc.setFill(Color.RED);
    gc.setFont(Font.font("Arial", FontWeight.BOLD, 56));
    gc.fillText("GAME OVER", WIDTH / 2 - 160, HEIGHT / 2 - 60);

    gc.setFill(Color.WHITE);
    gc.setFont(Font.font("Arial", 28));
    gc.fillText("Score:      " + score,     WIDTH / 2 - 100, HEIGHT / 2);
    gc.fillText("Best:       " + highScore, WIDTH / 2 - 100, HEIGHT / 2 + 40);

    gc.setFill(Color.LIGHTGRAY);
    gc.setFont(Font.font("Arial", 20));
    gc.fillText("Press R to Restart", WIDTH / 2 - 90, HEIGHT / 2 + 100);
}

🔧 UPDATED GAME STATE ENUM
javapublic enum GameState {
    PLAYING,
    PAUSED,
    UPGRADE_SCREEN,
    GAME_OVER
}

🔄 UPDATED GAME LOOP FLOW
AnimationTimer.handle()
    │
    ├── if PLAYING:
    │     ├── update deltaTime
    │     ├── screenShake.update()
    │     ├── player.update()
    │     ├── bullets.forEach(update)
    │     ├── enemies.forEach(moveToward player)
    │     ├── particleSystem.update()
    │     ├── check bullet↔enemy collisions
    │     │     └── on kill: spawnExplosion(), score++, check wave up
    │     ├── check enemy↔player collisions
    │     │     └── on hit: player.takeDamage(), screenShake.trigger(), playSound()
    │     ├── removeIf dead/off-screen
    │     ├── spawnTimer check → spawnEnemy()
    │     └── if waveKills >= 10 → gameState = UPGRADE_SCREEN
    │
    ├── if UPGRADE_SCREEN:
    │     └── skip update, only render upgrade cards
    │
    ├── if PAUSED:
    │     └── skip update, only render pause overlay
    │
    └── render():
          ├── gc.save() + gc.translate(shakeX, shakeY)
          ├── clear canvas
          ├── draw obstacles
          ├── draw player
          ├── draw bullets (with trails)
          ├── draw enemies (with HP bars)
          ├── particleSystem.draw()
          ├── gc.restore()
          ├── hud.draw() (health bar, score, wave)
          └── if UPGRADE_SCREEN: hud.drawUpgradeScreen()

📤 OUTPUT FORMAT
Deliver in this exact order:
1. 📁 Updated Project Structure
Clearly mark each file as NEW, MODIFIED, or UNCHANGED
2. 💻 Complete Code
One labeled block per file. Every file complete — no TODOs, no // same as before
3. 🔍 How It Works

How particles tie into the enemy death flow
How upgrade screen pauses the loop
How high score persists between sessions

4. 🧪 Quick Test Checklist
Steps to verify each new feature works

🚫 OUT OF SCOPE FOR PHASE 6
❌ Animated sprite sheets
❌ Procedural map generation
❌ More than 2 enemy types
❌ Online leaderboard
❌ Controller input
❌ Destructible obstacles

✅ DEFINITION OF DONE

 Particles burst from enemy death position
 Screen shakes when player takes damage
 Player flashes red briefly when hit
 Bullets render with a fading trail
 Shoot / hurt / death sounds play correctly
 Fast and Tank enemies spawn based on wave number
 Upgrade screen appears every 10 kills with 3 choices
 Pressing 1 / 2 / 3 applies upgrade and resumes game
 High score saved to highscore.txt and shown on game over
 All Phase 1–5 features still work correctly
---

*Generated for use with Claude, ChatGPT, Gemini, or any capable AI assistant.*
*Always test each phase before proceeding to the next.*
