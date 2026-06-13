package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the DRS-Enhanced JavaFX Client.
 */
public class DRSClientApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            ServerConnection conn = ServerConnection.getInstance();
            if (!conn.connect()) {
                showError("Cannot connect to DRS Server.\nPlease start the server first (DRSServer.java).");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/Login.fxml"));
            Scene scene = new Scene(loader.load(), 500, 520);
            primaryStage.setTitle("DRS-Enhanced - Disaster Response System");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to start application: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        ServerConnection.getInstance().disconnect();
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("DRS-Enhanced - Error");
        alert.setHeaderText("Startup Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
