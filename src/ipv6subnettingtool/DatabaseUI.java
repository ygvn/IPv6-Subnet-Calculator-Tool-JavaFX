/*
 * Copyright (c) 2010-2019, Yucel Guven
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Yucel Guven
 */
public final class DatabaseUI {

    //<editor-fold defaultstate="collapsed" desc="special initials/constants -yucel">

    Stage stage = new Stage();
    BorderPane root = new BorderPane();
    Scene scene = new Scene(root, 450, 500);
    GridPane grid = new GridPane();
    //
    Label r0 = new Label("Selected:");
    Label r1 = new Label("");
    Label lbprefix = new Label("prefix:");
    TextField tfprefix = new TextField("");
    Label lbnetname = new Label("netname:");
    TextField tfnetname = new TextField("");
    Label lbperson = new Label("person:");
    TextField tfperson = new TextField("");
    //
    final ObservableList<String> statustypes = FXCollections.observableArrayList(
            "ASSIGNED", "ALLOCATED", "RESERVED");
    final ComboBox<String> cbStatus = new ComboBox<>(statustypes);
    Button buttonClear = new Button("Clear All");
    //
    Label lbstatus = new Label("status:");
    Label lborg = new Label("organization:");
    TextField tforg = new TextField("");
    Label lbasnum = new Label("asplain-num:");
    TextField tfasnum = new TextField("");
    Label lbphone = new Label("phone:");
    TextField tfphone = new TextField("");
    Label lbemail = new Label("e-mail:");
    TextField tfemail = new TextField("");
    //
    Button buttonQuery = new Button("Query");
    Button buttonUpdate = new Button("Insert/Update");
    Button buttonDelete = new Button("Delete");
    Button buttonExit = new Button("Exit");
    Label lbcount = new Label("[ ]");

    ListView<String> prefixlist = new ListView<String>();
    ObservableList<String> liste = FXCollections.observableArrayList();
    Label statusString = new Label("");
    //
    public String prefix = null;
    public short pflen = 0;
    public short parentpflen = 0;
    NetInfo netinfo = new NetInfo();
    //DataBase
    public static Connection MySQLconnection;
    public static Label lbdbstatus = new Label("");
    DBServerInfo dbserverInfo;
    Statement statement = null;
    ResultSet resultSet = null;
    //
    final ContextMenu contextMenu = new ContextMenu();
    final Clipboard clipboard = Clipboard.getSystemClipboard();
    final ClipboardContent content = new ClipboardContent();    
    //    
//</editor-fold>
    //
    public DatabaseUI(String inprefix, short inpflen, short inparentpflen,
            Connection sqlcon, DBServerInfo dbsrvInfo) {
        this.prefix = inprefix;
        this.pflen = inpflen;
        this.parentpflen = inparentpflen;
        DatabaseUI.MySQLconnection = sqlcon;
        this.dbserverInfo = dbsrvInfo;

        SettingsAndEvents();
        //
        SetNewValues(inprefix, inpflen, inparentpflen);
        //
        root.setCenter(this.addAnchorPane(addGrid()));
        root.setBottom(addStatusBar());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setTitle("IPv6 Subnet Calculator - Database UI");
        stage.setScene(scene);
        stage.show();
        //
        stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if (key.getCode() == KeyCode.ESCAPE) {
                    IPv6SubnettingTool.RemoveStageItem(stage.hashCode());
                    stage.close();
                }
            }
        });        
        stage.setOnCloseRequest(new EventHandler<WindowEvent>(){
            @Override
            public void handle(WindowEvent event) {
                IPv6SubnettingTool.RemoveStageItem(stage.hashCode());
            }
        });
    }
    
    public void SettingsAndEvents() {
        r0.setStyle("-fx-font-weight: bold; -fx-text-fill: royalblue");
        r1.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 12));
        r1.setStyle("-fx-text-fill: royalblue; -fx-border-color: #d3d3d3;");
        tfprefix.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        tfnetname.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        tfperson.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        tforg.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        tfasnum.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        tfphone.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        tfemail.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        cbStatus.getSelectionModel().select(0);
        //
        prefixlist.setEditable(false);
        prefixlist.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        prefixlist.setContextMenu(this.contextMenu);
        //
        lbcount.setStyle("-fx-text-fill: royalblue");
        lbprefix.setStyle("-fx-font-weight: bold");
        lbnetname.setStyle("-fx-font-weight: bold");
        lbperson.setStyle("-fx-font-weight: bold");
        //
        tfprefix.setPrefWidth(330);
        //
        buttonClear.setPrefWidth(60);
        prefixlist.setPrefHeight(185);
        buttonQuery.setPrefWidth(60);
        buttonDelete.setPrefWidth(60);
        buttonExit.setPrefWidth(60);
        //
        /* Event Handlers */
        buttonClear.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                r1.setText("");
                tfprefix.clear();
                tfnetname.clear();
                tfperson.clear();
                cbStatus.getSelectionModel().select(0);
                tforg.clear();
                tfasnum.clear();
                tfphone.clear();
                tfemail.clear();
                prefixlist.getItems().clear();
                lbcount.setText("[ ]");
                statusString.setText("");
            }
        });
        buttonQuery.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                String[] sa = CheckAll((short) 1);
                String MySQLcmd = ""; String scmd = "";
                statusString.setText("");
                
                if (sa != null) {
                    if (sa[0] != null) {
                        MySQLcmd = "SELECT "
                                + " INET6_NTOA(prefix), pflen, netname, person, organization, "
                                + "`as-num`, phone, email, status, created, `last-updated` FROM "
                                + "`" + dbserverInfo.DBname + "`.`" + dbserverInfo.Tablename + "` "
                                + " WHERE "
                                + " (INET6_NTOA(prefix)='" + sa[0] + "' AND pflen=" + String.valueOf(sa[1])
                                + " ) LIMIT 100";
                    } else {
                        if (netinfo.netname.equals("") && netinfo.person.equals("")) {
                            scmd = "";
                            return;
                        } else if (!netinfo.netname.equals("") && netinfo.person.equals("")) {
                            scmd = " netname LIKE '%" + netinfo.netname + "%'";
                        } else if (netinfo.netname.equals("") && !netinfo.person.equals("")) {
                            scmd = " person LIKE '%" + netinfo.person + "%'";
                        } else if (!netinfo.netname.equals("") && !netinfo.person.equals("")) {
                            scmd = " netname LIKE '%" + netinfo.netname + "%'"
                                    + " AND person LIKE '%" + netinfo.person + "%'";
                        }

                        MySQLcmd = "SELECT "
                                + " INET6_NTOA(prefix), pflen, netname, person, organization, "
                                + "`as-num`, phone, email, status, created, `last-updated` FROM "
                                + "`" + dbserverInfo.DBname + "`.`" + dbserverInfo.Tablename + "` "
                                + " WHERE ("
                                + scmd
                                + ") LIMIT 100";
                    }
                    try {
                        IPv6SubnettingTool.UpdateDbStatus();
                        if (MySQLconnection != null) {
                            statement = MySQLconnection.createStatement();
                            resultSet = statement.executeQuery(MySQLcmd);
                            liste.clear();
                            
                            while (resultSet.next()) {
                                liste.add("prefix:\t\t " + resultSet.getString(1) + "/" + resultSet.getString(2));
                                liste.add("netname:\t " + resultSet.getString(3));
                                liste.add("person:\t\t " + resultSet.getString(4));
                                liste.add("organization:\t " + resultSet.getString(5));
                                liste.add("as-num:\t\t " + resultSet.getString(6));
                                liste.add("phone:\t\t " + resultSet.getString(7));
                                liste.add("email:\t\t " + resultSet.getString(8));
                                liste.add("status:\t\t " + resultSet.getString(9));
                                liste.add("created:\t " + resultSet.getString(10));
                                liste.add("last-updated:\t " + resultSet.getString(11));
                                liste.add("");
                                if (sa[0] != null) {
                                    tfprefix.setText(resultSet.getString(1) + "/" + resultSet.getString(2));
                                    tfnetname.setText(resultSet.getString(3));
                                    tfperson.setText(resultSet.getString(4));
                                    tforg.setText(resultSet.getString(5));
                                    tfasnum.setText(resultSet.getString(6));
                                    tfphone.setText(resultSet.getString(7));
                                    tfemail.setText(resultSet.getString(8));
                                    cbStatus.setValue(resultSet.getString(9));
                                }
                            }
                            prefixlist.setItems(liste);
                            lbcount.setText("[" + String.valueOf(liste.size()/11) + "] record(s)");
                            statusString.setText(" Record display limit is 100");
                        }
                    } catch (Exception ex) {
                        MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                        IPv6SubnettingTool.UpdateDbStatus();
                    }
                }
            }
        });
        buttonUpdate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                String[] sa = CheckAll((short) 2);
                int r = 0;
                statusString.setText("");
                
                if (sa != null && sa[0] != null && sa[1] != null )
                {
                    try {
                    String MySQLcmd = "INSERT INTO "
                            + "`" + dbserverInfo.DBname + "`.`" + dbserverInfo.Tablename + "` "
                            + "(prefix, pflen, parentpflen, netname, person, organization, `as-num`, phone, email, status) "
                            + "VALUES( inet6_aton('" + sa[0] + "'), " + sa[1] + ", "
                            + String.valueOf(parentpflen) + ", "
                            + "'" + netinfo.netname + "', "
                            + "'" + netinfo.person + "', "
                            + "'" + netinfo.organization + "', "
                            + "'" + netinfo.asnum + "', "
                            + "'" + netinfo.phone + "', "
                            + "'" + netinfo.email + "', "
                            + "'" + netinfo.status + "') "
                            + " ON DUPLICATE KEY UPDATE "
                            + " prefix=inet6_aton('" + sa[0] + "'), "
                            + " pflen=" + sa[1] + ", "
                            + " parentpflen=" + String.valueOf(parentpflen) + ", "
                            + " netname='" + netinfo.netname + "', "
                            + " person='" + netinfo.person + "', "
                            + " organization='" + netinfo.organization + "', "
                            + " `as-num`='" + netinfo.asnum + "', "
                            + " phone='" + netinfo.phone + "', "
                            + " email='" + netinfo.email + "', "
                            + " status='" + netinfo.status + "';";

                    IPv6SubnettingTool.UpdateDbStatus();
                        if (MySQLconnection != null) {
                            statement = MySQLconnection.createStatement();
                            r = statement.executeUpdate(MySQLcmd);
                            liste.clear();
                            MySQLcmd = "SELECT "
                                    + " INET6_NTOA(prefix), pflen, netname, person, "
                                    + "organization, `as-num`, phone, email, status, created, `last-updated` FROM "
                                    + "`" + dbserverInfo.DBname + "`.`" + dbserverInfo.Tablename + "` "
                                    + " WHERE "
                                    + " (INET6_NTOA(prefix)='" + sa[0] + "' AND pflen=" + sa[1]
                                    + " ) LIMIT 100";

                            resultSet = statement.executeQuery(MySQLcmd);
                            while (resultSet.next()) {
                                liste.add("prefix:\t\t " + resultSet.getString(1) + "/" + resultSet.getString(2));
                                liste.add("netname:\t " + resultSet.getString(3));
                                liste.add("person:\t\t " + resultSet.getString(4));
                                liste.add("organization:\t " + resultSet.getString(5));
                                liste.add("as-num:\t\t " + resultSet.getString(6));
                                liste.add("phone:\t\t " + resultSet.getString(7));
                                liste.add("email:\t\t " + resultSet.getString(8));
                                liste.add("status:\t\t " + resultSet.getString(9));
                                liste.add("created:\t " + resultSet.getString(10));
                                liste.add("last-updated:\t " + resultSet.getString(11));
                                liste.add("");
                                if (sa[0] != null) {
                                    tfprefix.setText(resultSet.getString(1) + "/" + resultSet.getString(2));
                                    tfnetname.setText(resultSet.getString(3));
                                    tfperson.setText(resultSet.getString(4));
                                    tforg.setText(resultSet.getString(5));
                                    tfasnum.setText(resultSet.getString(6));
                                    tfphone.setText(resultSet.getString(7));
                                    tfemail.setText(resultSet.getString(8));
                                    cbStatus.setValue(resultSet.getString(9));
                                }
                            }
                            prefixlist.setItems(liste);
                            lbcount.setText("[" + String.valueOf(liste.size()/11) + "] record(s)");
                            statusString.setText(" [ Record inserted/updated ]");
                        }
                    } catch (Exception ex) {
                        MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                        IPv6SubnettingTool.UpdateDbStatus();
                    }
                }
            }
        });
        buttonDelete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                String[] sa = CheckAll((short) 3);
                String MySQLcmd = "";
                statusString.setText("");

                if (sa != null && sa[0] != null && sa[1] != null) {
                    MySQLcmd = "DELETE FROM "
                            + "`" + dbserverInfo.DBname + "`.`" + dbserverInfo.Tablename + "` "
                            + " WHERE"
                            + " ( prefix=inet6_aton('" + sa[0] + "') "
                            + " AND pflen=" + sa[1] + ");";

                    try {
                        IPv6SubnettingTool.UpdateDbStatus();
                        if (MySQLconnection != null) {
                            statement = MySQLconnection.createStatement();
                            int c = statement.executeUpdate(MySQLcmd);
                            liste.clear();
                            buttonClear.fire();
                            statusString.setText(" " + String.valueOf(c) + " Record DELETED!");
                        }
                    } catch (Exception ex) {
                        MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                        IPv6SubnettingTool.UpdateDbStatus();
                    }
                }
            }
        });
        buttonExit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                try {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                } catch (Exception ex) {
                    MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                }
                IPv6SubnettingTool.RemoveStageItem(stage.hashCode());                
                stage.close();
            }
        });
        prefixlist.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (t.getButton().equals(MouseButton.PRIMARY) && t.getClickCount() == 2) {
                    GetSelected();
                }
            }
        });
        prefixlist.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent k) {
                if (k.getCode() == KeyCode.ENTER) {
                    GetSelected();
                }
                else if (k.getCode() == KeyCode.ESCAPE) {
                    buttonExit.fire();
                }
            }
        });
        prefixlist.setCellFactory(cell -> {
            return new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setFont(Font.font(java.awt.Font.MONOSPACED, 12));
                    }
                }
            };
        });
        //
        MenuItem contextitemSelectall = new MenuItem("Select All");
        contextitemSelectall.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                prefixlist.setVisible(false);
                prefixlist.getSelectionModel().selectAll();
                prefixlist.setVisible(true);
            }
        });
        MenuItem contextitemCopy = new MenuItem("Copy");
        contextitemCopy.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                
                String tmp = "" ;
                for (String s : prefixlist.getSelectionModel().getSelectedItems()) {
                    tmp += s + "\r\n";
                }
                content.putString(tmp);
                clipboard.setContent(content);
            }
        });
        MenuItem contextitemModify = new MenuItem("Modify Prefix");
        contextitemModify.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                GetSelected();
            }
        });
        contextMenu.getItems().addAll(contextitemSelectall, contextitemCopy,
                new SeparatorMenuItem(), contextitemModify);
        //
        contextMenu.setOnShowing(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                if (prefixlist.getSelectionModel().getSelectedIndex() % 11 != 0) {
                    contextitemModify.setDisable(true);
                } else {
                    contextitemModify.setDisable(false);
                }
            }
        });        
        
    }//END settingsEvents
    
    public void GetSelected() {
        if (prefixlist.getSelectionModel().getSelectedIndex() % 11 == 0) {
            String s = prefixlist.getSelectionModel().getSelectedItem().split(" ")[1];
            String[] sa = s.split("/");
            String MySQLcmd = "SELECT "
                    + " INET6_NTOA(prefix), pflen, netname, person, organization, `as-num`, phone, email, status, created, `last-updated` FROM "
                    + "`" + dbserverInfo.DBname + "`.`" + dbserverInfo.Tablename + "` "
                    + " WHERE "
                    + " (INET6_NTOA(prefix)='" + sa[0] + "' AND pflen=" + String.valueOf(sa[1])
                    + " ) LIMIT 100";

            if (sa[0] != null && sa[1] != null) {
                try {
                    IPv6SubnettingTool.UpdateDbStatus();
                    if (MySQLconnection != null) {
                        statement = MySQLconnection.createStatement();
                        resultSet = statement.executeQuery(MySQLcmd);
                        liste.clear();

                        while (resultSet.next()) {
                            tfprefix.setText(resultSet.getString(1) + "/" + resultSet.getString(2));
                            tfnetname.setText(resultSet.getString(3));
                            tfperson.setText(resultSet.getString(4));
                            tforg.setText(resultSet.getString(5));
                            tfasnum.setText(resultSet.getString(6));
                            tfphone.setText(resultSet.getString(7));
                            tfemail.setText(resultSet.getString(8));
                            cbStatus.setValue(resultSet.getString(9));
                            //
                                liste.add("prefix:\t\t " + resultSet.getString(1) + "/" + resultSet.getString(2));
                                liste.add("netname:\t " + resultSet.getString(3));
                                liste.add("person:\t\t " + resultSet.getString(4));
                                liste.add("organization:\t " + resultSet.getString(5));
                                liste.add("as-num:\t\t " + resultSet.getString(6));
                                liste.add("phone:\t\t " + resultSet.getString(7));
                                liste.add("email:\t\t " + resultSet.getString(8));
                                liste.add("status:\t\t " + resultSet.getString(9));
                                liste.add("created:\t " + resultSet.getString(10));
                                liste.add("last-updated:\t " + resultSet.getString(11));
                                liste.add("");
                        }
                        prefixlist.setItems(liste);
                        lbcount.setText("[" + String.valueOf(liste.size()/11) + "] record(s)");
                        statusString.setText(" [ Modifying selected record ]");
                    }
                } catch (Exception ex) {
                    MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                    IPv6SubnettingTool.UpdateDbStatus();
                }
            }
        }
    }

    public String[] CheckAll(short n) {
        String[] sa = new String[2];
        String qprefix = "", qpflen = "";
        
        prefixlist.getItems().clear();
        //
        tfprefix.setText(tfprefix.getText().trim());
        this.netinfo.netname = tfnetname.getText().trim();
        tfnetname.setText(netinfo.netname);
        this.netinfo.person = tfperson.getText().trim();
        tfperson.setText(netinfo.person);
        this.netinfo.organization = tforg.getText().trim();
        tforg.setText(netinfo.organization);
        this.netinfo.phone = tfphone.getText().trim();
        tfphone.setText(netinfo.phone);
        this.netinfo.email = tfemail.getText().trim();
        tfemail.setText(netinfo.email);

        lbcount.setText("[ ]");
        this.netinfo.status = (String) cbStatus.getSelectionModel().getSelectedItem();
        //
        if (!tfprefix.getText().equals("")) {
            int k = 0;
            char[] ca = tfprefix.getText().toCharArray();
            for (int i = 0; i < tfprefix.getText().length(); i++) {
                if (ca[i] == '/') {
                    k++;
                }
            }
            if (k != 1) {
                tfprefix.requestFocus();
                return null;
            }

            qprefix = tfprefix.getText().split("/")[0].trim();

            if (!v6ST.IsAddressCorrect(qprefix)) {
                tfprefix.requestFocus();
                return null;
            }

            qprefix = v6ST.CompressAddress(qprefix);
            if (qprefix.equals("::")) {
                tfprefix.setText(qprefix + "/" + qpflen);
                tfprefix.requestFocus();
                return null;
            }

            qpflen = tfprefix.getText().split("/")[1].trim();

            try {
                short ui = Short.parseShort(qpflen);
                if (ui > 128) {
                    tfprefix.requestFocus();
                    return null;
                }
            } catch (NumberFormatException ex) {
                MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                tfprefix.requestFocus();
                return null;
            }
            
            tfasnum.setText(tfasnum.getText().trim());
            if (!tfasnum.getText().equals("")) {
                try {
                    this.netinfo.asnum = Long.parseLong(tfasnum.getText());
                    if (this.netinfo.asnum > 4294967295.) {
                        tfasnum.requestFocus();
                        return null;
                    }
                } catch (NumberFormatException ex) {
                    MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                    tfasnum.requestFocus();
                    return null;
                }
            }
        }
        //btn2 (update/insert)
        if (n == 2) {
            if (qprefix.equals("")
                    || netinfo.netname.equals("")
                    || netinfo.person.equals("")) {
                if (qprefix.equals("")) {
                    tfprefix.clear();
                    tfprefix.requestFocus();
                    return null;
                }
                if (netinfo.netname.equals("")) {
                    tfnetname.clear();
                    tfnetname.requestFocus();
                    return null;
                }
                if (netinfo.person.equals("")) {
                    tfperson.clear();
                    tfperson.requestFocus();
                    return null;
                }
            }
        }
        //
        //btn3: delete
        if (n == 3) {
            if (qprefix.equals("")) {
                tfprefix.clear();
                tfprefix.requestFocus();
                return null;
            }
        }
        //        
        if (!qprefix.equals("")) {
            tfprefix.setText(qprefix + "/" + qpflen);
            sa[0] = qprefix;
            sa[1] = qpflen;
        }
        //
        return sa;
    }

    public GridPane addGrid() {
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setAlignment(Pos.BASELINE_CENTER);
        grid.setPadding(new Insets(5, 10, 0, 15));
        //
        HBox hbr0 = new HBox();
        hbr0.setAlignment(Pos.TOP_RIGHT);
        hbr0.getChildren().add(r0);
        grid.add(hbr0, 0, 0);
        grid.add(r1, 1, 0);
        //
        HBox hbr1 = new HBox();
        hbr1.setAlignment(Pos.CENTER_RIGHT);
        hbr1.getChildren().add(lbprefix);
        grid.add(hbr1, 0, 1);
        grid.add(tfprefix, 1, 1);
        //
        HBox hbr2 = new HBox();
        hbr2.setAlignment(Pos.CENTER_RIGHT);
        hbr2.getChildren().add(lbnetname);
        grid.add(hbr2, 0, 2);
        grid.add(tfnetname, 1, 2);
        //
        HBox hbr3 = new HBox();
        hbr3.setAlignment(Pos.CENTER_RIGHT);
        hbr3.getChildren().add(lbperson);
        grid.add(hbr3, 0, 3);
        grid.add(tfperson, 1, 3);
        //
        HBox hbr4 = new HBox();
        hbr4.setAlignment(Pos.CENTER_RIGHT);
        hbr4.getChildren().add(lbstatus);
        grid.add(hbr4, 0, 4);
        //
        HBox hbr42 = new HBox(160);
        hbr42.setAlignment(Pos.CENTER_LEFT);
        hbr42.getChildren().addAll(cbStatus, buttonClear);
        grid.add(hbr42, 1, 4);
        //
        HBox hbr5 = new HBox();
        hbr5.setAlignment(Pos.CENTER_RIGHT);
        hbr5.getChildren().add(lborg);
        grid.add(hbr5, 0, 5);
        grid.add(tforg, 1, 5);
        //
        HBox hbr6 = new HBox();
        hbr6.setAlignment(Pos.CENTER_RIGHT);
        hbr6.getChildren().add(lbasnum);
        grid.add(hbr6, 0, 6);
        grid.add(tfasnum, 1, 6);
        //
        HBox hbr7 = new HBox();
        hbr7.setAlignment(Pos.CENTER_RIGHT);
        hbr7.getChildren().add(lbphone);
        grid.add(hbr7, 0, 7);
        grid.add(tfphone, 1, 7);
        //
        HBox hbr8 = new HBox();
        hbr8.setAlignment(Pos.CENTER_RIGHT);
        hbr8.getChildren().add(lbemail);
        grid.add(hbr8, 0, 8);
        grid.add(tfemail, 1, 8);
        //
        HBox hbr9 = new HBox();
        hbr9.setPrefWidth(80);
        hbr9.setAlignment(Pos.BOTTOM_LEFT);
        hbr9.getChildren().add(lbcount);
        grid.add(hbr9, 0, 9);
        //
        HBox hbbuttons = new HBox(20);
        hbbuttons.setAlignment(Pos.CENTER_LEFT);
        hbbuttons.getChildren().addAll(buttonQuery, buttonUpdate, buttonDelete, buttonExit);
        grid.add(hbbuttons, 1, 9);
        //
        grid.add(prefixlist, 0, 10, 2, 1);
        //
        return grid;
    }

    public AnchorPane addAnchorPane(GridPane grid) {

        AnchorPane anchorpane = new AnchorPane();
        anchorpane.getChildren().addAll(grid);
        AnchorPane.setTopAnchor(grid, 10.0);

        return anchorpane;
    }

    public HBox addStatusBar() {
        HBox hbstatus = new HBox();
        hbstatus.setStyle("-fx-border-color: #d3d3d3;");

        HBox hbstatusstr = new HBox();
        
        hbstatusstr.setPrefWidth(350);
        hbstatusstr.setAlignment(Pos.CENTER_LEFT);
        statusString.setStyle("-fx-text-fill: royalblue;");
        hbstatusstr.getChildren().add(statusString);
        
        HBox hbdbstatus = new HBox();
        lbdbstatus.setStyle("-fx-text-fill: royalblue;");
        hbdbstatus.setAlignment(Pos.CENTER_RIGHT);
        hbdbstatus.setPrefWidth(100);
        hbdbstatus.getChildren().add(lbdbstatus);
        
        hbstatus.getChildren().addAll(hbstatusstr, hbdbstatus);
        return hbstatus;        
    }

    public void SetNewValues(String s, short pf, short ppf) {
        this.prefix = s;
        this.pflen = pf;
        this.parentpflen = ppf;
        //
        if (!this.prefix.trim().equals("")) {
            r1.setText(this.prefix + "/" + String.valueOf(this.pflen));
            tfprefix.setText(r1.getText());
        }        
    }
    
    public void StageShow() {
        stage.show();
        stage.toFront();
        if (stage.isIconified()) {
            stage.setIconified(false);
        }
        stage.centerOnScreen();
    }
    
}
