package client;

import util.Protocol;
import java.io.*;
import java.net.Socket;

/**
 * Manages the TCP connection from the JavaFX client to the DRS server.
 * Singleton pattern ensures one shared connection.
 */
public class ServerConnection {

    private static final String HOST = "localhost";
    private static final int PORT = 9090;
    private static ServerConnection instance;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean connected;

    private ServerConnection() {
    }

    public static synchronized ServerConnection getInstance() {
        if (instance == null) instance = new ServerConnection();
        return instance;
    }

    public boolean connect() {
        try {
            socket = new Socket(HOST, PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            connected = true;
            System.out.println("[Client] Connected to " + HOST + ":" + PORT);
            return true;
        } catch (IOException e) {
            System.err.println("[Client] Connection failed: " + e.getMessage());
            connected = false;
            return false;
        }
    }

    public String sendRequest(String command, String data) {
        if (!connected) {
            if (!connect()) return Protocol.FAILURE + Protocol.DELIMITER + "Not connected to server";
        }
        try {
            String request = (data != null && !data.isEmpty())
                    ? command + Protocol.DELIMITER + data
                    : command;
            writer.println(request);
            return reader.readLine();
        } catch (IOException e) {
            System.err.println("[Client] Request error: " + e.getMessage());
            connected = false;
            return Protocol.FAILURE + Protocol.DELIMITER + "Communication error";
        }
    }

    public String sendRequest(String command) {
        return sendRequest(command, "");
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() {
        try {
            connected = false;
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("[Client] Disconnect error: " + e.getMessage());
        }
    }
}
