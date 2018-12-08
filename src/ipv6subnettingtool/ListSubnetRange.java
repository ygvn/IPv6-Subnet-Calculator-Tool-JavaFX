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

import java.math.BigInteger;
import java.sql.Connection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
public class ListSubnetRange {
    
//<editor-fold defaultstate="collapsed" desc="special initials/constants -Yücel">
    public final int ID = 1; // ID of this Form.
    public int incomingID;
    //
    Stage stage = new Stage();
    BorderPane root = new BorderPane();
    Scene scene = new Scene(root, 420, 470);
    GridPane grid = new GridPane();
    //
    Label r0 = new Label("Range:");
    Label r1 = new Label("s> ");
    Label r2 = new Label("e> ");
    //
    Button buttonFirstPage = new Button("|<< FirstPage");
    Button buttonBack = new Button("<<");
    Button buttonFwd = new Button(">>");
    Button buttonLast = new Button(">>|");
    //
    ListView<String> prefixlist = new ListView<String>();
    //
    Label lbtotal = new Label("Total:");
    Label lbGoto = new Label("Goto:");
    TextField textTotal = new TextField("");
    TextField textGoto = new TextField("0");
    Button buttonGoto = new Button(" >>Go ");
    //
    SEaddress StartEnd = new SEaddress();
    short parentpflen = 0;
    SEaddress subnets = new SEaddress();
    SEaddress page = new SEaddress();
    final int upto = 128;

    BigInteger currentidx = BigInteger.ZERO;
    BigInteger pix = BigInteger.ZERO;
    public String findpfx = "";
    public String GotoForm_PrevValue = "";
    public BigInteger NumberOfSubnets = BigInteger.ZERO;
    BigInteger gotovalue = BigInteger.ZERO;
    BigInteger maxvalue = BigInteger.ZERO;
    Boolean is128Checked;
    int maxfontwidth = 0;
    //
    Label listcount = new Label("");
    HBox hblistcount = new HBox();
    //
    final ContextMenu contextMenu = new ContextMenu();
    final Clipboard clipboard = Clipboard.getSystemClipboard();
    final ClipboardContent content = new ClipboardContent();
    //
    static Connection MySQLconnection = null;
    DBServerInfo dbserverInfo = null;
    //
    Label statusString = new Label("");
    public static Label lbdbstatus = new Label("db=Down ");
    public void Setlbdbstatus(String s) {
        this.lbdbstatus.setText(s);
    }
    HBox hbstatusstr = new HBox();
    HBox hbdbstatus = new HBox();
    //
    SaveAsTxt saveas = null;
    ListDnsReverses dnsr = null;
    DatabaseUI dbUIsend = null;
    //
//</editor-fold>
    
    public ListSubnetRange(SEaddress input, String sin, Boolean is128Checked,
            Connection sqlcon, DBServerInfo dbsinfo) {

        root.setCenter(this.addAnchorPane(addGrid()));
        root.setBottom(addStatusBar());
        stage.setResizable(false);
        stage.setTitle("IPv6 Subnet Calculator - List Subnet Range");
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
        MySQLconnection = sqlcon;
        dbserverInfo = dbsinfo;
        SetNewValues(input, sin, is128Checked);
        //
    }

    public void SetNewValues(SEaddress input, String sin, Boolean is128Chked) {
        
        prefixlist.getItems().clear();
        parentpflen = (short) input.slash;
        this.StartEnd.ID = this.ID;
        this.incomingID = input.ID;
        this.is128Checked = is128Chked;
        this.buttonBack.setDisable(true);
        this.buttonFwd.setDisable(true);
        this.buttonLast.setDisable(true);
        //
        String[] sa = sin.split(" ");
        sa = sa[1].split("/");
        StartEnd.Resultv6 = v6ST.FormalizeAddr(sa[0]);
        StartEnd.slash = input.subnetslash;
        StartEnd.subnetslash = input.subnetslash;

        StartEnd = v6ST.StartEndAddresses(StartEnd, this.is128Checked);
        NumberOfSubnets = (StartEnd.End.subtract(StartEnd.Start)).add(BigInteger.ONE);

        String s1 = v6ST.Kolonlar(StartEnd.Start);
        String s2 = v6ST.Kolonlar(StartEnd.End);

        if (!is128Checked) {
            DefaultStage();
            s1 = s1.substring(0, 19) + "::";
            s1 = v6ST.CompressAddress(s1);
            s2 = s2.substring(0, 19) + "::";
            s2 = v6ST.CompressAddress(s2);
            r1.setText("s> " + s1 + "/" + StartEnd.subnetslash);
            r2.setText("e> " + s2 + "/" + StartEnd.subnetslash);
            //
            NumberOfSubnets = NumberOfSubnets.shiftRight(64);
            if (NumberOfSubnets.equals(BigInteger.ZERO)) {
                NumberOfSubnets = BigInteger.ONE;
            }
            textTotal.setText(String.valueOf(NumberOfSubnets));
        }
        else if (is128Checked) {
            ExpandStage();
            s1 = v6ST.CompressAddress(s1);
            s2 = v6ST.CompressAddress(s2);
            r1.setText("s> " + s1 + "/" + StartEnd.subnetslash);
            r2.setText("e> " + s2 + "/" + StartEnd.subnetslash);
            textTotal.setText(String.valueOf(NumberOfSubnets));
        }
        buttonFirstPage.fire();
    }
    
    public void SettingsAndEvents() {

        r0.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 12));
        r1.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 12));
        r2.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 12));
        //
        buttonFirstPage.setPrefWidth(90);
        buttonBack.setPrefWidth(40);
        buttonFwd.setPrefWidth(40);
        buttonLast.setPrefWidth(40);
        //
        prefixlist.setContextMenu(contextMenu);
        prefixlist.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);        
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
        //
        prefixlist.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if (key.getCode() == KeyCode.ESCAPE) {
                    IPv6SubnettingTool.RemoveStageItem(stage.hashCode());
                    stage.close();
                }
            }
        });
        //
        listcount.setStyle("-fx-text-fill: royalblue");
        hblistcount.setPrefWidth(173);
        //
        textTotal.setEditable(false);
        textTotal.setPrefWidth(270);
        textGoto.setPrefWidth(270);
        //
        buttonFirstPage.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                subnets.Start = page.Start = StartEnd.Start;
                page.End = BigInteger.ZERO;
                subnets.subnetslash = StartEnd.subnetslash;
                subnets.upto = upto;

                subnets.LowerLimitAddress = StartEnd.LowerLimitAddress;
                subnets.UpperLimitAddress = StartEnd.UpperLimitAddress;

                if (subnets.Start.equals(StartEnd.End)) {
                    UpdateCount();
                    return;
                }

                prefixlist.getItems().clear();
                subnets = v6ST.ListSubRangeFirstPage(subnets, is128Checked);
                if (is128Checked)
                    page.End = subnets.Start.subtract(BigInteger.ONE);
                else if (!is128Checked)
                    page.End = subnets.Start.subtract(BigInteger.ONE.shiftLeft(64));
                
                prefixlist.setItems(subnets.liste);

                if (NumberOfSubnets.compareTo(BigInteger.valueOf(upto)) <= 0) {
                    buttonBack.setDisable(true);
                    buttonFwd.setDisable(true);
                    buttonLast.setDisable(true);
                } else {
                    buttonBack.setDisable(true);
                    buttonFwd.setDisable(false);
                    buttonLast.setDisable(false);
                }

                UpdateCount();
            }
        });

        buttonBack.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                if (is128Checked)
                    subnets.Start = page.End = page.Start.subtract(BigInteger.ONE);
                else if (!is128Checked)
                    subnets.Start = page.End = page.Start.subtract(BigInteger.ONE.shiftLeft(64));
                
                subnets.subnetslash = StartEnd.subnetslash;
                subnets.upto = upto;

                subnets.LowerLimitAddress = StartEnd.LowerLimitAddress;
                subnets.UpperLimitAddress = StartEnd.UpperLimitAddress;

                prefixlist.getItems().clear();

                subnets = v6ST.ListSubRangePageBackward(subnets, is128Checked);
                if (is128Checked)
                    page.Start = subnets.Start.add(BigInteger.ONE);
                else if (!is128Checked)
                    page.Start = subnets.Start.add(BigInteger.ONE.shiftLeft(64));

                prefixlist.setItems(subnets.liste);

                if (subnets.subnetidx.equals(BigInteger.ZERO)) {
                    buttonBack.setDisable(true);
                    buttonFwd.setDisable(false);
                    buttonLast.setDisable(false);
                } else {
                    buttonFwd.setDisable(false);
                    buttonLast.setDisable(false);
                }
                UpdateCount();
            }
        });

        buttonFwd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (is128Checked)
                    subnets.Start = page.Start = page.End.add(BigInteger.ONE);
                else if (!is128Checked)
                    subnets.Start = page.Start = page.End.add(BigInteger.ONE.shiftLeft(64));
                
                subnets.subnetslash = StartEnd.subnetslash;
                subnets.upto = upto;
                subnets.UpperLimitAddress = StartEnd.UpperLimitAddress;
                subnets.LowerLimitAddress = StartEnd.LowerLimitAddress;

                prefixlist.getItems().clear();

                subnets = v6ST.ListSubRangePageForward(subnets, is128Checked);

                if (is128Checked) {
                    page.End = subnets.Start.subtract(BigInteger.ONE);
                }
                else if (!is128Checked) {
                    page.End = subnets.Start.subtract(BigInteger.ONE.shiftLeft(64));
                }

                prefixlist.setItems(subnets.liste);

                if (subnets.subnetidx.equals(NumberOfSubnets.subtract(BigInteger.ONE))) {
                    buttonBack.setDisable(false);
                    buttonFwd.setDisable(true);
                    buttonLast.setDisable(true);                    
                    UpdateCount();
                    return;
                } else {
                    buttonBack.setDisable(false);
                    buttonLast.setDisable(false);
                }
                UpdateCount();
            }
        });

        buttonLast.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

            subnets.Start = page.End = StartEnd.End;
            subnets.subnetslash = StartEnd.subnetslash;
            subnets.upto = upto;
            subnets.LowerLimitAddress = StartEnd.LowerLimitAddress;
            subnets.UpperLimitAddress = StartEnd.UpperLimitAddress;

            prefixlist.getItems().clear();
            
            subnets = v6ST.ListSubRangeLastPage(subnets, is128Checked);
            
            prefixlist.setItems(subnets.liste);
            
            if (is128Checked)
                page.Start = subnets.Start.add(BigInteger.ONE);
            else if (!is128Checked)
                page.Start = subnets.Start.add(BigInteger.ONE.shiftLeft(64));

            if (NumberOfSubnets.compareTo(BigInteger.valueOf(upto)) > 0)
            {
                buttonBack.setDisable(false);
                buttonFwd.setDisable(true);
                buttonLast.setDisable(true);
            }
            else
            {
                buttonBack.setDisable(true);
                buttonFwd.setDisable(true);
                buttonLast.setDisable(true);
            }

            UpdateCount();
            }
        });

        buttonGoto.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
            textGoto.setText(textGoto.getText().trim());
            
            if (textGoto.getText().equals(""))
            {
                textGoto.setText("0");
                return;
            }

            gotovalue = new BigInteger(textGoto.getText(), 10);

            if (gotovalue.compareTo(NumberOfSubnets.subtract(BigInteger.ONE)) > 0)
            {
                UpdateCount();
                textGoto.setText(String.valueOf(NumberOfSubnets.subtract(BigInteger.ONE)));
                return;
            }

            String ss = "";
            int count = 0;
            ObservableList<String> liste = FXCollections.observableArrayList();

            subnets.slash = StartEnd.slash;
            subnets.subnetslash = StartEnd.subnetslash;
            subnets.Start = StartEnd.Start;
            
            if (is128Checked)
                subnets.Start = subnets.Start.add(gotovalue);
            else if (!is128Checked)
                subnets.Start = subnets.Start.add(gotovalue.shiftLeft(64));
            
            if (subnets.Start.equals(StartEnd.Start)) {
                buttonBack.setDisable(true);
            } else {
                buttonBack.setDisable(false);
            }

            page.Start = subnets.Start;
            page.End = BigInteger.ZERO;

            prefixlist.getItems().clear();

            for (count = 0; count < upto; count++) {
                subnets = v6ST.RangeIndex(subnets, is128Checked);
                if (is128Checked)
                {
                    ss = v6ST.Kolonlar(subnets.Start);
                    ss = v6ST.CompressAddress(ss);
                    ss = "p" + subnets.subnetidx + "> " + ss + "/128";
                }
                else if (!is128Checked)
                {
                    ss = v6ST.Kolonlar(subnets.Start);
                    ss = ss.substring(0, 19); ss += "::";
                    ss = v6ST.CompressAddress(ss);
                    ss = "p" + subnets.subnetidx + "> " + ss + "/64";
                }
                liste.add(ss);
                
                if ( subnets.subnetidx.equals(NumberOfSubnets.subtract(BigInteger.ONE))
                        || subnets.Start.equals(StartEnd.End))
                {
                    break;
                }
                else
                {
                    if (is128Checked)
                        subnets.Start = subnets.Start.add(BigInteger.ONE);
                    else if (!is128Checked)
                        subnets.Start = subnets.Start.add(BigInteger.ONE.shiftLeft(64));
                }
            }
            if (is128Checked) {
                page.End = subnets.Start.subtract(BigInteger.ONE);
            }
            else if (!is128Checked) {
                page.End = subnets.Start.subtract(BigInteger.ONE.shiftLeft(64));
            }
            
            prefixlist.setItems(liste);

            if (count > (upto - 1))
            {
                buttonFwd.setDisable(false);
                buttonLast.setDisable(false);
            }
            else
            {
                buttonFwd.setDisable(true);
                buttonLast.setDisable(true);
            }
            UpdateCount();
            }
        });
        //
        textGoto.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                    String newValue) {
                if (!newValue.matches("\\d*")) { // only digits
                    textGoto.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        //
        contextMenu.setOnShowing(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent e) {

            }
        });
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
        //
        MenuItem contextListDNSRevs = new MenuItem("List All DNS reverse zones");
        contextListDNSRevs.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (!prefixlist.getItems().isEmpty()) {
                    
                    int tmp = StartEnd.subnetslash;
                    if (!is128Checked) {
                        StartEnd.subnetslash = 64;
                    } else if (is128Checked) {
                        StartEnd.subnetslash = 128;
                    }
                    if (dnsr == null) {
                        dnsr = new ListDnsReverses(StartEnd, is128Checked);
                        IPv6SubnettingTool.stageList.add(new StageList(dnsr.toString(), dnsr.stage, dnsr.stage.hashCode()));
                        IPv6SubnettingTool.AddStageItem(dnsr.toString(), dnsr.stage.hashCode());
                    } else {
                        if (!dnsr.stage.isShowing()) {
                            IPv6SubnettingTool.stageList.add(new StageList(dnsr.toString(), dnsr.stage, dnsr.stage.hashCode()));
                            IPv6SubnettingTool.AddStageItem(dnsr.toString(), dnsr.stage.hashCode());
                        }
                        dnsr.SetNewValues(StartEnd, is128Checked);
                        dnsr.StageShow();
                    }
                    StartEnd.subnetslash = tmp;
                }
            }
        });        
        //
        MenuItem contextSendtoDb = new MenuItem("Send prefix to database...");
        contextSendtoDb.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                IPv6SubnettingTool.UpdateDbStatus();
                if (MySQLconnection != null) {
                    if (prefixlist.getSelectionModel().getSelectedItem() != null) {
                        String selected = prefixlist.getSelectionModel().getSelectedItem().split(" ")[1].trim();
                        String snet = selected.split("/")[0].trim();
                        short plen = Short.parseShort(selected.split("/")[1].trim());
                        if (dbUIsend == null) {
                            dbUIsend = new DatabaseUI(snet, plen, parentpflen, MySQLconnection, dbserverInfo);
                            IPv6SubnettingTool.stageList.add(new StageList(dbUIsend.toString(),
                                    dbUIsend.stage, dbUIsend.stage.hashCode()));
                            IPv6SubnettingTool.AddStageItem(dbUIsend.toString(), dbUIsend.stage.hashCode());
                        } else {
                            if (!dbUIsend.stage.isShowing()) {
                                IPv6SubnettingTool.stageList.add(new StageList(dbUIsend.toString(),
                                        dbUIsend.stage, dbUIsend.stage.hashCode()));
                                IPv6SubnettingTool.AddStageItem(dbUIsend.toString(), dbUIsend.stage.hashCode());
                            }
                            dbUIsend.buttonClear.fire();
                            dbUIsend.SetNewValues(snet, plen, parentpflen);
                            dbUIsend.StageShow();
                        }
                    }
                }
            }
        });
        //
        MenuItem contextSaveas = new MenuItem("Save As Text...");
        contextSaveas.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (saveas == null) {
                    saveas = new SaveAsTxt(StartEnd, is128Checked);
                    IPv6SubnettingTool.stageList.add(new StageList(saveas.toString(), saveas.stage, saveas.stage.hashCode()));
                    IPv6SubnettingTool.AddStageItem(saveas.toString(), saveas.stage.hashCode());
                } else {
                    if (!saveas.stage.isShowing()) {
                        IPv6SubnettingTool.stageList.add(new StageList(saveas.toString(), saveas.stage, saveas.stage.hashCode()));
                        IPv6SubnettingTool.AddStageItem(saveas.toString(), saveas.stage.hashCode());
                    }
                    saveas.SetNewValues(StartEnd, is128Checked);
                    saveas.StageShow();
                }
            }
        });
        //
        contextMenu.getItems().addAll(contextitemSelectall, 
                contextitemCopy, contextListDNSRevs, contextSendtoDb, new SeparatorMenuItem(), contextSaveas);
        
    }//END

    public void UpdateCount() {
        if (prefixlist.getItems().isEmpty()) {
            listcount.setVisible(false);
        }
        else {
            listcount.setVisible(true);
            listcount.setText("[" + String.valueOf(prefixlist.getItems().size()) + " ent.]");
        }
    }    
    
    public void DefaultStage() {
        stage.setWidth(425);
        stage.setHeight(470);
        prefixlist.setPrefWidth(400);
        prefixlist.setPrefHeight(260);
        hblistcount.setPrefWidth(173);
        hbstatusstr.setPrefWidth(300);
        hbdbstatus.setPrefWidth(110);
    }

    public void ExpandStage() {
        stage.setWidth(625);
        stage.setHeight(470);
        prefixlist.setPrefWidth(600);
        prefixlist.setPrefHeight(260);
        hblistcount.setPrefWidth(373);
        hbstatusstr.setPrefWidth(500);
        hbdbstatus.setPrefWidth(110);
    }    
    
    public GridPane addGrid() {
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setAlignment(Pos.BASELINE_CENTER);
        grid.setPadding(new Insets(0, 10, 0, 10));
        //
        HBox hbr0 = new HBox();
        hbr0.setAlignment(Pos.CENTER_LEFT);
        hbr0.getChildren().addAll(r0, r1);
        grid.add(hbr0, 0, 0);
        grid.add(r1, 1, 0);
        grid.add(r2, 1, 1);
        //
        hblistcount.getChildren().add(listcount);
        hblistcount.setAlignment(Pos.BOTTOM_RIGHT);
        //
        HBox btns = new HBox(5);
        btns.getChildren().addAll(buttonFirstPage, buttonBack, buttonFwd, buttonLast, hblistcount);
        grid.add(btns, 0, 3, 2, 1);
        //
        grid.add(prefixlist, 0, 4, 2, 1);
        //
        HBox hb0 = new HBox(5);
        hb0.setAlignment(Pos.CENTER_LEFT);
        hb0.getChildren().addAll(lbtotal, textTotal);
        grid.add(hb0, 0, 5, 2, 1);
        //
        HBox hb1 = new HBox(5);
        hb1.setAlignment(Pos.CENTER_LEFT);
        hb1.getChildren().addAll(lbGoto, textGoto, buttonGoto);
        grid.add(hb1, 0, 6, 2, 1);
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

        hbstatusstr.setAlignment(Pos.CENTER_LEFT);
        statusString.setStyle("-fx-text-fill: royalblue;");
        hbstatusstr.getChildren().add(statusString);
        //
        lbdbstatus.setStyle("-fx-text-fill: royalblue;");
        hbdbstatus.setAlignment(Pos.CENTER_RIGHT);
        //
        hbdbstatus.getChildren().add(lbdbstatus);
        hbstatus.getChildren().addAll(hbstatusstr, hbdbstatus);
        return hbstatus;
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
