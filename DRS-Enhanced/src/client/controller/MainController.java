package client.controller;

import client.ServerConnection;
import client.Session;
import model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Tab teamsTab;
    @FXML private Tab resourcesTab;
    @FXML private Tab usersTab;
    @FXML private Tab auditTab;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = Session.getInstance().getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getFullName() + "!");
            roleLabel.setText("Role: " + user.getRole());
        }
        boolean canManage = Session.getInstance().canManageTeams();
        boolean isAdmin   = Session.getInstance().isAdmin();
        if (teamsTab     != null) teamsTab.setDisable(!canManage);
        if (resourcesTab != null) resourcesTab.setDisable(!canManage);
        if (usersTab     != null) usersTab.setDisable(!isAdmin);
        if (auditTab     != null) auditTab.setDisable(!isAdmin);
    }

    @FXML private void handleLogout() {
        Session.getInstance().logout();
        ServerConnection.getInstance().disconnect();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/Login.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setTitle("DRS-Enhanced - Login");
            stage.setScene(new Scene(loader.load(), 500, 520));
            stage.setResizable(false);
            stage.setMaximized(false);
            stage.centerOnScreen();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleAbout() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("About"); a.setHeaderText("DRS-Enhanced v1.0");
        a.setContentText("COIT20258 Assignment 3\n\nFeatures:\n"
                + "Feature 1: Team Coordination\nFeature 2: Resource Management\n"
                + "Multi-threaded Server | MySQL | MVC | JavaFX");
        a.showAndWait();
    }
}
