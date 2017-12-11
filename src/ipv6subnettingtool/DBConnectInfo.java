/*
 * Copyright (c) 2010-2018, Yucel Guven
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ipv6subnettingtool;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Yucel Guven
 */
public final class DBConnectInfo {
    //
    Stage stage = new Stage();
    BorderPane root = new BorderPane();
    Scene scene = new Scene(root, 320, 330);
    GridPane grid = new GridPane();
    //
    Label lbtitle = new Label("Database Server Information");
    Label lbtop = new Label("(*) Please note that the database and table"
            + " will be created IF they do not exist. Your account must have" 
            + " sufficient privileges on the database and table.\r\n"
            + "(ps: Only MySQL Server)");
    Label lbIP = new Label("Server IP:");
    TextField tfIP = new TextField("127.0.0.1");
    Label lbPort = new Label("Port:");
    TextField tfPort = new TextField("3306");
    Label lbDBname = new Label("*Database Name:");
    TextField tfDBname = new TextField("");
    Label lbTablename = new Label("*Table Name:");
    TextField tfTablename = new TextField("");
    Label lbUsername = new Label("Username:");
    TextField tfUsername = new TextField("");
    Label lbPasswd = new Label("Password:");
    PasswordField tfPasswd = new PasswordField();
    Button buttonConnect = new Button("Connect");    
    Button buttonCancel = new Button("Cancel");
    
    //
    
    public DBConnectInfo() {
        SettingsAndEvents();
        //
        stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if (key.getCode() == KeyCode.ESCAPE) {
                    IPv6SubnettingTool.dbserverInfo.Initialize();
                    key.consume();
                    stage.close();
                }
            }
        });
        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                    IPv6SubnettingTool.dbserverInfo.Initialize();
                    t.consume();
                    stage.close();
            }
        });
        
        root.setCenter(this.addAnchorPane(addGrid()));
        stage.setResizable(false);
        stage.setTitle("IPv6 Subnet Calculator - DBinfo");
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        //
    }

    public void SettingsAndEvents() {
        tfIP.setPrefWidth(200);
        tfPort.setPrefWidth(200);
        tfDBname.setPrefWidth(200);
        tfTablename.setPrefWidth(200);
        tfUsername.setPrefWidth(200);
        tfPasswd.setPrefWidth(200);
        //
        tfIP.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        tfPort.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        tfDBname.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        tfTablename.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        tfUsername.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        //
        buttonConnect.setPrefWidth(60);
        buttonCancel.setPrefWidth(60);
        //        
        /* EventHandlers:*/
        
        tfIP.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    tfIP.setStyle("-fx-text-fill: black;");
                }
            }
        });
        tfPort.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    tfPort.setStyle("-fx-text-fill: black;");
                }
            }
        });
        //
        buttonConnect.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent t) {
               
                try {
                    try {
                        IPv6SubnettingTool.dbserverInfo.ServerIP = InetAddress.getByName(tfIP.getText().trim());
                    } catch (UnknownHostException ex) {
                        Logger.getLogger(DBConnectInfo.class.getName()).log(Level.SEVERE, null, ex);
                        MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                        tfIP.setStyle("-fx-text-fill: red;");
                        return;
                    }
                    try {
                        IPv6SubnettingTool.dbserverInfo.PortNum = Integer.parseInt(tfPort.getText().trim());
                    } catch (NumberFormatException ex) {
                        Logger.getLogger(DBConnectInfo.class.getName()).log(Level.SEVERE, null, ex);
                        MsgBox.Show(Alert.AlertType.ERROR, "Enter integer for port number\r\n" + ex.toString());
                        tfPort.requestFocus();
                        return;
                    }
                    IPv6SubnettingTool.dbserverInfo.DBname = tfDBname.getText().trim();
                    IPv6SubnettingTool.dbserverInfo.Tablename = tfTablename.getText().trim();
                    IPv6SubnettingTool.dbserverInfo.Username = tfUsername.getText().trim();
                    IPv6SubnettingTool.dbserverInfo.Password = tfPasswd;
                } catch (Exception ex) {
                    Logger.getLogger(DBConnectInfo.class.getName()).log(Level.SEVERE, null, ex);
                    MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                    return;
                }
                //
                if (tfIP.getText().trim().isEmpty()) {
                    MsgBox.Show(Alert.AlertType.ERROR, "Enter MySQL server's IP Address");
                    tfIP.clear();
                    tfIP.setPromptText("Enter MySQL server's IP Address");
                    tfIP.requestFocus();
                    return;
                }

                if (tfPort.getText().trim().isEmpty()) {
                    MsgBox.Show(Alert.AlertType.ERROR, "Enter MySQL server's Port Number");
                    tfPort.clear();
                    tfPort.setPromptText("Enter MySQL server's Port Number");
                    tfPort.requestFocus();
                    return;
                }
                if (tfDBname.getText().trim().isEmpty()) {
                    MsgBox.Show(Alert.AlertType.ERROR, "Enter Database Name");
                    tfDBname.clear();
                    tfDBname.setPromptText("Enter Database Name");
                    tfDBname.requestFocus();
                    return;
                }
                if (tfTablename.getText().trim().isEmpty()) {
                    MsgBox.Show(Alert.AlertType.ERROR, "Enter Database Table Name");
                    tfTablename.clear();
                    tfTablename.setPromptText("Enter Database Table Name");
                    tfTablename.requestFocus();
                    return;
                }
                if (tfUsername.getText().trim().isEmpty()) {
                    MsgBox.Show(Alert.AlertType.ERROR, "Enter Database Username");
                    tfUsername.clear();
                    tfUsername.setPromptText("Enter Database Username");
                    tfUsername.requestFocus();
                    return;
                }
                //
                stage.close();
            }
        });
        buttonCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                IPv6SubnettingTool.dbserverInfo.Initialize();
                stage.close();
            }
        });
    }

    public GridPane addGrid() {
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setAlignment(Pos.BASELINE_CENTER);
        grid.setPadding(new Insets(0, 10, 0, 10));
        //
        lbtitle.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 19));
        grid.add(lbtitle, 0, 0, 2, 1);
        //
        lbtop.setPrefWidth(310); lbtop.setWrapText(true);
        grid.add(lbtop, 0, 1, 2, 1);
        //
        HBox hblbip = new HBox();
        hblbip.setAlignment(Pos.CENTER_RIGHT);
        hblbip.getChildren().add(lbIP);
        grid.add(hblbip, 0, 3); grid.add(tfIP, 1, 3);
        //
        HBox hblbport = new HBox();
        hblbport.setAlignment(Pos.CENTER_RIGHT);
        hblbport.getChildren().add(lbPort);
        grid.add(hblbport, 0, 4); grid.add(tfPort, 1, 4);
        //
        HBox hblbdbname = new HBox();
        hblbdbname.setAlignment(Pos.CENTER_RIGHT);
        hblbdbname.getChildren().add(lbDBname);
        grid.add(hblbdbname, 0, 5); grid.add(tfDBname, 1, 5);
        //
        HBox hblbtablename = new HBox();
        hblbtablename.setAlignment(Pos.CENTER_RIGHT);
        hblbtablename.getChildren().add(lbTablename);
        grid.add(hblbtablename, 0, 6); grid.add(tfTablename, 1, 6);
        //
        HBox hblbusername = new HBox();
        hblbusername.setAlignment(Pos.CENTER_RIGHT);
        hblbusername.getChildren().add(lbUsername);
        grid.add(hblbusername, 0, 7); grid.add(tfUsername, 1, 7);
        //
        HBox hblbpasswd = new HBox();
        hblbpasswd.setAlignment(Pos.CENTER_RIGHT);
        hblbpasswd.getChildren().add(lbPasswd);
        grid.add(hblbpasswd, 0, 8); grid.add(tfPasswd, 1, 8);
        //
        HBox hbbuttons = new HBox(5);
        hbbuttons.setAlignment(Pos.CENTER_LEFT);
        hbbuttons.getChildren().addAll(buttonConnect, buttonCancel);
        grid.add(hbbuttons, 1, 9);
        //
        return grid;
    }

    public AnchorPane addAnchorPane(GridPane grid) {

        AnchorPane anchorpane = new AnchorPane();
        anchorpane.getChildren().addAll(grid);
        AnchorPane.setTopAnchor(grid, 10.0);

        return anchorpane;
    }    
}
