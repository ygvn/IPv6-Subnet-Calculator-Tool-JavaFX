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

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Locale;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Yucel Guven
 */
//public final class StatsUsage {
public final class StatsUsage {
//<editor-fold defaultstate="collapsed" desc="special initials/constants -yucel">
    Stage stage = new Stage();
    BorderPane root = new BorderPane();
    Scene scene = new Scene(root, 420, 400);
    GridPane grid = new GridPane();
    ListAssignedfromDB listassigned = null;
    Label statusString = new Label("");
    public static Label lbdbstatus = new Label("");
    //
    final double gcWidth = 400, gcHeight = 150;
    final Canvas canvas = new Canvas(gcWidth, gcHeight);
    GraphicsContext gc = canvas.getGraphicsContext2D();    
    //
    Label lb0 = new Label("");
    Button buttonPfx = new Button("List Assigned Prefixes");
    Button buttonRefresh = new Button("Refresh");
    Button buttonExit = new Button("Exit");
    TextArea txtaResults = new TextArea();
    //
    String prefix = "";
    String end = "";
    short pflen = 0;
    short parentpflen = 0;
    //
    public static int result = 0;
    BigInteger rangetotal = BigInteger.ZERO;
    //float angle = 0;
    double angle = 0;
    //float percent = 0;
    double percent = 0;
    //float filledh = 0;
    double filledh = 0;
    String assigned = "", available = "";
    //
    Boolean chks = false;
    static Connection MySQLconnection = null;
    DBServerInfo dbserverInfo = new DBServerInfo();
    Statement statement = null;
    ResultSet resultSet = null;

//</editor-fold>

    public StatsUsage(String prefix, String end, short parentpflen, short pflen, Boolean chks,
            Connection sqlcon, DBServerInfo servinfo) {

        this.prefix = prefix;
        this.end = end;
        this.pflen = pflen;
        this.parentpflen = parentpflen;
        this.chks = chks;
        MySQLconnection = sqlcon;
        this.dbserverInfo = servinfo;
        //
        this.lb0.setText("Range> " + prefix + "-" + pflen);
        //
        //
        //
        root.setCenter(this.addAnchorPane(addGrid()));
        root.setBottom(addStatusBar());
        stage.setResizable(false);
        stage.setTitle("IPv6 Subnet Calculator - Statistics/Utilization");
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
        //
        SettingsAndEvents();        
        Calculate();        
    }

    public void SettingsAndEvents() {
        lb0.setStyle("-fx-text-fill: royalblue; -fx-font-weight: bold;");
        lb0.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 12));
        buttonRefresh.setPrefWidth(60);
        buttonExit.setPrefWidth(60);
        //
        txtaResults.setEditable(false);
        txtaResults.setPrefWidth(400);
        txtaResults.setPrefHeight(140);
        txtaResults.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.NORMAL, 12));
        //
        buttonPfx.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent t) {
                IPv6SubnettingTool.UpdateDbStatus();
                if (MySQLconnection != null) {
                    if (listassigned == null) {
                        listassigned = new ListAssignedfromDB(prefix, end,
                                parentpflen, pflen, chks, MySQLconnection, dbserverInfo);
                        IPv6SubnettingTool.stageList.add(new StageList(listassigned.toString(),
                                listassigned.stage, listassigned.stage.hashCode()));
                        IPv6SubnettingTool.AddStageItem(listassigned.toString(), listassigned.stage.hashCode());
                    } else {
                        if (!listassigned.stage.isShowing()) {
                            IPv6SubnettingTool.stageList.add(new StageList(listassigned.toString(),
                                    listassigned.stage, listassigned.stage.hashCode()));
                            IPv6SubnettingTool.AddStageItem(listassigned.toString(), listassigned.stage.hashCode());
                        }
                        listassigned.SetNewValues(prefix, end, parentpflen, pflen, chks);
                        listassigned.StageShow();
                    }
                }
            }
        });
        buttonRefresh.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent t) {
                IPv6SubnettingTool.UpdateDbStatus();
                if (MySQLconnection != null) {
                    parentpflen = (short) IPv6SubnettingTool.sd1Value;
                    pflen = (short) IPv6SubnettingTool.sd2Value;
                    prefix = IPv6SubnettingTool.tfstartAddr.getText();
                    end = IPv6SubnettingTool.tfendAddr.getText();
                    lb0.setText("Range> " + prefix + "-" + pflen);
                    Calculate();
                }
            }
        });
        buttonExit.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent t) {
                IPv6SubnettingTool.RemoveStageItem(stage.hashCode());
                stage.close();
            }
        });
    }

    public int MySQLquery() {
        
        if (MySQLconnection == null) {
            MsgBox.Show(Alert.AlertType.NONE, "MySQLconnection is null!\r\nNo connection.");
            return -1;
        }

        int r = 0;
        String MySQLcmd = "";

        MySQLcmd = "SELECT COUNT(*) FROM "
                + dbserverInfo.Tablename
                + " WHERE ( prefix BETWEEN inet6_aton('" + this.prefix.split("/")[0] + "')"
                + " AND inet6_aton('" + this.end.split("/")[0] + "')"
                + " AND parentpflen= " + parentpflen + " AND pflen= " + pflen + " );";

        try {
            IPv6SubnettingTool.UpdateDbStatus();
            statement = MySQLconnection.createStatement();
            resultSet = statement.executeQuery(MySQLcmd);

            resultSet.next();
            r = Integer.parseInt(resultSet.getString(1));
            return r;
        }
        catch (NumberFormatException | SQLException ex) {
            MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
            IPv6SubnettingTool.UpdateDbStatus();
            return -1;
        }
    }

    public void Calculate() {

        result = MySQLquery();
        if (result < 0) {
            return;
        }
        
        rangetotal = BigInteger.ONE.shiftLeft(pflen - parentpflen);
        double ratio = ((double) result / rangetotal.doubleValue());
        percent = ratio * 100;
        angle = ratio * 360;
        filledh = ratio * 120;
        
        assigned = ""; available = "";
        if (ratio == 0 || ratio > 0.01) {
            assigned = String.format(Locale.ROOT, "%.2f", percent);
            available = String.format(Locale.ROOT, "%1$.2f", (100 - Double.parseDouble(assigned)));
        } else {
            assigned = String.format(Locale.ROOT, "%.5f", percent);
            available = String.format(Locale.ROOT, "%1$.5f", (100 - Double.parseDouble(assigned)));
        }

        assigned += "% Assigned";
        available += "% Available";

        UpdateTextBox();
        StatsUsagePaint();
    }

    public void StatsUsagePaint() {
        gc.clearRect(0, 0, gcWidth, gcHeight);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, gcWidth, gcWidth);        
        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        //
        gc.strokeRect(30, 16, 20, 120);
        gc.strokeOval(90, 15, 120, 120);
        gc.setFill(Color.RED);
        gc.fillArc(90, 15, 120, 120, 0, this.angle, ArcType.ROUND);
        //
        gc.setStroke(Color.ROYALBLUE);
        gc.setLineWidth(1);
        gc.strokeLine(151, 75, 209, 75);
        //
        gc.fillRect(30, (135 - this.filledh), 20, this.filledh);
        //
        gc.setLineWidth(2);
        gc.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 14));
        gc.fillText(this.assigned, 220, 65);
        gc.setFill(Color.BLACK);
        gc.fillText(this.available, 220, 95);
        //

    }

    public void UpdateTextBox() {
        txtaResults.setText("\r\n" + lb0.getText() + "\r\n\r\n");
        txtaResults.setText(txtaResults.getText() + "Total Prefixes: \t"
                + String.valueOf(rangetotal) + "\r\n");
        txtaResults.setText(txtaResults.getText() + "Assigned Prefixes: \t"
                + String.valueOf(result) + "\r\n");
        txtaResults.setText(txtaResults.getText() + "Available Prefixes: \t"
                + String.valueOf(rangetotal.subtract(BigInteger.valueOf(result))) + "\r\n");
        txtaResults.setText(txtaResults.getText() + "Assigned Percentage: \t"
                + String.valueOf(assigned.split("%")[0] + "%" + "\r\n"));
        txtaResults.setText(txtaResults.getText() + "Available Percentage: \t"
                + String.valueOf(available.split("%")[0] + "%"));
    }

    public GridPane addGrid() {
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setAlignment(Pos.BASELINE_CENTER);
        grid.setPadding(new Insets(5, 10, 0, 10));
        //
        grid.add(lb0, 0, 0);
        grid.add(canvas, 0, 1);
        grid.add(new Label(""), 0, 2);
        //
        HBox hbbuttons = new HBox(10);
        hbbuttons.setAlignment(Pos.CENTER_LEFT);
        hbbuttons.getChildren().addAll(buttonPfx, buttonRefresh, buttonExit);
        grid.add(hbbuttons, 0, 3);
        //
        grid.add(txtaResults, 0, 4);
        
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
        hbstatus.setAlignment(Pos.CENTER_RIGHT);
        lbdbstatus.setStyle("-fx-text-fill: royalblue;");
        hbstatus.getChildren().addAll(lbdbstatus);
        return hbstatus;
    }

    public void StageShow() {
        stage.show();
        stage.toFront();
        if (stage.isIconified()) {
            stage.setIconified(false);
        }
    }

}
    