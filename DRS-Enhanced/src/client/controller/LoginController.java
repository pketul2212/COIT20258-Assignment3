package client.controller;

import client.ServerConnection;
import client.Session;
import model.User;
import util.Protocol;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;

    @FXML public void initialize() { statusLabel.setText(""); }

    @FXML private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) { statusLabel.setText("Enter username and password."); return; }
        loginButton.setDisable(true);
        statusLabel.setText("Connecting...");
        String response = ServerConnection.getInstance().sendRequest(Protocol.LOGIN,
                username + Protocol.FIELD_DELIMITER + password);
        if (response != null && response.startsWith(Protocol.SUCCESS)) {
            String userData = response.substring(Protocol.SUCCESS.length() + Protocol.DELIMITER.length());
            User user = parseUser(userData);
            Session.getInstance().setCurrentUser(user);
            openMain();
        } else {
            statusLabel.setText("Login failed: " + extractMsg(response));
            loginButton.setDisable(false);
        }
    }

    @FXML private void handleRegisterLink() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/Register.fxml"));
            Stage s = new Stage(); s.setTitle("Register"); s.setScene(new Scene(loader.load())); s.show();
        } catch (Exception e) { statusLabel.setText("Cannot open register."); }
    }

    private void openMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/view/Main.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("DRS-Enhanced - Disaster Response System");
            stage.setScene(new Scene(loader.load(), 1280, 800));
            stage.setResizable(true);
            stage.setMaximized(true);
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
            loginButton.setDisable(false);
        }
    }

    private User parseUser(String data) {
        String[] p = data.split(",", -1);
        User u = new User();
        u.setUserId(Integer.parseInt(p[0])); u.setUsername(p[1]);
        u.setRole(User.Role.valueOf(p[2]));
        u.setFullName(p.length > 3 ? p[3] : "");
        u.setEmail(p.length > 4 ? p[4] : "");
        u.setPhone(p.length > 5 ? p[5] : "");
        u.setDepartment(p.length > 6 ? p[6] : "");
        u.setActive(p.length > 7 && Boolean.parseBoolean(p[7]));
        return u;
    }

    private String extractMsg(String r) {
        if (r == null) return "No response from server";
        int i = r.indexOf(Protocol.DELIMITER); return i >= 0 ? r.substring(i+1) : r;
    }
}
