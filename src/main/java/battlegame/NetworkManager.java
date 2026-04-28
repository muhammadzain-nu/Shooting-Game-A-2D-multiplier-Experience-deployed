package battlegame;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * NetworkManager — Owns the socket and handles all network I/O.
 * Responsibility: Send local game state each frame; receive and store
 *                 remote state in a thread-safe way.
 *
 * Thread safety strategy:
 *   - remoteX/Y/hp/connected/opponentDied → volatile (single-writer receiver thread)
 *   - remoteBullets → volatile reference replaced atomically (immutable snapshot)
 *   - Sending → called from game loop thread only (single writer to 'out')
 */
public class NetworkManager {

    // ── Network streams ──────────────────────────────────────────────────────
    private final Socket       socket;
    private final PrintWriter  out;
    private final BufferedReader in;

    // ── Remote player state (written by receiver thread, read by game thread) ──
    public volatile double  remoteX         = -9999; // off-screen until first message
    public volatile double  remoteY         = -9999;
    public volatile int     remoteHp        = Player.MAX_HP;
    public volatile boolean connected       = true;
    public volatile boolean opponentDied    = false;

    // Replaced atomically — readers always get a consistent snapshot
    private volatile List<double[]> remoteBullets = Collections.emptyList();

    // ── Constructor ──────────────────────────────────────────────────────────

    public NetworkManager(Socket socket) throws IOException {
        this.socket = socket;
        this.out    = new PrintWriter(
                            new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())), true);
        this.in     = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
        startReceiverThread();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Sending  (called from game loop thread every frame)
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Serialise local player and bullets into a single newline-terminated message.
     * Format:  STATE|x|y|hp|bx1,by1;bx2,by2;...
     */
    public void sendState(Player player, List<Bullet> bullets) {
        if (!connected) return;

        StringBuilder sb = new StringBuilder("STATE|")
                .append(player.x).append('|')
                .append(player.y).append('|')
                .append(player.getHp()).append('|');

        // Encode each bullet as "x,y" separated by ";"
        for (int i = 0; i < bullets.size(); i++) {
            if (i > 0) sb.append(';');
            Bullet b = bullets.get(i);
            sb.append(b.x).append(',').append(b.y);
        }

        out.println(sb);
    }

    /** Notify opponent that this player has died. */
    public void sendGameOver() {
        if (connected) out.println("GAMEOVER");
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Receiving  (background daemon thread)
    // ────────────────────────────────────────────────────────────────────────

    private void startReceiverThread() {
        Thread receiver = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    parseMessage(line);
                }
            } catch (IOException e) {
                System.out.println("[Net] Receive error: " + e.getMessage());
            }
            connected = false; // socket closed / disconnected
            System.out.println("[Net] Disconnected.");
        }, "net-receiver");
        receiver.setDaemon(true);
        receiver.start();
    }

    /**
     * Parse one incoming message and update volatile fields.
     * Called only from the receiver thread — no synchronisation needed.
     */
    private void parseMessage(String line) {
        if ("GAMEOVER".equals(line)) {
            opponentDied = true;
            return;
        }
        if (!line.startsWith("STATE|")) return;

        try {
            // STATE | x | y | hp | bullets
            String[] parts = line.split("\\|", -1);
            remoteX  = Double.parseDouble(parts[1]);
            remoteY  = Double.parseDouble(parts[2]);
            remoteHp = Integer.parseInt(parts[3]);

            // Parse bullet list — may be empty string
            List<double[]> newBullets = new ArrayList<>();
            if (parts.length > 4 && !parts[4].isEmpty()) {
                for (String pair : parts[4].split(";")) {
                    String[] xy = pair.split(",");
                    if (xy.length == 2) {
                        newBullets.add(new double[]{
                            Double.parseDouble(xy[0]),
                            Double.parseDouble(xy[1])
                        });
                    }
                }
            }

            // Atomic replace — game thread always sees a complete consistent snapshot
            remoteBullets = Collections.unmodifiableList(newBullets);

        } catch (Exception e) {
            // Malformed message — silently ignore
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Accessors  (called from game thread)
    // ────────────────────────────────────────────────────────────────────────

    /** Returns an immutable snapshot of the remote player's current bullets. */
    public List<double[]> getRemoteBullets() {
        return remoteBullets; // volatile read — always a complete list
    }

    /** Convenience method so callers don't need to access the field directly. */
    public boolean isConnected() { return connected; }

    public void close() {
        try { socket.close(); } catch (IOException ignored) {}
    }
}
