/*
 * Copyright (c) 2010-2020, Yucel Guven
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Yucel Guven
 */
public class GetPrefixInfoFromDB {
//<editor-fold defaultstate="collapsed" desc="special initials/constants -yucel">

    Stage stage = new Stage();
    BorderPane root = new BorderPane();
    Scene scene = new Scene(root, 400, 240);
    GridPane grid = new GridPane();

    //DataBase
    Connection MySQLconnection = null;
    DBServerInfo dbserverInfo = new DBServerInfo();
    Statement statement = null;
    ResultSet resultSet = null;
    public ObservableList<String> liste = FXCollections.observableArrayList();
    ListView<String> prefixlist = new ListView<String>();
    //
    ContextMenu contextMenu = new ContextMenu();
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    //
    //
    String prefix = "";
    int pflen = 0;
    //</editor-fold>
    
    public GetPrefixInfoFromDB(String pfix, Connection sqlcon, DBServerInfo servinfo) {
        MySQLconnection = sqlcon;
        dbserverInfo = servinfo;
        this.prefix = pfix.split("/")[0];
        this.pflen = Integer.parseInt(pfix.split("/")[1]);
        //
        //
        root.setCenter(this.addAnchorPane(addGrid()));
        stage.setResizable(false);
        stage.setTitle("IPv6 Subnet Calculator - Prefix info from DB");
        stage.setScene(scene);

        stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if (key.getCode() == KeyCode.ESCAPE) {
                    //IPv6SubnettingTool.RemoveStageItem(stage.hashCode());                    
                    stage.close();
                }
            }
        });
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                //IPv6SubnettingTool.RemoveStageItem(stage.hashCode());
            }
        });        
        //
        SettingsAndEvents();
        int r = DBQueryPrefix();
        //System.out.println(">>>>" + r);
        
        stage.initModality(Modality.APPLICATION_MODAL);        
        stage.showAndWait();
    }

    public void SettingsAndEvents() {
        prefixlist.setPrefWidth(410);
        prefixlist.setPrefHeight(250);
        prefixlist.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        prefixlist.setContextMenu(contextMenu);
        //
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
        prefixlist.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if (key.getCode() == KeyCode.ESCAPE) {
                    stage.close();
                }
            }
        });
        //
        MenuItem contextitemSelectall = new MenuItem("Select All");
        contextitemSelectall.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                prefixlist.setVisible(false);
                prefixlist.getSelectionModel().selectAll();
                prefixlist.setVisible(true);
            }
        });
        MenuItem contextitemCopy = new MenuItem("Copy");
        contextitemCopy.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                String tmp = "";
                for (String s : prefixlist.getSelectionModel().getSelectedItems()) {
                    tmp += s + "\r\n";
                }
                content.putString(tmp);
                clipboard.setContent(content);
            }
        });
        contextMenu.getItems().addAll(contextitemSelectall, contextitemCopy);
        //        
    }

    private int DBQueryPrefix() {
        if (this.MySQLconnection == null) {
            MsgBox.Show(Alert.AlertType.WARNING, "There is no opened DB connection!");
            return -1;
        }
        int r = 0;
        String MySQLcmd = "";
        this.prefixlist.getItems().clear();
        
        MySQLcmd = "SELECT "
                    + " INET6_NTOA(prefix), pflen, netname, person, organization, "
                    + "`as-num`, phone, email, status, created, `last-updated` FROM "
                    + this.dbserverInfo.Tablename
                    + " WHERE prefix = inet6_aton('" + this.prefix + "')"
                    + " AND pflen = " + String.valueOf(this.pflen);

        try {
            statement = MySQLconnection.createStatement();
            resultSet = statement.executeQuery(MySQLcmd);

            if (resultSet.last()) {
                r = resultSet.getRow();
                resultSet.beforeFirst();
            }

            if (r > 0) {

                    while (resultSet.next()) {
                        liste.add("prefix:\t\t " + resultSet.getString(1) 
                                + "/" + resultSet.getString(2));
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
             }
            else {
                liste.add(" ");
                liste.add("> Not found. Prefix is not in the database.");
                prefixlist.setItems(liste);
            }
        }
        catch (Exception ex) {
            MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
            return -1;
        }
        
        return r;
    }
    
    public GridPane addGrid() {
        grid.setAlignment(Pos.BASELINE_CENTER);
        grid.setPadding(new Insets(0, 0, 0, 0));
        //
        grid.add(this.prefixlist, 0, 0);

        return grid;
    }

    public AnchorPane addAnchorPane(GridPane grid) {

        AnchorPane anchorpane = new AnchorPane();
        anchorpane.getChildren().addAll(grid);
        AnchorPane.setTopAnchor(grid, 0.0);

        return anchorpane;
    }
}
