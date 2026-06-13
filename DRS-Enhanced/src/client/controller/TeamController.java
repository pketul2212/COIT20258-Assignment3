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
import java.net.URL;
import java.util.ResourceBundle;

/** Feature 1: Team Coordination */
public class TeamController implements Initializable {

    @FXML private TableView<TR> teamTable;
    @FXML private TableColumn<TR, Integer> teamIdCol;
    @FXML private TableColumn<TR, String> teamNameCol, deptCol, leaderCol, contactCol, teamStatusCol, membersCol;

    @FXML private TableView<AR> assignmentTable;
    @FXML private TableColumn<AR, Integer> assignIdCol;
    @FXML private TableColumn<AR, String> assignTeamCol, assignDisasterCol, assignStatusCol, assignDateCol;

    @FXML private TextField teamNameField, leaderField, contactField, capacityField, membersField, disasterIdField;
    @FXML private ComboBox<String> deptCombo, teamStatusCombo;
    @FXML private TextArea assignNotesField;
    @FXML private Label teamStatusLabel, selectedTeamLabel;

    private final ObservableList<TR> teamData = FXCollections.observableArrayList();
    private final ObservableList<AR> assignData = FXCollections.observableArrayList();

    private int selectedTeamId = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        teamIdCol.setCellValueFactory(new PropertyValueFactory<>("teamId"));
        teamNameCol.setCellValueFactory(new PropertyValueFactory<>("teamName"));
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        leaderCol.setCellValueFactory(new PropertyValueFactory<>("leader"));
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        teamStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        membersCol.setCellValueFactory(new PropertyValueFactory<>("members"));

        teamTable.setItems(teamData);
        colorTeamStatus();

        assignIdCol.setCellValueFactory(new PropertyValueFactory<>("assignmentId"));
        assignTeamCol.setCellValueFactory(new PropertyValueFactory<>("teamName"));
        assignDisasterCol.setCellValueFactory(new PropertyValueFactory<>("disasterTitle"));
        assignStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        assignDateCol.setCellValueFactory(new PropertyValueFactory<>("assignedAt"));

        assignmentTable.setItems(assignData);

        deptCombo.setItems(FXCollections.observableArrayList(
                "FIRE_EMERGENCY", "HOSPITAL", "ELECTRICITY",
                "TRANSPORTATION", "WASTE_MANAGEMENT", "WATER_SUPPLY",
                "SCHOOLS", "LAW_ENFORCEMENT", "OTHER"
        ));

        teamStatusCombo.setItems(FXCollections.observableArrayList(
                "AVAILABLE", "DEPLOYED", "STANDBY", "UNAVAILABLE"
        ));

        deptCombo.setValue("FIRE_EMERGENCY");
        teamStatusCombo.setValue("AVAILABLE");
        capacityField.setText("10");
        membersField.setText("0");

        fixAssignNotesTextColor();

        teamTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                selectedTeamId = selected.getTeamId();

                if (selectedTeamLabel != null) {
                    selectedTeamLabel.setText("Selected: #" + selectedTeamId + " - " + selected.getTeamName());
                }

                teamNameField.setText(selected.getTeamName());
                deptCombo.setValue(selected.getDepartment());
                leaderField.setText(selected.getLeader());
                contactField.setText(selected.getContact());
                teamStatusCombo.setValue(selected.getStatus());

                String[] memberParts = selected.getMembers().split("/");
                if (memberParts.length == 2) {
                    membersField.setText(memberParts[0]);
                    capacityField.setText(memberParts[1]);
                }

                clearStatus();
            }
        });

        loadTeams();
    }

    private void fixAssignNotesTextColor() {
        if (assignNotesField != null) {
            assignNotesField.setStyle(
                    "-fx-text-fill: black;" +
                    "-fx-control-inner-background: white;" +
                    "-fx-background-color: white;" +
                    "-fx-highlight-fill: #3399ff;" +
                    "-fx-highlight-text-fill: white;" +
                    "-fx-prompt-text-fill: #555555;"
            );
        }
    }

    private void colorTeamStatus() {
        teamStatusCol.setCellFactory(col -> new TableCell<TR, String>() {
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
                    case "AVAILABLE":
                        setStyle("-fx-background-color:#4CAF50;-fx-text-fill:white;");
                        break;
                    case "DEPLOYED":
                        setStyle("-fx-background-color:#2196F3;-fx-text-fill:white;");
                        break;
                    case "STANDBY":
                        setStyle("-fx-background-color:#FF9800;-fx-text-fill:white;");
                        break;
                    default:
                        setStyle("-fx-background-color:#9E9E9E;-fx-text-fill:white;");
                }
            }
        });
    }

    @FXML
    private void handleAddTeam() {
        if (!validateTeamForm(false)) return;

        String data = teamNameField.getText().trim()
                + Protocol.FIELD_DELIMITER + deptCombo.getValue()
                + Protocol.FIELD_DELIMITER + leaderField.getText().trim()
                + Protocol.FIELD_DELIMITER + contactField.getText().trim()
                + Protocol.FIELD_DELIMITER + capacityField.getText().trim()
                + Protocol.FIELD_DELIMITER + membersField.getText().trim();

        String resp = ServerConnection.getInstance().sendRequest(Protocol.CREATE_TEAM, data);

        if (ok(resp)) {
            green("Team created successfully. ID: " + msg(resp));
            clearForm();
            loadTeams();
        } else {
            red("Error: " + msg(resp));
            showError("Team Create Failed", msg(resp));
        }
    }

    @FXML
    private void handleUpdateTeam() {
        if (selectedTeamId < 0) {
            warning("No Team Selected", "Please select a team from the table first.");
            return;
        }

        if (!validateTeamForm(true)) return;

        String data = selectedTeamId
                + Protocol.FIELD_DELIMITER + teamNameField.getText().trim()
                + Protocol.FIELD_DELIMITER + deptCombo.getValue()
                + Protocol.FIELD_DELIMITER + leaderField.getText().trim()
                + Protocol.FIELD_DELIMITER + contactField.getText().trim()
                + Protocol.FIELD_DELIMITER + teamStatusCombo.getValue()
                + Protocol.FIELD_DELIMITER + capacityField.getText().trim()
                + Protocol.FIELD_DELIMITER + membersField.getText().trim();

        String resp = ServerConnection.getInstance().sendRequest(Protocol.UPDATE_TEAM, data);

        if (ok(resp)) {
            green("Team updated successfully.");
            loadTeams();
        } else {
            red("Error: " + msg(resp));
            showError("Team Update Failed", msg(resp));
        }
    }

    @FXML
    private void handleAssignTeam() {
        if (selectedTeamId < 0) {
            warning("No Team Selected", "Please select a team before assigning.");
            return;
        }

        if (!validateDisasterId()) return;

        String notes = assignNotesField != null && assignNotesField.getText() != null
                ? assignNotesField.getText().trim()
                : "";

        String data = disasterIdField.getText().trim()
                + Protocol.FIELD_DELIMITER + selectedTeamId
                + Protocol.FIELD_DELIMITER + Session.getInstance().getCurrentUser().getUserId()
                + Protocol.FIELD_DELIMITER + notes;

        String resp = ServerConnection.getInstance().sendRequest(Protocol.ASSIGN_TEAM, data);

        if (ok(resp)) {
            green("Team assigned to disaster #" + disasterIdField.getText().trim());
            loadAssignments(Integer.parseInt(disasterIdField.getText().trim()));
            loadTeams();
        } else {
            red("Error: " + msg(resp));
            showError("Assign Team Failed", msg(resp));
        }
    }

    @FXML
    private void handleLoadAssignments() {
        if (!validateDisasterId()) return;
        loadAssignments(Integer.parseInt(disasterIdField.getText().trim()));
    }

    @FXML
    private void handleCompleteAssignment() {
        AR selected = assignmentTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            warning("No Assignment Selected", "Please select an assignment from the assignment table first.");
            return;
        }

        if ("COMPLETED".equalsIgnoreCase(selected.getStatus())) {
            warning("Already Completed", "This assignment is already completed.");
            return;
        }

        String resp = ServerConnection.getInstance().sendRequest(
                Protocol.COMPLETE_ASSIGNMENT,
                String.valueOf(selected.getAssignmentId())
        );

        if (ok(resp)) {
            green("Assignment completed successfully. Team released.");

            if (disasterIdField.getText() != null && !disasterIdField.getText().trim().isEmpty()) {
                try {
                    loadAssignments(Integer.parseInt(disasterIdField.getText().trim()));
                } catch (NumberFormatException ignored) {
                    assignData.clear();
                }
            }

            loadTeams();
        } else {
            red("Error: " + msg(resp));
            showError("Complete Assignment Failed", msg(resp));
        }
    }

    @FXML
    private void handleRefreshTeams() {
        loadTeams();
        green("Team list refreshed.");
    }

    private boolean validateTeamForm(boolean isUpdate) {
        clearStatus();

        String teamName = teamNameField.getText() == null ? "" : teamNameField.getText().trim();
        String leader = leaderField.getText() == null ? "" : leaderField.getText().trim();
        String contact = contactField.getText() == null ? "" : contactField.getText().trim();
        String capacityText = capacityField.getText() == null ? "" : capacityField.getText().trim();
        String membersText = membersField.getText() == null ? "" : membersField.getText().trim();

        if (teamName.isEmpty()) {
            warning("Team Name Required", "Please enter team name.");
            teamNameField.requestFocus();
            return false;
        }

        if (teamName.length() < 3) {
            warning("Invalid Team Name", "Team name must be at least 3 characters.");
            teamNameField.requestFocus();
            return false;
        }

        if (deptCombo.getValue() == null || deptCombo.getValue().trim().isEmpty()) {
            warning("Department Required", "Please select department.");
            return false;
        }

        if (leader.isEmpty()) {
            warning("Leader Required", "Please enter team leader name.");
            leaderField.requestFocus();
            return false;
        }

        if (leader.length() < 3) {
            warning("Invalid Leader Name", "Leader name must be at least 3 characters.");
            leaderField.requestFocus();
            return false;
        }

        if (contact.isEmpty()) {
            warning("Contact Required", "Please enter contact number.");
            contactField.requestFocus();
            return false;
        }

        if (!contact.matches("[0-9+\\-\\s()]{6,20}")) {
            warning("Invalid Contact", "Contact number must contain only numbers, spaces, +, -, or brackets.");
            contactField.requestFocus();
            return false;
        }

        if (capacityText.isEmpty()) {
            warning("Capacity Required", "Please enter team capacity.");
            capacityField.requestFocus();
            return false;
        }

        if (membersText.isEmpty()) {
            warning("Members Required", "Please enter current members.");
            membersField.requestFocus();
            return false;
        }

        int capacity;
        int members;

        try {
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            warning("Invalid Capacity", "Capacity must be a whole number. Example: 10");
            capacityField.requestFocus();
            return false;
        }

        try {
            members = Integer.parseInt(membersText);
        } catch (NumberFormatException e) {
            warning("Invalid Members", "Members must be a whole number. Example: 5");
            membersField.requestFocus();
            return false;
        }

        if (capacity <= 0) {
            warning("Invalid Capacity", "Capacity must be greater than 0.");
            capacityField.requestFocus();
            return false;
        }

        if (members < 0) {
            warning("Invalid Members", "Members cannot be negative.");
            membersField.requestFocus();
            return false;
        }

        if (members > capacity) {
            warning("Invalid Members", "Current members cannot be greater than capacity.");
            membersField.requestFocus();
            return false;
        }

        if (isUpdate && (teamStatusCombo.getValue() == null || teamStatusCombo.getValue().trim().isEmpty())) {
            warning("Status Required", "Please select team status.");
            return false;
        }

        return true;
    }

    private boolean validateDisasterId() {
        clearStatus();

        if (disasterIdField.getText() == null || disasterIdField.getText().trim().isEmpty()) {
            warning("Disaster ID Required", "Please enter Disaster ID.");
            disasterIdField.requestFocus();
            return false;
        }

        try {
            int id = Integer.parseInt(disasterIdField.getText().trim());

            if (id <= 0) {
                warning("Invalid Disaster ID", "Disaster ID must be greater than 0.");
                disasterIdField.requestFocus();
                return false;
            }

        } catch (NumberFormatException e) {
            warning("Invalid Disaster ID", "Disaster ID must be a whole number. Example: 1");
            disasterIdField.requestFocus();
            return false;
        }

        return true;
    }

    private void loadTeams() {
        String resp = ServerConnection.getInstance().sendRequest(Protocol.GET_ALL_TEAMS);
        teamData.clear();

        if (resp == null || resp.trim().isEmpty()) {
            red("No response from server.");
            return;
        }

        if (resp.startsWith(Protocol.SUCCESS)) {
            String payload = resp.substring(Protocol.SUCCESS.length() + Protocol.DELIMITER.length());

            if (payload.trim().isEmpty()) return;

            for (String item : payload.split(Protocol.LIST_DELIMITER)) {
                if (item.trim().isEmpty()) continue;

                TR row = parseTR(item.trim());
                if (row != null) teamData.add(row);
            }
        } else {
            red("Error: " + msg(resp));
        }
    }

    private void loadAssignments(int disasterId) {
        String resp = ServerConnection.getInstance().sendRequest(
                Protocol.GET_TEAM_ASSIGNMENTS,
                String.valueOf(disasterId)
        );

        assignData.clear();

        if (resp == null || resp.trim().isEmpty()) {
            red("No response from server.");
            return;
        }

        if (resp.startsWith(Protocol.SUCCESS)) {
            String payload = resp.substring(Protocol.SUCCESS.length() + Protocol.DELIMITER.length());

            if (payload.trim().isEmpty()) {
                warning("No Assignments Found", "No team assignments found for Disaster ID " + disasterId + ".");
                return;
            }

            for (String item : payload.split(Protocol.LIST_DELIMITER)) {
                if (item.trim().isEmpty()) continue;

                AR row = parseAR(item.trim());
                if (row != null) assignData.add(row);
            }
        } else {
            red("Error: " + msg(resp));
        }
    }

    private TR parseTR(String s) {
        try {
            String[] p = s.split(",", -1);
            return new TR(
                    Integer.parseInt(p[0]),
                    p[1],
                    p[2],
                    p[3],
                    p[4],
                    p[5],
                    p[6] + "/" + p[7]
            );
        } catch (Exception e) {
            System.err.println("[TeamController] Team parse error: " + e.getMessage());
            return null;
        }
    }

    private AR parseAR(String s) {
        try {
            String[] p = s.split(",", -1);
            return new AR(
                    Integer.parseInt(p[0]),
                    p[5],
                    p[6],
                    p[3],
                    p[4],
                    p[7]
            );
        } catch (Exception e) {
            System.err.println("[TeamController] Assignment parse error: " + e.getMessage());
            return null;
        }
    }

    private void clearForm() {
        teamNameField.clear();
        leaderField.clear();
        contactField.clear();
        capacityField.setText("10");
        membersField.setText("0");
        deptCombo.setValue("FIRE_EMERGENCY");
        teamStatusCombo.setValue("AVAILABLE");
        selectedTeamId = -1;

        teamTable.getSelectionModel().clearSelection();

        if (selectedTeamLabel != null) {
            selectedTeamLabel.setText("None selected");
        }

        clearStatus();
    }

    private boolean ok(String r) {
        return r != null && r.startsWith(Protocol.SUCCESS);
    }

    private String msg(String r) {
        if (r == null) return "No response";

        int i = r.indexOf(Protocol.DELIMITER);
        return i >= 0 ? r.substring(i + 1) : r;
    }

    private void clearStatus() {
        if (teamStatusLabel != null) {
            teamStatusLabel.setText("");
            teamStatusLabel.setStyle("-fx-text-fill:white;");
        }
    }

    private void warning(String title, String message) {
        if (teamStatusLabel != null) {
            teamStatusLabel.setStyle("-fx-text-fill:orange;");
            teamStatusLabel.setText(message);
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        if (teamStatusLabel != null) {
            teamStatusLabel.setStyle("-fx-text-fill:red;");
            teamStatusLabel.setText(message);
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void green(String m) {
        if (teamStatusLabel != null) {
            teamStatusLabel.setStyle("-fx-text-fill:green;");
            teamStatusLabel.setText(m);
        }
    }

    private void red(String m) {
        if (teamStatusLabel != null) {
            teamStatusLabel.setStyle("-fx-text-fill:red;");
            teamStatusLabel.setText(m);
        }
    }

    public static class TR {
        private final SimpleIntegerProperty teamId;
        private final SimpleStringProperty teamName, department, leader, contact, status, members;

        public TR(int teamId, String teamName, String department, String leader,
                  String contact, String status, String members) {
            this.teamId = new SimpleIntegerProperty(teamId);
            this.teamName = new SimpleStringProperty(teamName);
            this.department = new SimpleStringProperty(department);
            this.leader = new SimpleStringProperty(leader);
            this.contact = new SimpleStringProperty(contact);
            this.status = new SimpleStringProperty(status);
            this.members = new SimpleStringProperty(members);
        }

        public int getTeamId() { return teamId.get(); }
        public String getTeamName() { return teamName.get(); }
        public String getDepartment() { return department.get(); }
        public String getLeader() { return leader.get(); }
        public String getContact() { return contact.get(); }
        public String getStatus() { return status.get(); }
        public String getMembers() { return members.get(); }
    }

    public static class AR {
        private final SimpleIntegerProperty assignmentId;
        private final SimpleStringProperty teamName, disasterTitle, status, assignedAt, notes;

        public AR(int assignmentId, String teamName, String disasterTitle,
                  String status, String assignedAt, String notes) {
            this.assignmentId = new SimpleIntegerProperty(assignmentId);
            this.teamName = new SimpleStringProperty(teamName);
            this.disasterTitle = new SimpleStringProperty(disasterTitle);
            this.status = new SimpleStringProperty(status);
            this.assignedAt = new SimpleStringProperty(assignedAt);
            this.notes = new SimpleStringProperty(notes);
        }

        public int getAssignmentId() { return assignmentId.get(); }
        public String getTeamName() { return teamName.get(); }
        public String getDisasterTitle() { return disasterTitle.get(); }
        public String getStatus() { return status.get(); }
        public String getAssignedAt() { return assignedAt.get(); }
        public String getNotes() { return notes.get(); }
    }
}