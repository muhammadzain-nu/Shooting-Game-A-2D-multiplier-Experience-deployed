package battlegame;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * HUD — Heads-Up Display renderer.
 * Responsibility: Draw ALL UI overlays onto the canvas each frame.
 *
 * Completely decoupled from game logic — receives only raw values as params.
 * GameEngine calls hud.draw(...) once per frame after rendering game objects.
 */
public class HUD {

    // ── Layout constants ─────────────────────────────────────────────────────
    private static final double BAR_X      = 20;
    private static final double BAR_Y      = 20;
    private static final double BAR_W      = 200;
    private static final double BAR_H      = 18;
    private static final double PADDING    = 8;

    /**
     * Draw the complete HUD for one frame.
     *
     * @param gc        GraphicsContext to draw onto
     * @param hp        Player's current health
     * @param maxHp     Player's maximum health
     * @param score     Current kill score
     * @param wave      Current wave number
     * @param state     Current GameState (affects which overlays are shown)
     * @param fps       Calculated frames-per-second (0 = hide)
     * @param width     Canvas width  (for right-aligned elements)
     * @param height    Canvas height (for bottom-aligned elements)
     */
    public void draw(GraphicsContext gc,
                     int hp, int maxHp,
                     int score, int wave,
                     GameState state,
                     int fps,
                     double width, double height) {

        drawHealthBar(gc, hp, maxHp);
        drawScore(gc, score, width);
        drawWave(gc, wave, width);

        if (fps > 0) drawFps(gc, fps, width, height);

        // State-specific overlays
        switch (state) {
            case PAUSED    -> drawPausedOverlay(gc, width, height);
            case GAME_OVER -> drawGameOverOverlay(gc, score, width, height);
            default        -> { /* PLAYING — no overlay */ }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Health Bar  (top-left)
    //  Colour: green > 50 %  |  yellow 25–50 %  |  red < 25 %
    // ────────────────────────────────────────────────────────────────────────

    private void drawHealthBar(GraphicsContext gc, int hp, int maxHp) {
        double pct = (double) hp / maxHp;

        // Label above the bar
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 13));
        gc.fillText("HP  " + hp + " / " + maxHp, BAR_X, BAR_Y - 3);

        // Dark background track
        gc.setFill(Color.rgb(40, 10, 10));
        gc.fillRoundRect(BAR_X, BAR_Y, BAR_W, BAR_H, 6, 6);

        // Colour changes as HP drops
        if      (pct > 0.50) gc.setFill(Color.rgb( 50, 200,  60));
        else if (pct > 0.25) gc.setFill(Color.rgb(220, 200,  30));
        else                 gc.setFill(Color.rgb(220,  40,  40));

        // Filled portion (minimum 0 width)
        double fillW = Math.max(0, BAR_W * pct);
        gc.fillRoundRect(BAR_X, BAR_Y, fillW, BAR_H, 6, 6);

        // Thin border over everything
        gc.setStroke(Color.rgb(255, 255, 255, 0.3));
        gc.setLineWidth(1);
        gc.strokeRoundRect(BAR_X, BAR_Y, BAR_W, BAR_H, 6, 6);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Score  (top-right)
    // ────────────────────────────────────────────────────────────────────────

    private void drawScore(GraphicsContext gc, int score, double width) {
        String text = "SCORE: " + score;
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        // Approximate right-align: each monospaced char ≈ 11 px at size 18
        double textW = text.length() * 10.8;
        gc.setFill(Color.GOLD);
        gc.fillText(text, width - textW - PADDING, BAR_Y + BAR_H - 2);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Wave counter  (centre-top)
    // ────────────────────────────────────────────────────────────────────────

    private void drawWave(GraphicsContext gc, int wave, double width) {
        String text = "— WAVE " + wave + " —";
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 15));
        double textW = text.length() * 9.0;
        gc.setFill(Color.rgb(180, 210, 255));
        gc.fillText(text, (width - textW) / 2, BAR_Y + BAR_H - 2);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  FPS counter  (bottom-right, small)
    // ────────────────────────────────────────────────────────────────────────

    private void drawFps(GraphicsContext gc, int fps, double width, double height) {
        String text = "FPS: " + fps;
        gc.setFont(Font.font("Monospaced", 11));
        double textW = text.length() * 6.6;
        gc.setFill(Color.rgb(140, 140, 140, 0.7));
        gc.fillText(text, width - textW - PADDING, height - 6);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  PAUSED overlay  (centre-screen)
    // ────────────────────────────────────────────────────────────────────────

    private void drawPausedOverlay(GraphicsContext gc, double width, double height) {
        // Semi-transparent dark veil
        gc.setFill(Color.rgb(0, 0, 0, 0.55));
        gc.fillRect(0, 0, width, height);

        // "PAUSED" title
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 52));
        gc.setFill(Color.rgb(220, 220, 255));
        double titleW = 52 * 6.0; // approx
        gc.fillText("PAUSED", width / 2 - titleW / 2, height / 2 - 10);

        // Sub-hint
        gc.setFont(Font.font("Monospaced", 18));
        gc.setFill(Color.rgb(180, 180, 180));
        gc.fillText("Press ESC to resume", width / 2 - 110, height / 2 + 35);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  GAME OVER overlay  (centre-screen)
    // ────────────────────────────────────────────────────────────────────────

    private void drawGameOverOverlay(GraphicsContext gc, int score, double width, double height) {
        // Dark veil
        gc.setFill(Color.rgb(0, 0, 0, 0.72));
        gc.fillRect(0, 0, width, height);

        // "GAME OVER" title
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 54));
        gc.setFill(Color.rgb(220, 50, 50));
        gc.fillText("GAME OVER", width / 2 - 168, height / 2 - 40);

        // Final score
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 24));
        gc.setFill(Color.WHITE);
        gc.fillText("Final Score:  " + score, width / 2 - 100, height / 2 + 10);

        // Restart hint
        gc.setFont(Font.font("Monospaced", 18));
        gc.setFill(Color.rgb(180, 220, 180));
        gc.fillText("Press  R  to Restart", width / 2 - 100, height / 2 + 50);
    }
}
