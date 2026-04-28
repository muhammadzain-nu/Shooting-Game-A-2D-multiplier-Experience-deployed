package battlegame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

/**
 * GameApp — JavaFX entry point.
 * Phase 5: Shows a launch menu (Single Player / Host / Join) before starting.
 */
public class GameApp extends Application {

    static final int WIDTH  = 800;
    static final int HEIGHT = 600;

    @Override
    public void start(Stage stage) {
        // ── Launch menu ──────────────────────────────────────────────────────
        Alert menu = new Alert(Alert.AlertType.NONE);
        menu.setTitle("Battle Game");
        menu.setHeaderText("Battle Game — Choose Mode");
        menu.setContentText("Select how you want to play:");

        ButtonType soloBtn = new ButtonType("Single Player");
        ButtonType hostBtn = new ButtonType("Host Game");
        ButtonType joinBtn = new ButtonType("Join Game");
        menu.getButtonTypes().setAll(soloBtn, hostBtn, joinBtn);

        Optional<ButtonType> choice = menu.showAndWait();
        if (choice.isEmpty()) return; // window closed

        NetworkManager netManager = null;

        if (choice.get() == hostBtn) {
            netManager = hostGame(stage);
            if (netManager == null) return; // failed

        } else if (choice.get() == joinBtn) {
            netManager = joinGame(stage);
            if (netManager == null) return; // cancelled or failed
        }
        // soloBtn → netManager stays null (single-player mode)

        launchGame(stage, netManager);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Host flow: start relay server then connect to localhost
    // ────────────────────────────────────────────────────────────────────────

    private NetworkManager hostGame(Stage stage) {
        // Start the relay server in a background daemon thread
        Thread serverThread = new Thread(new GameServer(), "game-server");
        serverThread.setDaemon(true);
        serverThread.start();

        // Brief pause so the ServerSocket is ready before we connect
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}

        // Host connects to its own server as player 1
        try {
            Socket socket = new Socket("localhost", GameServer.PORT);
            showInfo("Hosting on port " + GameServer.PORT + ".\n" +
                     "Waiting for opponent to connect…\n" +
                     "(Game starts immediately — opponent appears when they join.)");
            return new NetworkManager(socket);
        } catch (IOException e) {
            showError("Could not start server:\n" + e.getMessage());
            return null;
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Join flow: ask for IP, connect to that server
    // ────────────────────────────────────────────────────────────────────────

    private NetworkManager joinGame(Stage stage) {
        TextInputDialog ipDialog = new TextInputDialog("localhost");
        ipDialog.setTitle("Join Game");
        ipDialog.setHeaderText("Enter the host's IP address:");
        ipDialog.setContentText("IP:");

        Optional<String> ip = ipDialog.showAndWait();
        if (ip.isEmpty() || ip.get().isBlank()) return null;

        try {
            Socket socket = new Socket(ip.get().trim(), GameServer.PORT);
            return new NetworkManager(socket);
        } catch (IOException e) {
            showError("Could not connect to " + ip.get() + ":\n" + e.getMessage());
            return null;
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Launch the game window
    // ────────────────────────────────────────────────────────────────────────

    private void launchGame(Stage stage, NetworkManager netManager) {
        Canvas   canvas = new Canvas(WIDTH, HEIGHT);
        Scene    scene  = new Scene(new StackPane(canvas), WIDTH, HEIGHT);

        String title = (netManager == null)
                ? "Battle Game – Single Player"
                : "Battle Game – Multiplayer";

        GameEngine engine = new GameEngine(canvas, scene, WIDTH, HEIGHT, netManager);
        engine.start();

        stage.setTitle(title);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> { if (netManager != null) netManager.close(); });
        stage.show();
        canvas.requestFocus();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle("Error");
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle("Hosting");
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
