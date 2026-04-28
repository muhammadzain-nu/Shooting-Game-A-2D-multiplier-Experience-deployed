package battlegame;

/**
 * GameState — Tracks what mode the game is currently in.
 * Responsibility: Single source of truth for the game's lifecycle state.
 *
 * Used by GameEngine to decide what to update/render,
 * and passed to HUD.draw() so the HUD knows what overlay to show.
 */
public enum GameState {
    PLAYING,   // Normal gameplay — update + render everything
    PAUSED,    // ESC pressed — freeze updates, show "PAUSED" overlay
    GAME_OVER  // Player HP == 0 — freeze updates, show game-over screen
}
