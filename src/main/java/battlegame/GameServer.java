package battlegame;

import java.io.*;
import java.net.*;

/**
 * GameServer — Pure relay server. No game logic runs here.
 * Responsibility: Accept exactly 2 player connections and forward each
 *                 player's messages to the other player.
 *
 * Run in a background daemon thread by GameApp (host side).
 */
public class GameServer implements Runnable {

    public static final int PORT = 5555;

    @Override
    public void run() {
        System.out.println("[Server] Starting on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            // Accept Player 1 (the host's own game client connects first)
            Socket p1 = serverSocket.accept();
            System.out.println("[Server] Player 1 connected: " + p1.getInetAddress());

            // Accept Player 2 (the remote client)
            Socket p2 = serverSocket.accept();
            System.out.println("[Server] Player 2 connected: " + p2.getInetAddress());

            // Two daemon relay threads — each reads from one socket, writes to the other
            Thread t1 = new Thread(() -> relay(p1, p2, "P1→P2"), "relay-p1-p2");
            Thread t2 = new Thread(() -> relay(p2, p1, "P2→P1"), "relay-p2-p1");
            t1.setDaemon(true);
            t2.setDaemon(true);
            t1.start();
            t2.start();

            // Block until both relay threads finish (both connections closed)
            t1.join();
            t2.join();

        } catch (IOException | InterruptedException e) {
            System.out.println("[Server] Stopped: " + e.getMessage());
        }
        System.out.println("[Server] Shut down.");
    }

    /**
     * Read every newline-terminated message from {@code from} and write it
     * to {@code to}.  Exits silently when either socket closes.
     */
    private void relay(Socket from, Socket to, String label) {
        try (
            BufferedReader  in  = new BufferedReader(
                                        new InputStreamReader(from.getInputStream()));
            PrintWriter     out = new PrintWriter(
                                        new BufferedWriter(
                                            new OutputStreamWriter(to.getOutputStream())), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                out.println(line);
            }
        } catch (IOException e) {
            System.out.println("[Server] " + label + " ended: " + e.getMessage());
        }
    }
}
