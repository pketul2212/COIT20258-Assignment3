package client.controller;

import client.ServerConnection;
import util.Protocol;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label statusLabel;
    @FXML private Button registerButton;

    @FXML private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmPasswordField.getText();
        String fullName = fullNameField.getText().trim();
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            statusLabel.setText("Username, password and full name are required."); return;
        }
        if (!password.equals(confirm)) { statusLabel.setText("Passwords do not match."); return; }
        String data = username + Protocol.FIELD_DELIMITER + password + Protocol.FIELD_DELIMITER
                + fullName + Protocol.FIELD_DELIMITER + emailField.getText().trim()
                + Protocol.FIELD_DELIMITER + phoneField.getText().trim();
        String response = ServerConnection.getInstance().sendRequest(Protocol.REGISTER, data);
        if (response != null && response.startsWith(Protocol.SUCCESS)) {
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("Registered! You can now login.");
            registerButton.setDisable(true);
        } else {
            statusLabel.setText("Error: " + extractMsg(response));
        }
    }

    @FXML private void handleCancel() { ((Stage) registerButton.getScene().getWindow()).close(); }

    private String extractMsg(String r) {
        if (r == null) return "No response"; int i = r.indexOf(Protocol.DELIMITER); return i >= 0 ? r.substring(i+1) : r;
    }
}
