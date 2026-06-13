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

/** Feature 2: Resource Management */
public class ResourceController implements Initializable {

    @FXML private TableView<RR> resourceTable;
    @FXML private TableColumn<RR, Integer> resIdCol, resQtyCol;
    @FXML private TableColumn<RR, String> resNameCol, resTypeCol, resUnitCol, resLocationCol, resStatusCol, resDeptCol;

    @FXML private TableView<ALR> allocationTable;
    @FXML private TableColumn<ALR, Integer> allocIdCol, allocQtyCol;
    @FXML private TableColumn<ALR, String> allocResNameCol, allocDisasterCol, allocStatusCol, allocDateCol;

    @FXML private TextField resNameField, resQtyField, resUnitField, resLocationField, allocDisasterIdField, allocQtyField;
    @FXML private ComboBox<String> resTypeCombo, resStatusCombo, resDeptCombo;
    @FXML private Label resStatusLabel, selectedResLabel;

    private final ObservableList<RR> resData = FXCollections.observableArrayList();
    private final ObservableList<ALR> allocData = FXCollections.observableArrayList();

    private int selectedResourceId = -1;
    private int selectedResourceQty = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        resIdCol.setCellValueFactory(new PropertyValueFactory<>("resourceId"));
        resNameCol.setCellValueFactory(new PropertyValueFactory<>("resourceName"));
        resTypeCol.setCellValueFactory(new PropertyValueFactory<>("resourceType"));
        resQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        resUnitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        resLocationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        resStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        resDeptCol.setCellValueFactory(new PropertyValueFactory<>("department"));

        resourceTable.setItems(resData);
        colorStatus();

        allocIdCol.setCellValueFactory(new PropertyValueFactory<>("allocationId"));
        allocResNameCol.setCellValueFactory(new PropertyValueFactory<>("resourceName"));
        allocDisasterCol.setCellValueFactory(new PropertyValueFactory<>("disasterTitle"));
        allocQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantityAllocated"));
        allocStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        allocDateCol.setCellValueFactory(new PropertyValueFactory<>("allocatedAt"));

        allocationTable.setItems(allocData);

        resTypeCombo.setItems(FXCollections.observableArrayList(
                "VEHICLE", "MEDICAL_SUPPLY", "FOOD_WATER",
                "EQUIPMENT", "PERSONNEL", "SHELTER", "OTHER"
        ));

        resStatusCombo.setItems(FXCollections.observableArrayList(
                "AVAILABLE", "IN_USE", "DEPLETED", "MAINTENANCE"
        ));

        resDeptCombo.setItems(FXCollections.observableArrayList(
                "FIRE_EMERGENCY", "HOSPITAL", "ELECTRICITY",
                "TRANSPORTATION", "WASTE_MANAGEMENT", "WATER_SUPPLY",
                "SCHOOLS", "LAW_ENFORCEMENT", "OTHER"
        ));

        resTypeCombo.setValue("EQUIPMENT");
        resStatusCombo.setValue("AVAILABLE");
        resDeptCombo.setValue("OTHER");
        resQtyField.setText("0");
        resUnitField.setText("units");
        allocQtyField.setText("1");

        resourceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                selectedResourceId = selected.getResourceId();
                selectedResourceQty = selected.getQuantity();

                if (selectedResLabel != null) {
                    selectedResLabel.setText("Selected: #" + selectedResourceId + " - " + selected.getResourceName());
                }

                resNameField.setText(selected.getResourceName());
                resTypeCombo.setValue(selected.getResourceType());
                resQtyField.setText(String.valueOf(selected.getQuantity()));
                resUnitField.setText(selected.getUnit());
                resLocationField.setText(selected.getLocation());
                resStatusCombo.setValue(selected.getStatus());
                resDeptCombo.setValue(selected.getDepartment());

                clearStatus();
            }
        });

        loadResources();
    }

    private void colorStatus() {
        resStatusCol.setCellFactory(col -> new TableCell<RR, String>() {
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
                    case "IN_USE":
                        setStyle("-fx-background-color:#2196F3;-fx-text-fill:white;");
                        break;
                    case "DEPLETED":
                        setStyle("-fx-background-color:#F44336;-fx-text-fill:white;");
                        break;
                    case "MAINTENANCE":
                        setStyle("-fx-background-color:#FF9800;-fx-text-fill:white;");
                        break;
                    default:
                        setStyle("");
                }
            }
        });
    }

    @FXML
    private void handleAddResource() {
        if (!validateResourceForm(false)) return;

        String data = resNameField.getText().trim()
                + Protocol.FIELD_DELIMITER + resTypeCombo.getValue()
                + Protocol.FIELD_DELIMITER + resQtyField.getText().trim()
                + Protocol.FIELD_DELIMITER + resUnitField.getText().trim()
                + Protocol.FIELD_DELIMITER + resLocationField.getText().trim()
                + Protocol.FIELD_DELIMITER + resDeptCombo.getValue();

        String resp = ServerConnection.getInstance().sendRequest(Protocol.CREATE_RESOURCE, data);

        if (ok(resp)) {
            green("Resource created successfully. ID: " + msg(resp));
            clearForm();
            loadResources();
        } else {
            red("Error: " + msg(resp));
            showError("Create Resource Failed", msg(resp));
        }
    }

    @FXML
    private void handleUpdateResource() {
        if (selectedResourceId < 0) {
            warning("No Resource Selected", "Please select a resource from the table first.");
            return;
        }

        if (!validateResourceForm(true)) return;

        String data = selectedResourceId
                + Protocol.FIELD_DELIMITER + resNameField.getText().trim()
                + Protocol.FIELD_DELIMITER + resTypeCombo.getValue()
                + Protocol.FIELD_DELIMITER + resQtyField.getText().trim()
                + Protocol.FIELD_DELIMITER + resUnitField.getText().trim()
                + Protocol.FIELD_DELIMITER + resLocationField.getText().trim()
                + Protocol.FIELD_DELIMITER + resStatusCombo.getValue()
                + Protocol.FIELD_DELIMITER + resDeptCombo.getValue();

        String resp = ServerConnection.getInstance().sendRequest(Protocol.UPDATE_RESOURCE, data);

        if (ok(resp)) {
            green("Resource updated successfully.");
            loadResources();
            reselectResource(selectedResourceId);
        } else {
            red("Error: " + msg(resp));
            showError("Update Resource Failed", msg(resp));
        }
    }

    @FXML
    private void handleAllocateResource() {
        if (selectedResourceId < 0) {
            warning("No Resource Selected", "Please select a resource before allocation.");
            return;
        }

        if (!validateAllocationForm()) return;

        int qty = Integer.parseInt(allocQtyField.getText().trim());

        String data = allocDisasterIdField.getText().trim()
                + Protocol.FIELD_DELIMITER + selectedResourceId
                + Protocol.FIELD_DELIMITER + qty
                + Protocol.FIELD_DELIMITER + Session.getInstance().getCurrentUser().getUserId();

        String resp = ServerConnection.getInstance().sendRequest(Protocol.ALLOCATE_RESOURCE, data);

        if (ok(resp)) {
            green("Resource allocated successfully. ID: " + msg(resp));
            refreshAllData();
        } else {
            red("Error: " + msg(resp));
            showError("Allocate Resource Failed", msg(resp));
        }
    }

    @FXML
    private void handleLoadAllocations() {
        if (!validateAllocationDisasterId()) return;
        loadAllocations(Integer.parseInt(allocDisasterIdField.getText().trim()));
    }

    @FXML
    private void handleReturnAllocation() {
        ALR selected = allocationTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            warning("No Allocation Selected", "Please select an allocation from the allocation table first.");
            return;
        }

        if (!"ACTIVE".equalsIgnoreCase(selected.getStatus())) {
            warning("Invalid Allocation", "Only ACTIVE allocations can be returned.");
            return;
        }

        String resp = ServerConnection.getInstance().sendRequest(
                Protocol.RETURN_ALLOCATION,
                String.valueOf(selected.getAllocationId())
        );

        if (ok(resp)) {
            green("Resource returned successfully.");
            refreshAllData();
        } else {
            red("Error: " + msg(resp));
            showError("Return Allocation Failed", msg(resp));
        }
    }

    @FXML
    private void handleRefreshResources() {
        refreshAllData();
        green("Resource list refreshed.");
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    private void refreshAllData() {
        int oldSelectedId = selectedResourceId;

        loadResources();

        if (oldSelectedId > 0) {
            reselectResource(oldSelectedId);
        }

        if (allocDisasterIdField.getText() != null && !allocDisasterIdField.getText().trim().isEmpty()) {
            try {
                int disasterId = Integer.parseInt(allocDisasterIdField.getText().trim());
                if (disasterId > 0) {
                    loadAllocations(disasterId);
                }
            } catch (NumberFormatException e) {
                allocData.clear();
            }
        }
    }

    private void reselectResource(int resourceId) {
        for (RR r : resData) {
            if (r.getResourceId() == resourceId) {
                resourceTable.getSelectionModel().select(r);
                resourceTable.scrollTo(r);
                return;
            }
        }

        selectedResourceId = -1;
        selectedResourceQty = 0;

        if (selectedResLabel != null) {
            selectedResLabel.setText("None selected");
        }
    }

    private boolean validateResourceForm(boolean isUpdate) {
        clearStatus();

        String name = resNameField.getText() == null ? "" : resNameField.getText().trim();
        String qtyText = resQtyField.getText() == null ? "" : resQtyField.getText().trim();
        String unit = resUnitField.getText() == null ? "" : resUnitField.getText().trim();
        String location = resLocationField.getText() == null ? "" : resLocationField.getText().trim();

        if (name.isEmpty()) {
            warning("Resource Name Required", "Please enter resource name.");
            resNameField.requestFocus();
            return false;
        }

        if (name.length() < 3) {
            warning("Invalid Resource Name", "Resource name must be at least 3 characters.");
            resNameField.requestFocus();
            return false;
        }

        if (resTypeCombo.getValue() == null || resTypeCombo.getValue().trim().isEmpty()) {
            warning("Resource Type Required", "Please select resource type.");
            return false;
        }

        if (qtyText.isEmpty()) {
            warning("Quantity Required", "Please enter quantity.");
            resQtyField.requestFocus();
            return false;
        }

        int qty;

        try {
            qty = Integer.parseInt(qtyText);
        } catch (NumberFormatException e) {
            warning("Invalid Quantity", "Quantity must be a whole number. Example: 10");
            resQtyField.requestFocus();
            return false;
        }

        if (qty < 0) {
            warning("Invalid Quantity", "Quantity cannot be negative.");
            resQtyField.requestFocus();
            return false;
        }

        if (unit.isEmpty()) {
            warning("Unit Required", "Please enter unit. Example: trucks, kits, packs, units.");
            resUnitField.requestFocus();
            return false;
        }

        if (unit.length() < 2) {
            warning("Invalid Unit", "Unit must be at least 2 characters.");
            resUnitField.requestFocus();
            return false;
        }

        if (location.isEmpty()) {
            warning("Location Required", "Please enter resource location.");
            resLocationField.requestFocus();
            return false;
        }

        if (location.length() < 2) {
            warning("Invalid Location", "Location must be at least 2 characters.");
            resLocationField.requestFocus();
            return false;
        }

        if (isUpdate && (resStatusCombo.getValue() == null || resStatusCombo.getValue().trim().isEmpty())) {
            warning("Status Required", "Please select resource status.");
            return false;
        }

        if (resDeptCombo.getValue() == null || resDeptCombo.getValue().trim().isEmpty()) {
            warning("Department Required", "Please select department.");
            return false;
        }

        return true;
    }

    private boolean validateAllocationDisasterId() {
        clearStatus();

        if (allocDisasterIdField.getText() == null || allocDisasterIdField.getText().trim().isEmpty()) {
            warning("Disaster ID Required", "Please enter Disaster ID.");
            allocDisasterIdField.requestFocus();
            return false;
        }

        try {
            int disasterId = Integer.parseInt(allocDisasterIdField.getText().trim());

            if (disasterId <= 0) {
                warning("Invalid Disaster ID", "Disaster ID must be greater than 0.");
                allocDisasterIdField.requestFocus();
                return false;
            }

        } catch (NumberFormatException e) {
            warning("Invalid Disaster ID", "Disaster ID must be a whole number. Example: 1");
            allocDisasterIdField.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateAllocationForm() {
        if (!validateAllocationDisasterId()) return false;

        if (allocQtyField.getText() == null || allocQtyField.getText().trim().isEmpty()) {
            warning("Allocation Quantity Required", "Please enter allocation quantity.");
            allocQtyField.requestFocus();
            return false;
        }

        int qty;

        try {
            qty = Integer.parseInt(allocQtyField.getText().trim());
        } catch (NumberFormatException e) {
            warning("Invalid Allocation Quantity", "Allocation quantity must be a whole number. Example: 5");
            allocQtyField.requestFocus();
            return false;
        }

        if (qty <= 0) {
            warning("Invalid Allocation Quantity", "Allocation quantity must be greater than 0.");
            allocQtyField.requestFocus();
            return false;
        }

        if (selectedResourceQty <= 0) {
            warning("Resource Not Available", "Selected resource has no available quantity.");
            return false;
        }

        if (qty > selectedResourceQty) {
            warning("Not Enough Quantity", "You cannot allocate more than available quantity. Available: " + selectedResourceQty);
            allocQtyField.requestFocus();
            return false;
        }

        return true;
    }

    private void loadResources() {
        String resp = ServerConnection.getInstance().sendRequest(Protocol.GET_ALL_RESOURCES);
        resData.clear();

        if (resp == null || resp.trim().isEmpty()) {
            red("No response from server.");
            return;
        }

        if (resp.startsWith(Protocol.SUCCESS)) {
            String payload = resp.substring(Protocol.SUCCESS.length() + Protocol.DELIMITER.length());

            if (payload.trim().isEmpty()) return;

            for (String item : payload.split(Protocol.LIST_DELIMITER)) {
                if (item.trim().isEmpty()) continue;

                RR row = parseRR(item.trim());
                if (row != null) resData.add(row);
            }
        } else {
            red("Error: " + msg(resp));
        }
    }

    private void loadAllocations(int disasterId) {
        String resp = ServerConnection.getInstance().sendRequest(
                Protocol.GET_RESOURCE_ALLOCATIONS,
                String.valueOf(disasterId)
        );

        allocData.clear();

        if (resp == null || resp.trim().isEmpty()) {
            red("No response from server.");
            return;
        }

        if (resp.startsWith(Protocol.SUCCESS)) {
            String payload = resp.substring(Protocol.SUCCESS.length() + Protocol.DELIMITER.length());

            if (payload.trim().isEmpty()) {
                return;
            }

            for (String item : payload.split(Protocol.LIST_DELIMITER)) {
                if (item.trim().isEmpty()) continue;

                ALR row = parseALR(item.trim());
                if (row != null) allocData.add(row);
            }
        } else {
            red("Error: " + msg(resp));
        }
    }

    private RR parseRR(String s) {
        try {
            String[] p = s.split(",", -1);

            return new RR(
                    Integer.parseInt(p[0]),
                    p[1],
                    p[2],
                    Integer.parseInt(p[3]),
                    p[4],
                    p[5],
                    p[6],
                    p.length > 7 ? p[7] : "OTHER"
            );
        } catch (Exception e) {
            System.err.println("[ResourceController] Resource parse error: " + e.getMessage());
            return null;
        }
    }

    private ALR parseALR(String s) {
        try {
            String[] p = s.split(",", -1);

            return new ALR(
                    Integer.parseInt(p[0]),
                    p[6],
                    p[7],
                    Integer.parseInt(p[3]),
                    p[4],
                    p[5]
            );
        } catch (Exception e) {
            System.err.println("[ResourceController] Allocation parse error: " + e.getMessage());
            return null;
        }
    }

    private void clearForm() {
        resNameField.clear();
        resQtyField.setText("0");
        resUnitField.setText("units");
        resLocationField.clear();

        resTypeCombo.setValue("EQUIPMENT");
        resStatusCombo.setValue("AVAILABLE");
        resDeptCombo.setValue("OTHER");

        selectedResourceId = -1;
        selectedResourceQty = 0;

        resourceTable.getSelectionModel().clearSelection();

        if (selectedResLabel != null) {
            selectedResLabel.setText("None selected");
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
        if (resStatusLabel != null) {
            resStatusLabel.setText("");
            resStatusLabel.setStyle("-fx-text-fill:white;");
        }
    }

    private void warning(String title, String message) {
        if (resStatusLabel != null) {
            resStatusLabel.setStyle("-fx-text-fill:orange;");
            resStatusLabel.setText(message);
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        if (resStatusLabel != null) {
            resStatusLabel.setStyle("-fx-text-fill:red;");
            resStatusLabel.setText(message);
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void green(String m) {
        if (resStatusLabel != null) {
            resStatusLabel.setStyle("-fx-text-fill:green;");
            resStatusLabel.setText(m);
        }
    }

    private void red(String m) {
        if (resStatusLabel != null) {
            resStatusLabel.setStyle("-fx-text-fill:red;");
            resStatusLabel.setText(m);
        }
    }

    public static class RR {
        private final SimpleIntegerProperty resourceId, quantity;
        private final SimpleStringProperty resourceName, resourceType, unit, location, status, department;

        public RR(int id, String name, String type, int qty, String unit, String loc, String status, String dept) {
            this.resourceId = new SimpleIntegerProperty(id);
            this.resourceName = new SimpleStringProperty(name);
            this.resourceType = new SimpleStringProperty(type);
            this.quantity = new SimpleIntegerProperty(qty);
            this.unit = new SimpleStringProperty(unit);
            this.location = new SimpleStringProperty(loc);
            this.status = new SimpleStringProperty(status);
            this.department = new SimpleStringProperty(dept);
        }

        public int getResourceId() { return resourceId.get(); }
        public String getResourceName() { return resourceName.get(); }
        public String getResourceType() { return resourceType.get(); }
        public int getQuantity() { return quantity.get(); }
        public String getUnit() { return unit.get(); }
        public String getLocation() { return location.get(); }
        public String getStatus() { return status.get(); }
        public String getDepartment() { return department.get(); }
    }

    public static class ALR {
        private final SimpleIntegerProperty allocationId, quantityAllocated;
        private final SimpleStringProperty resourceName, disasterTitle, allocatedAt, status;

        public ALR(int id, String resName, String disTitle, int qty, String at, String status) {
            this.allocationId = new SimpleIntegerProperty(id);
            this.resourceName = new SimpleStringProperty(resName);
            this.disasterTitle = new SimpleStringProperty(disTitle);
            this.quantityAllocated = new SimpleIntegerProperty(qty);
            this.allocatedAt = new SimpleStringProperty(at);
            this.status = new SimpleStringProperty(status);
        }

        public int getAllocationId() { return allocationId.get(); }
        public String getResourceName() { return resourceName.get(); }
        public String getDisasterTitle() { return disasterTitle.get(); }
        public int getQuantityAllocated() { return quantityAllocated.get(); }
        public String getAllocatedAt() { return allocatedAt.get(); }
        public String getStatus() { return status.get(); }
    }
}