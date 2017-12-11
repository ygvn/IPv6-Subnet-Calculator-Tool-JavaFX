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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Yucel Guven
 */
public final class PrefixSubLevels {
//<editor-fold defaultstate="collapsed" desc="special initials/constants -yucel">
//
    Stage stage = new Stage();
    BorderPane root = new BorderPane();
    Scene scene = new Scene(root, 430, 430);
    GridPane grid = new GridPane();
    //
    Label lb0 = new Label("Parent Prefix:");
    Label lb1 = new Label("");
    Label lb2 = new Label("Selected Prefix:");
    Label lb3 = new Label("");
    //
    static String prefix = null;
    static short pflen = 0;
    static String parentprefix = null;
    static short parentpflen = 0;
    static int t1 = 0, t2 = 0;
    String end = null;
    Boolean chks = false;
    List<String[]> liste = null;

    //Database
    static Connection MySQLconnection = null;
    DBServerInfo dbserverInfo = null;
    Statement statement = null;
    ResultSet resultSet = null;
    static Label lbdbstatus = new Label("");
    //
    TreeView<String> treeView = null;
    TreeItem<String> rootItem = null;
    TreeItem<String> dummy = null;

//</editor-fold>
    
    public PrefixSubLevels(String inprefix, short inpflen, String inparentprefix, Boolean chks,
            int int1, int int2, Connection sqlcon, DBServerInfo servinfo) {
        this.liste = new ArrayList<String[]>();
        this.treeView = new TreeView<String>();
        this.dbserverInfo = new DBServerInfo();
        this.prefix = v6ST.CompressAddress(inprefix);
        this.parentprefix = inparentprefix;
        this.pflen = inpflen;
        this.parentpflen = inpflen;
        this.chks = chks;
        this.t1 = int1;
        this.t2 = int2;
        MySQLconnection = sqlcon;
        this.dbserverInfo = servinfo;
        //
        SetSelectedFirst();
        //
        SettingsAndEvents();
        //
        root.setCenter(this.addAnchorPane(addGrid()));
        root.setBottom(addStatusBar());
        
        stage.setResizable(false);
        stage.setTitle("IPv6 Subnet Calculator - Prefix Sub-Levels");
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

    private void SettingsAndEvents() {
        lb0.setStyle("-fx-font-weight: bold;");
        lb1.setStyle("-fx-font-weight: bold;");
        lb2.setStyle("-fx-text-fill: royalblue; -fx-font-weight: bold;");
        lb3.setStyle("-fx-text-fill: royalblue; -fx-font-weight: bold;");
        treeView.setPrefWidth(410);
        treeView.setPrefHeight(350);
        treeView.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if (key.getCode() == KeyCode.ESCAPE) {
                    IPv6SubnettingTool.RemoveStageItem(stage.hashCode());                    
                    stage.close();
                }
            }
        });

    }

    @SuppressWarnings("unchecked")
    private void AddNode(TreeItem t) {
        String inprefix = t.getValue().toString().split("/")[0];
        short pfln = Short.parseShort(t.getValue().toString().split("/")[1]);
        String end = this.FindEnd(inprefix, pfln, this.chks);

        int r = this.MySQLquery(inprefix, end, pfln);

        if (r > 0) {
            int i = 0;
            for (String[] s : this.liste) {
                TreeItem<String> tmpItem = new TreeItem<String>(s[0] + "/" + s[1]);
                tmpItem.expandedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable,
                            Boolean oldValue, Boolean newValue) {
                        BooleanProperty bp = (BooleanProperty) observable;
                        TreeItem<String> t = (TreeItem<String>) bp.getBean();
                        if (newValue) {
                            t.getChildren().clear();
                            AddNode(t);
                        } else {
                            t.getChildren().clear();
                            t.getChildren().add(dummy);
                        }
                    }
                });
                t.getChildren().add(tmpItem);

                if (!this.chks) {
                    if (!s[1].equals("64")) {
                        tmpItem.getChildren().add(this.dummy);
                    }
                } else if (this.chks) {
                    if (!s[1].equals("128")) {
                        tmpItem.getChildren().add(this.dummy);
                    }
                }
                i++;
            }
        }
    }

    private String FindEnd(String snet, short pflen, Boolean chks) {
        SEaddress se = new SEaddress();
        String end = "";

        if (chks) {
            se.Start = v6ST.FormalizeAddr(snet);
            se.slash = this.t1;
            se.subnetslash = pflen;
            se = v6ST.Subnetting(se, chks);
            end = v6ST.Kolonlar(se.End);
        } else if (!chks) {
            String start = v6ST.Kolonlar(v6ST.FormalizeAddr(snet));
            start = start.substring(0, 19) + "::";
            se.Start = v6ST.FormalizeAddr(start);
            se.slash = this.t1;
            se.subnetslash = pflen;
            se = v6ST.Subnetting(se, chks);
            end = v6ST.Kolonlar(se.End);
            end = end.substring(0, 19) + "::";
        }
        end = v6ST.CompressAddress(end);

        return end;
    }

    public int MySQLquery(String inprefix, String end, short pflen) {
        
        IPv6SubnettingTool.UpdateDbStatus();
        
        if (MySQLconnection == null) {
            return -1;
        }

        this.liste.clear();

        int r = 0;
        String MySQLcmd = "";
        String is128bits = "";

        if (!this.chks) {
            is128bits = " AND pflen > " + pflen + " AND pflen <= 64";
        } else if (this.chks) {
            is128bits = " AND pflen > " + pflen;
        }

        MySQLcmd = "SELECT inet6_ntoa(prefix), pflen, netname, status "
                + " from " + dbserverInfo.Tablename
                + " WHERE ( prefix BETWEEN inet6_aton('" + inprefix + "') "
                + " AND inet6_aton('" + end + "') "
                + is128bits + " AND parentpflen= " + pflen + ") "
                + " LIMIT 32768; ";

        try {
            statement = MySQLconnection.createStatement();
            resultSet = statement.executeQuery(MySQLcmd);
            this.liste.clear();

            while (resultSet.next()) {
                this.liste.add(new String[]{
                    resultSet.getString(1), resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getString(4)}
                );
                r++;
            }
            return r;
        } catch (Exception ex) {
            MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
            IPv6SubnettingTool.UpdateDbStatus();
            return -1;
        }
    }

    public GridPane addGrid() {
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setAlignment(Pos.BASELINE_CENTER);
        grid.setPadding(new Insets(5, 10, 0, 10));
        //
        HBox hb0 = new HBox();
        hb0.setAlignment(Pos.CENTER_RIGHT);
        hb0.getChildren().add(lb0);
        grid.add(hb0, 0, 0);
        grid.add(lb1, 1, 0);
        HBox hb2 = new HBox();
        hb2.setAlignment(Pos.CENTER_RIGHT);
        hb2.getChildren().add(lb2);
        grid.add(hb2, 0, 1);
        grid.add(lb3, 1, 1);
        //
        grid.add(this.treeView, 0, 2, 2, 1);
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
        lbdbstatus.setStyle("-fx-text-fill: royalblue;");
        hbstatus.setAlignment(Pos.CENTER_RIGHT);
        hbstatus.getChildren().addAll(lbdbstatus);
        return hbstatus;
    }

    public void SetNewValues(String snet, short pf, String inppfx, int int1, int int2, Boolean inchk128) {
        this.prefix = snet;
        this.pflen = pf;
        this.parentprefix = inppfx;
        this.t1 = int1;
        this.t2 = int2;
        this.chks = inchk128;
        SetSelectedFirst();
    }    
    
    @SuppressWarnings("unchecked")
    public void SetSelectedFirst() {

        this.lb1.setText(this.parentprefix);
        this.lb3.setText("  └ " + this.prefix + "/" + this.pflen);
        this.rootItem = new TreeItem<String>(this.prefix + "/" + String.valueOf(this.pflen));
        this.dummy = new TreeItem<String>("");
        this.end = this.FindEnd(this.prefix, this.pflen, this.chks);
        this.rootItem.getChildren().add(this.dummy);
        this.treeView.setRoot(rootItem);
        rootItem.expandedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                    Boolean oldValue, Boolean newValue) {
                BooleanProperty bp = (BooleanProperty) observable;
                
                TreeItem t = (TreeItem) bp.getBean();
                if (newValue) {
                    t.getChildren().clear();
                    AddNode(t);
                } else {
                    t.getChildren().clear();
                    t.getChildren().add(dummy);
                }
            }
        });        
    }    

    public void StageShow() {
        stage.show();
        stage.toFront();
        if (stage.isIconified()) {
            stage.setIconified(false);
        }
    }
}