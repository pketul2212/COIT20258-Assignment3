package client.controller;

import client.ServerConnection;
import util.Protocol;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.util.ResourceBundle;

public class UserController implements Initializable {
    @FXML private TableView<UR> userTable;
    @FXML private TableColumn<UR,Integer> userIdCol;
    @FXML private TableColumn<UR,String>  usernameCol,roleCol,fullNameCol,emailCol,deptCol,activeCol;
    @FXML private TextField fullNameField,emailField,phoneField,departmentField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private CheckBox activeCheckBox;
    @FXML private Label userStatusLabel,selectedUserLabel;

    private final ObservableList<UR> userData = FXCollections.observableArrayList();
    private int selectedUserId = -1;

    @Override public void initialize(URL url, ResourceBundle rb) {
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        userTable.setItems(userData);
        roleCombo.setItems(FXCollections.observableArrayList("ADMIN","COORDINATOR","RESPONDER","PUBLIC"));
        roleCombo.setValue("PUBLIC");
        userTable.getSelectionModel().selectedItemProperty().addListener((obs,o,n)->{
            if(n!=null){selectedUserId=n.getUserId();
                if(selectedUserLabel!=null)selectedUserLabel.setText("Selected: "+n.getUsername());
                fullNameField.setText(n.getFullName()); emailField.setText(n.getEmail());
                departmentField.setText(n.getDepartment()); roleCombo.setValue(n.getRole());
                if(activeCheckBox!=null)activeCheckBox.setSelected(n.getActive().equals("true"));}
        });
        loadUsers();
    }

    @FXML private void handleUpdateUser(){
        if(selectedUserId<0){if(userStatusLabel!=null)userStatusLabel.setText("Select a user first.");return;}
        String data=selectedUserId+Protocol.FIELD_DELIMITER+fullNameField.getText().trim()
                +Protocol.FIELD_DELIMITER+emailField.getText().trim()
                +Protocol.FIELD_DELIMITER+(phoneField!=null?phoneField.getText().trim():"")
                +Protocol.FIELD_DELIMITER+departmentField.getText().trim()
                +Protocol.FIELD_DELIMITER+roleCombo.getValue()
                +Protocol.FIELD_DELIMITER+(activeCheckBox!=null&&activeCheckBox.isSelected());
        String resp=ServerConnection.getInstance().sendRequest(Protocol.UPDATE_USER,data);
        if(resp!=null&&resp.startsWith(Protocol.SUCCESS)){
            if(userStatusLabel!=null){userStatusLabel.setStyle("-fx-text-fill:green;");userStatusLabel.setText("User updated.");}
            loadUsers();
        } else {
            if(userStatusLabel!=null){userStatusLabel.setStyle("-fx-text-fill:red;");userStatusLabel.setText("Error: "+msg(resp));}
        }
    }

    @FXML private void handleRefreshUsers(){ loadUsers(); }

    private void loadUsers(){
        String resp=ServerConnection.getInstance().sendRequest(Protocol.GET_ALL_USERS);
        userData.clear();
        if(resp!=null&&resp.startsWith(Protocol.SUCCESS)){
            String payload=resp.substring(Protocol.SUCCESS.length()+Protocol.DELIMITER.length());
            if(payload.trim().isEmpty())return;
            for(String item:payload.split(Protocol.LIST_DELIMITER)){
                if(item.trim().isEmpty())continue;
                try{String[]p=item.split(",",-1);
                    userData.add(new UR(Integer.parseInt(p[0]),p[1],p[2],
                            p.length>3?p[3]:"",p.length>4?p[4]:"",p.length>6?p[6]:"",p.length>7?p[7]:"true"));}
                catch(Exception e){System.err.println("User parse err: "+e.getMessage());}
            }
        }
    }

    private String msg(String r){if(r==null)return"No response";int i=r.indexOf(Protocol.DELIMITER);return i>=0?r.substring(i+1):r;}

    public static class UR{
        private final SimpleIntegerProperty userId;
        private final SimpleStringProperty username,role,fullName,email,department,active;
        public UR(int userId,String username,String role,String fullName,String email,String department,String active){
            this.userId=new SimpleIntegerProperty(userId); this.username=new SimpleStringProperty(username);
            this.role=new SimpleStringProperty(role); this.fullName=new SimpleStringProperty(fullName);
            this.email=new SimpleStringProperty(email); this.department=new SimpleStringProperty(department);
            this.active=new SimpleStringProperty(active);
        }
        public int getUserId(){return userId.get();} public String getUsername(){return username.get();}
        public String getRole(){return role.get();} public String getFullName(){return fullName.get();}
        public String getEmail(){return email.get();} public String getDepartment(){return department.get();}
        public String getActive(){return active.get();}
    }
}
