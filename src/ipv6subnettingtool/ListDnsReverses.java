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

import java.math.BigInteger;
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
public final class ListDnsReverses {
    
    //<editor-fold defaultstate="collapsed" desc="special initials/constants -Yücel">
    public final int ID = 2; // ID of this Form.
    public int incomingID;
    SEaddress StartEnd = new SEaddress();
    SEaddress subnets = new SEaddress();
    SEaddress page = new SEaddress();
    public int upto = 128;
    public final String arpa = "ip6.arpa.";
    BigInteger NumberOfZones = BigInteger.ZERO;
    public BigInteger zmaxval = BigInteger.ZERO;
    public Boolean is128Checked;
    //
    Stage stage = new Stage();
    BorderPane root = new BorderPane();
    Scene scene = new Scene(root, 420, 420);
    GridPane grid = new GridPane();
    //
    Label r0 = new Label("Range:");
    Label r1 = new Label("s> ");
    Label r2 = new Label("e> ");
    Label nibble = new Label(" Non-Nibble boundary! ");
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
    TextField textTotal = new TextField("0");
    TextField textGoto = new TextField("0");
    Button buttonGoto = new Button(" >>Go ");
    //
    Label listcount = new Label("");
    HBox hblistcount = new HBox();
    //
    final ContextMenu contextMenu = new ContextMenu();
    final Clipboard clipboard = Clipboard.getSystemClipboard();
    final ClipboardContent content = new ClipboardContent();
    //
    SaveAsTxt saveas = null;
    //</editor-fold>

    public ListDnsReverses(SEaddress input, Boolean is128Checked) {
        this.StartEnd.ID = ID;
        //
        root.setCenter(this.addAnchorPane(addGrid()));
        stage.setResizable(false);
        stage.setTitle("IPv6 Subnet Calculator - List DNS Reverse Zones");
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
        SettingsAndEvents();        
        //
        SetNewValues(input, is128Checked);
    }

    public void SetNewValues(SEaddress input, Boolean is128Checked) {
        
        prefixlist.getItems().clear();
        textTotal.clear();
        textGoto.clear();
        this.incomingID = input.ID;
        this.is128Checked = is128Checked;
        this.buttonBack.setDisable(true);
        this.buttonFwd.setDisable(true);
        this.buttonLast.setDisable(true);
        //
        if (input.subnetslash % 4 != 0)
            nibble.setVisible(true);
        else
            nibble.setVisible(false);
        //
        StartEnd.Start = input.Start;
        StartEnd.End = input.End;
        StartEnd.Resultv6 = input.Resultv6;
        StartEnd.LowerLimitAddress = input.LowerLimitAddress;
        StartEnd.UpperLimitAddress = input.UpperLimitAddress;
        StartEnd.upto = upto;
        StartEnd.slash = input.slash;
        StartEnd.subnetslash = input.subnetslash;
        StartEnd.subnetidx = input.subnetidx;

        subnets.Start = input.Start;
        subnets.End = input.End;
        subnets.slash = input.slash;
        subnets.subnetslash = input.subnetslash;
        subnets.LowerLimitAddress = input.LowerLimitAddress;
        subnets.UpperLimitAddress = input.UpperLimitAddress;

        BigInteger max = NumberOfZones = 
                BigInteger.ONE.shiftLeft(input.subnetslash - input.slash);
        zmaxval = max.subtract(BigInteger.ONE);
        textTotal.setText(String.valueOf(NumberOfZones));
        

        if (!is128Checked) {
            DefaultStage();
            String s = v6ST.Kolonlar(StartEnd.Start);
            s = s.substring(0, 19) + "::";
            s = v6ST.CompressAddress(s);
            r1.setText("s> " + s + "/" + String.valueOf(StartEnd.subnetslash));
            //
            s = v6ST.Kolonlar(StartEnd.End);
            s = s.substring(0, 19) + "::";
            s = v6ST.CompressAddress(s);
            r2.setText("e> " + s + "/" + String.valueOf(StartEnd.subnetslash));
        } else if (is128Checked) {
            ExpandStage();
            String s = v6ST.CompressAddress(v6ST.Kolonlar(StartEnd.Start));
            r1.setText("s> " + s + "/" + String.valueOf(StartEnd.subnetslash));
            s = v6ST.CompressAddress(v6ST.Kolonlar(StartEnd.End));
            r2.setText("e> " + s + "/" + String.valueOf(StartEnd.subnetslash));
        }
        buttonFirstPage.fire();
    }
    
    public void UpdateCount() {
        if (prefixlist.getItems().isEmpty()) {
            listcount.setVisible(false);
        }
        else {
            if (StartEnd.subnetslash % 4 == 0) {
                listcount.setText("[" + String.valueOf(prefixlist.getItems().size()) + " ent.]");
            }
            else {
                listcount.setVisible(true);
                int remainder = StartEnd.subnetslash % 4;
                int nzones = (1 << (4 - remainder));
                listcount.setText("[" + String.valueOf(prefixlist.getItems().size() / nzones) + " ent.]");
            }
        }
    }    
    
    public void SettingsAndEvents() {
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
                subnets.End = page.End = BigInteger.ZERO;
                subnets.subnetidx = BigInteger.ZERO;
                subnets.slash = StartEnd.slash;
                subnets.subnetslash = StartEnd.subnetslash;

                subnets.upto = upto;
                subnets.UpperLimitAddress = StartEnd.UpperLimitAddress;
                subnets.LowerLimitAddress = StartEnd.LowerLimitAddress;

                if (subnets.End.equals(StartEnd.End)) {
                    UpdateCount();
                    return;
                }

                prefixlist.getItems().clear();

                String[] sa;

                if ((!is128Checked && StartEnd.slash == 64)
                        || (is128Checked && StartEnd.slash == 128)) {
                    subnets.Start = StartEnd.Resultv6;
                    sa = v6ST.DnsRev(subnets.Start, subnets.subnetslash, is128Checked);
                    prefixlist.getItems().add("p0> " + sa[0]);
                    UpdateCount();
                    return;
                }

                subnets = v6ST.ListDnsRevFirstPage(subnets, is128Checked);
                prefixlist.setItems(subnets.liste);
                page.End = subnets.End;

                if (NumberOfZones.compareTo(BigInteger.valueOf(upto)) <= 0) {
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
            subnets.upto = upto;
            subnets.LowerLimitAddress = StartEnd.LowerLimitAddress;
            subnets.UpperLimitAddress = StartEnd.UpperLimitAddress;
            
            prefixlist.getItems().clear();
            
            subnets.End = page.End = page.Start.subtract(BigInteger.ONE);
            subnets = v6ST.ListDnsRevPageBackward(subnets, is128Checked);
            page.Start = subnets.Start;            

            prefixlist.setItems(subnets.liste);
            
            if (subnets.Start.equals(StartEnd.Start))
            {
                buttonBack.setDisable(true);
                buttonFwd.setDisable(false);
                buttonLast.setDisable(false);

                UpdateCount();
                return;
            }
            else
            {
                buttonFwd.setDisable(false);
                buttonLast.setDisable(false);
            }
            UpdateCount();
            }
        });

        buttonFwd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            subnets.upto = upto;
            subnets.LowerLimitAddress = StartEnd.LowerLimitAddress;
            subnets.UpperLimitAddress = StartEnd.UpperLimitAddress;

            subnets.Start = page.Start = page.End.add(BigInteger.ONE);

            prefixlist.getItems().clear();

            subnets = v6ST.ListDnsRevPageForward(subnets, is128Checked);
            page.End = subnets.End;

            prefixlist.setItems(subnets.liste);

            if (subnets.End.equals(StartEnd.End))
            {
                buttonBack.setDisable(false);
                buttonFwd.setDisable(true);
                buttonLast.setDisable(true);
                
                UpdateCount();
                return;
            }
            else
            {
                buttonBack.setDisable(false);
                buttonLast.setDisable(false);
            }
            UpdateCount();
            }
        });

        buttonLast.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            subnets.upto = upto;
            subnets.LowerLimitAddress = StartEnd.LowerLimitAddress;
            subnets.UpperLimitAddress = StartEnd.UpperLimitAddress;

            prefixlist.getItems().clear();

            subnets.subnetidx = BigInteger.ZERO;
            subnets.End = page.End = StartEnd.End;
            subnets = v6ST.ListDnsRevLastPage(subnets, is128Checked);
            page.Start = subnets.Start;

            prefixlist.setItems(subnets.liste);

                if (subnets.subnetidx.equals(BigInteger.ZERO)) {
                    buttonBack.setDisable(true);
                } else {
                    buttonBack.setDisable(false);
                }

            buttonFwd.setDisable(true);
            buttonLast.setDisable(true);
            UpdateCount();
            }
        });
        
        buttonGoto.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent t) {
                String[] sa;
                ObservableList<String> liste = FXCollections.observableArrayList();
                int count = 0;
                int spaces = 0;

                if (textGoto.getText().trim().equals("")) {
                    return;
                }
                
                BigInteger newidx = new BigInteger(textGoto.getText().trim());
                if (newidx.compareTo(zmaxval) > 0) {
                    textGoto.setText(String.valueOf(zmaxval));
                    return;
                }
                else if (zmaxval.equals(BigInteger.ZERO)) {
                    return;
                }

                subnets.subnetidx = newidx;
                subnets.slash = StartEnd.slash;
                subnets.subnetslash = StartEnd.subnetslash;
                subnets.Start = StartEnd.Start;
                subnets.Resultv6 = StartEnd.Resultv6;

                subnets = v6ST.GoToSubnet(subnets, is128Checked);

                page.Start = subnets.Start;
                page.End = BigInteger.ZERO;

                if (subnets.End.equals(StartEnd.End)) {
                    buttonFwd.setDisable(true);
                }

                prefixlist.getItems().clear();

                for (count = 0; count < upto; count++) {
                    subnets = v6ST.Subnetting(subnets, is128Checked);

                    sa = v6ST.DnsRev(subnets.Start, subnets.subnetslash, is128Checked);
                    String sf = "p" + subnets.subnetidx + "> " + sa[0];
                    liste.add(sf);

                    String[] sr = sf.split(" ");
                    spaces = sr[0].length() + 1;

                    for (int i = 1; i < 8; i++) {
                        if (sa[i] == null) {
                            break;
                        }
                        sa[i] = String.format("%1$" + Integer.valueOf(sa[i].length() 
                                + spaces) + "s", sa[i]);
                        liste.add(sa[i]);
                    }

                    if (subnets.End.equals(StartEnd.End)) {
                        buttonFwd.setDisable(true);
                        break;
                    } else {
                        if (is128Checked)
                            subnets.Start = subnets.End.add(BigInteger.ONE);
                        else if (!is128Checked)
                            subnets.Start = subnets.End.add(BigInteger.ONE.shiftLeft(64));
                    }
                }
                prefixlist.setItems(liste);

                page.End = subnets.End;
                if (newidx.equals(BigInteger.ZERO)) {
                    buttonBack.setDisable(true);
                } else {
                    buttonBack.setDisable(false);
                }
                if (subnets.subnetidx.equals(zmaxval)) {
                    buttonFwd.setDisable(true);
                    buttonLast.setDisable(true);
                } else {
                    buttonFwd.setDisable(false);
                    buttonLast.setDisable(false);
                }
                UpdateCount();
            }
        });
        
        textGoto.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                    String newValue) {
                if (!newValue.matches("\\d*")) { // only digits
                    textGoto.setText(newValue.replaceAll("[^\\d]", ""));
                }
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

                String tmp = "";
                for (String s : prefixlist.getSelectionModel().getSelectedItems()) {
                    tmp += s + "\r\n";
                }
                content.putString(tmp);
                clipboard.setContent(content);
            }
        });
        MenuItem contextSaveas = new MenuItem("Save As Text...");
        contextSaveas.setOnAction(new EventHandler<ActionEvent>() {
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
        contextMenu.getItems().addAll(contextitemSelectall,
                contextitemCopy, new SeparatorMenuItem(), contextSaveas);

    }//END

    public void DefaultStage() {
        stage.setWidth(425);
        stage.setHeight(450);
        prefixlist.setPrefWidth(400);
        prefixlist.setPrefHeight(260);
        hblistcount.setPrefWidth(173);
    }
    
    public void ExpandStage() {
        stage.setWidth(625);
        stage.setHeight(450);
        prefixlist.setPrefWidth(600);
        prefixlist.setPrefHeight(260);
        hblistcount.setPrefWidth(373);
    }
    
    public GridPane addGrid() {
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setAlignment(Pos.BASELINE_CENTER);
        grid.setPadding(new Insets(0, 10, 0, 10));
        //
        r0.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 12));
        HBox hbr0 = new HBox();
        hbr0.setAlignment(Pos.CENTER_LEFT);
        hbr0.getChildren().add(r0);
        grid.add(hbr0, 0, 0);
        r1.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 12));        
        HBox hbnibble = new HBox(15);
        hbnibble.setAlignment(Pos.CENTER_LEFT);
        nibble.setStyle("-fx-text-fill: #ff0000; -fx-border-color: #d3d3d3;");
        hbnibble.getChildren().addAll(r1, nibble);
        grid.add(hbnibble, 1, 0);
        r2.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 12));
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
    
    public void StageShow() {
        stage.show();
        stage.toFront();
        if (stage.isIconified()) {
            stage.setIconified(false);
        }
        stage.centerOnScreen();
    }    
    
} //END class
