package client.controller;

import client.ServerConnection;
import util.Protocol;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.util.ResourceBundle;

public class AuditController implements Initializable {
    @FXML private TableView<AuditRow> auditTable;
    @FXML private TableColumn<AuditRow,String> timestampCol,usernameCol,actionCol,tableCol,recordIdCol;
    @FXML private Label auditStatusLabel;

    private final ObservableList<AuditRow> auditData = FXCollections.observableArrayList();

    @Override public void initialize(URL url, ResourceBundle rb) {
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
        tableCol.setCellValueFactory(new PropertyValueFactory<>("tableAffected"));
        recordIdCol.setCellValueFactory(new PropertyValueFactory<>("recordId"));
        auditTable.setItems(auditData);
        loadAuditLogs();
    }

    @FXML private void handleRefreshLogs(){ loadAuditLogs(); }

    private void loadAuditLogs(){
        String resp=ServerConnection.getInstance().sendRequest(Protocol.GET_AUDIT_LOGS);
        auditData.clear();
        if(resp!=null&&resp.startsWith(Protocol.SUCCESS)){
            String payload=resp.substring(Protocol.SUCCESS.length()+Protocol.DELIMITER.length());
            if(payload.trim().isEmpty()){if(auditStatusLabel!=null)auditStatusLabel.setText("No logs.");return;}
            int count=0;
            for(String item:payload.split(Protocol.LIST_DELIMITER)){
                if(item.trim().isEmpty())continue;
                String[]p=item.split(",",-1);
                if(p.length>=5){auditData.add(new AuditRow(p[0],p[1],p[2],p[3],p[4]));count++;}
            }
            if(auditStatusLabel!=null)auditStatusLabel.setText("Loaded "+count+" entries.");
        } else {if(auditStatusLabel!=null)auditStatusLabel.setText("Failed to load logs.");}
    }

    public static class AuditRow{
        private final SimpleStringProperty timestamp,username,action,tableAffected,recordId;
        public AuditRow(String ts,String user,String action,String table,String rid){
            this.timestamp=new SimpleStringProperty(ts); this.username=new SimpleStringProperty(user);
            this.action=new SimpleStringProperty(action); this.tableAffected=new SimpleStringProperty(table);
            this.recordId=new SimpleStringProperty(rid);
        }
        public String getTimestamp(){return timestamp.get();} public String getUsername(){return username.get();}
        public String getAction(){return action.get();} public String getTableAffected(){return tableAffected.get();}
        public String getRecordId(){return recordId.get();}
    }
}
