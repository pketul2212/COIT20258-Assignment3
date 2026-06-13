package client.controller;

import client.ServerConnection;
import client.Session;
import util.Protocol;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class DisasterController implements Initializable {

    @FXML private TableView<DR> disasterTable;
    @FXML private TableColumn<DR, Integer> idCol;
    @FXML private TableColumn<DR, String> typeCol;
    @FXML private TableColumn<DR, String> titleCol;
    @FXML private TableColumn<DR, String> locationCol;
    @FXML private TableColumn<DR, String> severityCol;
    @FXML private TableColumn<DR, String> statusCol;
    @FXML private TableColumn<DR, Integer> affectedCol;
    @FXML private TableColumn<DR, Integer> priorityCol;
    @FXML private TableColumn<DR, String> reportedAtCol;

    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> severityCombo;
    @FXML private ComboBox<String> statusCombo;

    @FXML private TextField titleField;
    @FXML private TextField locationField;
    @FXML private TextField affectedField;

    @FXML private TextArea descriptionField;

    @FXML private Label formStatusLabel;
    @FXML private Label selectedIdLabel;
    @FXML private Label formTitleLabel;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button updateStatusButton;

    @FXML private ToggleButton prioritizeToggle;

    @FXML private VBox statusSection;
    @FXML private HBox adminButtonBox;

    private final ObservableList<DR> data = FXCollections.observableArrayList();
    private int selectedId = -1;
    private boolean canManageDisasters = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initTable();
        initDropdowns();
        initSelection();
        initPermission();
        fixInputTextColors();
        loadDisasters(false);
    }

    private void initTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        severityCol.setCellValueFactory(new PropertyValueFactory<>("severity"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        affectedCol.setCellValueFactory(new PropertyValueFactory<>("affected"));
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priorityScore"));
        reportedAtCol.setCellValueFactory(new PropertyValueFactory<>("reportedAt"));

        disasterTable.setItems(data);
        colorSeverity();
    }

    private void initDropdowns() {
        typeCombo.setItems(FXCollections.observableArrayList(
                "HURRICANE", "FIRE", "EARTHQUAKE", "FLOOD", "TORNADO", "TSUNAMI", "OTHER"
        ));

        severityCombo.setItems(FXCollections.observableArrayList(
                "LOW", "MEDIUM", "HIGH", "CRITICAL"
        ));

        statusCombo.setItems(FXCollections.observableArrayList(
                "REPORTED", "ASSESSED", "RESPONDING", "RESOLVED", "CLOSED"
        ));

        typeCombo.setValue("FIRE");
        severityCombo.setValue("MEDIUM");
        statusCombo.setValue("REPORTED");
        affectedField.setText("0");
    }

    private void initSelection() {
        disasterTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                selectedId = selected.getId();

                if (selectedIdLabel != null) {
                    selectedIdLabel.setText("Selected: #" + selectedId + " - " + selected.getTitle());
                }

                typeCombo.setValue(selected.getType());
                titleField.setText(selected.getTitle());
                descriptionField.setText(selected.getDescription());
                locationField.setText(selected.getLocation());
                severityCombo.setValue(selected.getSeverity());
                statusCombo.setValue(selected.getStatus());
                affectedField.setText(String.valueOf(selected.getAffected()));

                if (!canManageDisasters) {
                    statusCombo.setValue("REPORTED");
                }

                clearStatus();
            }
        });
    }

    private void initPermission() {
        canManageDisasters = Session.getInstance().canManageTeams();

        if (updateButton != null) {
            updateButton.setDisable(!canManageDisasters);
            updateButton.setVisible(canManageDisasters);
            updateButton.setManaged(canManageDisasters);
        }

        if (updateStatusButton != null) {
            updateStatusButton.setDisable(!canManageDisasters);
            updateStatusButton.setVisible(canManageDisasters);
            updateStatusButton.setManaged(canManageDisasters);
        }

        if (adminButtonBox != null) {
            adminButtonBox.setVisible(canManageDisasters);
            adminButtonBox.setManaged(canManageDisasters);
        }

        if (statusSection != null) {
            statusSection.setVisible(canManageDisasters);
            statusSection.setManaged(canManageDisasters);
        }

        if (statusCombo != null) {
            statusCombo.setDisable(!canManageDisasters);
        }

        if (formTitleLabel != null) {
            formTitleLabel.setText(canManageDisasters ? "Report / Edit Disaster" : "Report Disaster");
        }
    }

    private void fixInputTextColors() {
        String textFieldStyle =
                "-fx-text-fill: white;" +
                "-fx-prompt-text-fill: #c9c9c9;" +
                "-fx-control-inner-background: #123f70;";

        String textAreaStyle =
                "-fx-text-fill: white;" +
                "-fx-prompt-text-fill: #c9c9c9;" +
                "-fx-control-inner-background: #123f70;";

        if (titleField != null) titleField.setStyle(textFieldStyle);
        if (locationField != null) locationField.setStyle(textFieldStyle);
        if (affectedField != null) affectedField.setStyle(textFieldStyle);
        if (descriptionField != null) descriptionField.setStyle(textAreaStyle);
    }

    private void colorSeverity() {
        severityCol.setCellFactory(col -> new TableCell<DR, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item);

                switch (item) {
                    case "CRITICAL":
                        setStyle("-fx-background-color:#cc0000;-fx-text-fill:white;-fx-font-weight:bold;");
                        break;
                    case "HIGH":
                        setStyle("-fx-background-color:#ff8800;-fx-text-fill:white;");
                        break;
                    case "MEDIUM":
                        setStyle("-fx-background-color:#ccaa00;-fx-text-fill:white;");
                        break;
                    case "LOW":
                        setStyle("-fx-background-color:#448833;-fx-text-fill:white;");
                        break;
                    default:
                        setStyle("");
                        break;
                }
            }
        });
    }

    @FXML
    private void handleAddDisaster() {
        if (!validateDisasterForm(false)) return;

        String requestData = typeCombo.getValue()
                + Protocol.FIELD_DELIMITER + cleanInput(titleField.getText())
                + Protocol.FIELD_DELIMITER + getSafeDescription()
                + Protocol.FIELD_DELIMITER + cleanInput(locationField.getText())
                + Protocol.FIELD_DELIMITER + severityCombo.getValue()
                + Protocol.FIELD_DELIMITER + cleanInput(affectedField.getText())
                + Protocol.FIELD_DELIMITER + Session.getInstance().getCurrentUser().getUserId();

        String response = ServerConnection.getInstance().sendRequest(Protocol.REPORT_DISASTER, requestData);

        if (isSuccess(response)) {
            showSuccess("Disaster reported successfully. ID: " + getMessage(response));
            clearForm();
            loadDisasters(isPrioritized());
        } else {
            showError("Report Disaster Failed", getMessage(response));
        }
    }

    @FXML
    private void handleUpdateDisaster() {
        if (!canManageDisasters) {
            showWarning("Access Denied", "Only authorised staff can update disaster records.");
            return;
        }

        if (selectedId < 0) {
            showWarning("No Disaster Selected", "Please select a disaster record from the table first.");
            return;
        }

        if (!validateDisasterForm(true)) return;

        String requestData = selectedId
                + Protocol.FIELD_DELIMITER + typeCombo.getValue()
                + Protocol.FIELD_DELIMITER + cleanInput(titleField.getText())
                + Protocol.FIELD_DELIMITER + getSafeDescription()
                + Protocol.FIELD_DELIMITER + cleanInput(locationField.getText())
                + Protocol.FIELD_DELIMITER + severityCombo.getValue()
                + Protocol.FIELD_DELIMITER + statusCombo.getValue()
                + Protocol.FIELD_DELIMITER + cleanInput(affectedField.getText())
                + Protocol.FIELD_DELIMITER + "";

        String response = ServerConnection.getInstance().sendRequest(Protocol.UPDATE_DISASTER, requestData);

        if (isSuccess(response)) {
            showSuccess("Disaster updated successfully.");
            loadDisasters(isPrioritized());
        } else {
            showError("Update Failed", getMessage(response));
        }
    }

    @FXML
    private void handleUpdateStatus() {
        if (!canManageDisasters) {
            showWarning("Access Denied", "Only authorised staff can update disaster status.");
            return;
        }

        if (selectedId < 0) {
            showWarning("No Disaster Selected", "Please select a disaster record before updating status.");
            return;
        }

        if (statusCombo.getValue() == null || statusCombo.getValue().trim().isEmpty()) {
            showWarning("Invalid Status", "Please select a valid disaster status.");
            return;
        }

        String response = ServerConnection.getInstance().sendRequest(
                Protocol.UPDATE_DISASTER_STATUS,
                selectedId + Protocol.FIELD_DELIMITER + statusCombo.getValue()
        );

        if (isSuccess(response)) {
            showSuccess("Disaster status updated successfully.");
            loadDisasters(isPrioritized());
        } else {
            showError("Status Update Failed", getMessage(response));
        }
    }

    @FXML
    private void handleRefresh() {
        loadDisasters(isPrioritized());
        showSuccess("Disaster list refreshed.");
    }

    @FXML
    private void handleTogglePriority() {
        loadDisasters(isPrioritized());
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    private boolean validateDisasterForm(boolean requireStatus) {
        clearStatus();

        if (typeCombo.getValue() == null || typeCombo.getValue().trim().isEmpty()) {
            showWarning("Invalid Type", "Please select a disaster type.");
            return false;
        }

        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            showWarning("Title Required", "Please enter disaster title.");
            titleField.requestFocus();
            return false;
        }

        if (titleField.getText().trim().length() < 3) {
            showWarning("Invalid Title", "Title must be at least 3 characters.");
            titleField.requestFocus();
            return false;
        }

        if (locationField.getText() == null || locationField.getText().trim().isEmpty()) {
            showWarning("Location Required", "Please enter city or region.");
            locationField.requestFocus();
            return false;
        }

        if (locationField.getText().trim().length() < 2) {
            showWarning("Invalid Location", "Location must be at least 2 characters.");
            locationField.requestFocus();
            return false;
        }

        if (severityCombo.getValue() == null || severityCombo.getValue().trim().isEmpty()) {
            showWarning("Invalid Severity", "Please select disaster severity.");
            return false;
        }

        if (requireStatus && (statusCombo.getValue() == null || statusCombo.getValue().trim().isEmpty())) {
            showWarning("Invalid Status", "Please select disaster status.");
            return false;
        }

        if (affectedField.getText() == null || affectedField.getText().trim().isEmpty()) {
            showWarning("Affected Required", "Please enter estimated affected number.");
            affectedField.requestFocus();
            return false;
        }

        int affected;

        try {
            affected = Integer.parseInt(affectedField.getText().trim());
        } catch (NumberFormatException e) {
            showWarning("Invalid Affected Number", "Estimated affected must be a whole number only. Example: 10");
            affectedField.requestFocus();
            return false;
        }

        if (affected < 0) {
            showWarning("Invalid Affected Number", "Estimated affected cannot be negative.");
            affectedField.requestFocus();
            return false;
        }

        if (affected > 10000000) {
            showWarning("Invalid Affected Number", "Estimated affected number is too large.");
            affectedField.requestFocus();
            return false;
        }

        return true;
    }

    private void loadDisasters(boolean prioritized) {
        String command = prioritized ? Protocol.GET_PRIORITIZED_DISASTERS : Protocol.GET_ALL_DISASTERS;
        String response = ServerConnection.getInstance().sendRequest(command);

        data.clear();

        if (response == null || response.trim().isEmpty()) {
            showError("Server Error", "No response received from server.");
            return;
        }

        if (!response.startsWith(Protocol.SUCCESS)) {
            showError("Load Failed", getMessage(response));
            return;
        }

        String payload = response.substring(Protocol.SUCCESS.length() + Protocol.DELIMITER.length());

        if (payload.trim().isEmpty()) {
            return;
        }

        for (String item : payload.split(Protocol.LIST_DELIMITER)) {
            if (item.trim().isEmpty()) continue;

            DR row = parseDisasterRow(item.trim());

            if (row != null) {
                data.add(row);
            }
        }
    }

    private DR parseDisasterRow(String value) {
        try {
            String[] p = value.split(",", -1);

            return new DR(
                    Integer.parseInt(p[0]),
                    p.length > 1 ? p[1] : "",
                    p.length > 2 ? p[2] : "",
                    p.length > 3 ? p[3] : "",
                    p.length > 4 ? p[4] : "",
                    p.length > 5 ? p[5] : "",
                    p.length > 6 ? p[6] : "",
                    p.length > 9 ? safeParseInt(p[9]) : 0,
                    p.length > 10 ? p[10] : "",
                    p.length > 11 ? safeParseInt(p[11]) : 0
            );

        } catch (Exception e) {
            System.err.println("[DisasterController] Row parse error: " + e.getMessage());
            return null;
        }
    }

    private String getSafeDescription() {
        if (descriptionField == null || descriptionField.getText() == null) {
            return "";
        }

        return cleanInput(descriptionField.getText());
    }

    private String cleanInput(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .replace(Protocol.DELIMITER, " ")
                .replace(Protocol.FIELD_DELIMITER, " ")
                .replace(Protocol.LIST_DELIMITER, " ")
                .replace(",", ";")
                .replace("\n", " ")
                .replace("\r", " ");
    }

    private void clearForm() {
        selectedId = -1;

        titleField.clear();
        locationField.clear();
        affectedField.setText("0");

        if (descriptionField != null) {
            descriptionField.clear();
        }

        typeCombo.setValue("FIRE");
        severityCombo.setValue("MEDIUM");
        statusCombo.setValue("REPORTED");

        if (selectedIdLabel != null) {
            selectedIdLabel.setText("None selected");
        }

        disasterTable.getSelectionModel().clearSelection();
        clearStatus();
    }

    private boolean isPrioritized() {
        return prioritizeToggle != null && prioritizeToggle.isSelected();
    }

    private boolean isSuccess(String response) {
        return response != null && response.startsWith(Protocol.SUCCESS);
    }

    private String getMessage(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "No server response.";
        }

        int index = response.indexOf(Protocol.DELIMITER);

        if (index >= 0 && index + Protocol.DELIMITER.length() < response.length()) {
            return response.substring(index + Protocol.DELIMITER.length());
        }

        return response;
    }

    private int safeParseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private void showSuccess(String message) {
        if (formStatusLabel != null) {
            formStatusLabel.setStyle("-fx-text-fill: #00cc00;");
            formStatusLabel.setText(message);
        }
    }

    private void clearStatus() {
        if (formStatusLabel != null) {
            formStatusLabel.setText("");
            formStatusLabel.setStyle("-fx-text-fill: white;");
        }
    }

    private void showWarning(String title, String message) {
        if (formStatusLabel != null) {
            formStatusLabel.setStyle("-fx-text-fill: orange;");
            formStatusLabel.setText(message);
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        if (formStatusLabel != null) {
            formStatusLabel.setStyle("-fx-text-fill: red;");
            formStatusLabel.setText(message);
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class DR {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty type;
        private final SimpleStringProperty title;
        private final SimpleStringProperty description;
        private final SimpleStringProperty location;
        private final SimpleStringProperty severity;
        private final SimpleStringProperty status;
        private final SimpleIntegerProperty affected;
        private final SimpleStringProperty reportedAt;
        private final SimpleIntegerProperty priorityScore;

        public DR(int id,
                  String type,
                  String title,
                  String description,
                  String location,
                  String severity,
                  String status,
                  int affected,
                  String reportedAt,
                  int priorityScore) {
            this.id = new SimpleIntegerProperty(id);
            this.type = new SimpleStringProperty(type);
            this.title = new SimpleStringProperty(title);
            this.description = new SimpleStringProperty(description);
            this.location = new SimpleStringProperty(location);
            this.severity = new SimpleStringProperty(severity);
            this.status = new SimpleStringProperty(status);
            this.affected = new SimpleIntegerProperty(affected);
            this.reportedAt = new SimpleStringProperty(reportedAt);
            this.priorityScore = new SimpleIntegerProperty(priorityScore);
        }

        public int getId() {
            return id.get();
        }

        public String getType() {
            return type.get();
        }

        public String getTitle() {
            return title.get();
        }

        public String getDescription() {
            return description.get();
        }

        public String getLocation() {
            return location.get();
        }

        public String getSeverity() {
            return severity.get();
        }

        public String getStatus() {
            return status.get();
        }

        public int getAffected() {
            return affected.get();
        }

        public String getReportedAt() {
            return reportedAt.get();
        }

        public int getPriorityScore() {
            return priorityScore.get();
        }
    }
}