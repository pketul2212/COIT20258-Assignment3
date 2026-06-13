package server;

import database.DatabaseConnection;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Multi-threaded DRS Server.
 * Accepts client connections and handles each in a separate thread pool.
 */
public class DRSServer {

    public static final int PORT = 9090;
    private static final int THREAD_POOL_SIZE = 20;
    private ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private volatile boolean running;

    public DRSServer() {
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void start() {
        try {
            DatabaseConnection dbConn = DatabaseConnection.getInstance();
            dbConn.initializeDatabase();

            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("╔══════════════════════════════════════╗");
            System.out.println("║     DRS-Enhanced Server Started      ║");
            System.out.println("║     Listening on port: " + PORT + "          ║");
            System.out.println("╚══════════════════════════════════════╝");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[Server] New connection: " + clientSocket.getInetAddress());
                    threadPool.execute(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    if (running) System.err.println("[Server] Accept error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[Server] Failed to start: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        threadPool.shutdown();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            System.err.println("[Server] Stop error: " + e.getMessage());
        }
        DatabaseConnection.getInstance().closeConnection();
        System.out.println("[Server] Stopped.");
    }

    public static void main(String[] args) {
        DRSServer server = new DRSServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
    }
}
