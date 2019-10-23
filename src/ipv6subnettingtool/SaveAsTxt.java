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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Yucel Guven
 */
public class SaveAsTxt {

    final int ID = 3; // ID of this Form.
    int incomingID;

    public class CurrentState {

        public BigInteger SavedLines = BigInteger.ZERO;
        public int percentage = 0;
    }

    //<editor-fold defaultstate="collapsed" desc="special initials/constants -yucel">
    CurrentState saveState = new CurrentState();
    Stage stage = new Stage();
    BorderPane root = new BorderPane();
    Scene scene = new Scene(root, 410, 420);
    GridPane grid = new GridPane();
    final ProgressBar bar = new ProgressBar(0);
    FirstLineService service = new FirstLineService();
    Label labelPercent = new Label(""); // CLOSED ! "0%"
    Button buttonSave = new Button("Save...");
    Button buttonCancel = new Button("Cancel");
    Button buttonExit = new Button("Exit");
    Label labelStatus = new Label();
    String FileName = "";
    String ss = "", se = "";
    //
    Label r0 = new Label("Range:");
    Label r1 = new Label("s> ");
    Label r2 = new Label("e> ");
    Label maxIndex = new Label("Max.Index#:");
    TextField textmaxIndexNo = new TextField("");
    Label lbfromIndex = new Label("From index:");
    TextField textfromIndexNo = new TextField("0");
    Label lbtoIndex = new Label("To index:");
    TextField texttoIndexNo = new TextField("0");
    Label lbStatus = new Label("Status:");
    Label savingNo = new Label("Saving#:");
    Label currIndex = new Label("");
    CheckBox ckendaddr = new CheckBox();
    final Label lbckendaddr = new Label("Save with Prefix End Addresses?");
    Label lbsv = new Label("");

    public BigInteger maxsubnet = BigInteger.ZERO;
    SEaddress StartEnd = new SEaddress();

    long FromIndex = 0;
    long ToIndex = 0;
    BigInteger TotalBytes = BigInteger.ZERO;

    Boolean is128Checked;
    FileChooser fileChooser = new FileChooser();
    File file;
    // Service/Task Threading does not progressReport BigInteger:
    long count = 0;
    long howmany = 0;
    //</editor-fold>

    // Default:
    public SaveAsTxt(SEaddress input, Boolean is128Checked) {

        this.StartEnd.ID = this.ID;
        //
        ImageView saveasicon = new ImageView(
                new Image(SaveAsTxt.class.getResourceAsStream("saveas.png")));
        root.setTop(saveasicon);
        root.setCenter(this.addAnchorPane(addGrid()));
        stage.setResizable(false);
        stage.sizeToScene();
        stage.initModality(Modality.NONE);

        stage.setTitle("IPv6 Subnet Calculator / Tool");
        stage.setScene(scene);
        stage.show();
        //
        SettingsAndEvents();
        //
        stage.setOnCloseRequest(new EventHandler<WindowEvent>(){
            @Override
            public void handle(WindowEvent event) {
                IPv6SubnettingTool.RemoveStageItem(stage.hashCode());
            }
        });
        //
        SetNewValues(input, is128Checked);
        //
        if ((!is128Checked && input.subnetslash == 64)
                ||
                (is128Checked && input.subnetslash == 128)
                ||
                input.ID == 1
                ||
                input.ID == 2
                )
        {
            ckendaddr.setSelected(false);
            ckendaddr.setDisable(true);
        }
        else {
            ckendaddr.setDisable(false);
        }
        
        if (input.ID == 0 || input.ID == 1) {
            this.lbsv.setText("[Prefixes]");
        } else if (input.ID == 2) {
            this.lbsv.setText("[Reverse DNS]");
        }

    }

    public void SetNewValues(SEaddress input, Boolean is128Checked) {
        this.incomingID = input.ID;
        this.is128Checked = is128Checked;        
        this.is128Checked = is128Checked;
        this.StartEnd.LowerLimitAddress = input.LowerLimitAddress;
        this.StartEnd.Resultv6 = input.Resultv6;
        this.StartEnd.slash = input.slash;
        this.StartEnd.Start = input.Start;
        this.StartEnd.End = input.End;
        this.StartEnd.subnetidx = input.subnetidx;
        this.StartEnd.subnetslash = input.subnetslash;
        this.StartEnd.UpperLimitAddress = input.UpperLimitAddress;
        this.StartEnd.upto = input.upto;
        //
        ss = v6ST.Kolonlar(this.StartEnd.Start);
        se = v6ST.Kolonlar(this.StartEnd.End);
        //
        bar.progressProperty().unbind();
        //labelPercent.textProperty().unbind();
        //labelPercent.setText("0%");
        labelStatus.textProperty().unbind();
        labelStatus.setText("");
        buttonSave.disableProperty().unbind();
        service.reset();
        bar.setProgress(0);
        //

        if (!this.is128Checked) {
            this.r1.setText("s> " + ss.substring(0, 19) + "::" + "/" + this.StartEnd.subnetslash);
            this.r2.setText("e> " + se.substring(0, 19) + "::" + "/" + this.StartEnd.subnetslash);
        } else if (this.is128Checked) {
            this.r1.setText("s> " + ss + "/" + this.StartEnd.subnetslash);
            this.r2.setText("e> " + se + "/" + this.StartEnd.subnetslash);
        }

        if (input.ID == 0 || input.ID == 2) {
            this.maxsubnet = BigInteger.ONE.shiftLeft(this.StartEnd.subnetslash - this.StartEnd.slash);
        } else if (input.ID == 1) {
            if (!this.is128Checked) {
                this.maxsubnet = BigInteger.ONE.shiftLeft(64 - this.StartEnd.subnetslash);
            } else if (this.is128Checked) {
                this.maxsubnet = BigInteger.ONE.shiftLeft(128 - this.StartEnd.subnetslash);
            }
        } else {
            return;
        }
        this.maxsubnet = this.maxsubnet.subtract(BigInteger.ONE);
        this.textmaxIndexNo.setText(String.valueOf(this.maxsubnet));
        //
        if ((!is128Checked && input.subnetslash == 64)
                ||
                (is128Checked && input.subnetslash == 128)
                ||
                input.ID == 1
                ||
                input.ID == 2
                )
        {
            ckendaddr.setSelected(false);
            ckendaddr.setDisable(true);
        }
        else {
            ckendaddr.setDisable(false);
        }        
    }
    
    public void SettingsAndEvents() {
        ckendaddr.setIndeterminate(false);
        lbckendaddr.setGraphic(ckendaddr);
        
        textfromIndexNo.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                    String newValue) {
                if (!newValue.matches("\\d*")) {
                    textfromIndexNo.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        texttoIndexNo.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                    String newValue) {
                if (!newValue.matches("\\d*")) {
                    texttoIndexNo.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        buttonSave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bar.progressProperty().unbind();
                //labelPercent.textProperty().unbind(); CLOSED!
                labelStatus.textProperty().unbind();
                buttonSave.disableProperty().unbind();
                //
                //labelPercent.setText("0%"); CLOSED!
                labelStatus.setText("");
                service.reset();
                //
                bar.setProgress(0);
                bar.progressProperty().bind(service.progressProperty());
                //labelPercent.textProperty().bind(service.messageProperty()); CLOSED!
                currIndex.textProperty().bind(service.messageProperty());
                labelStatus.textProperty().bind(service.stateProperty().asString());
                buttonSave.disableProperty().bind(service.runningProperty());
                //
                try {
                    FromIndex = Long.valueOf(textfromIndexNo.getText());
                    ToIndex = Long.valueOf(texttoIndexNo.getText());
                } catch (NumberFormatException ex) {
                    MsgBox.Show(AlertType.ERROR, ex.getMessage() + "\r\n"
                            + "Max.Value is: 9223372036854775806"
                            );
                    return;
                }
                long maxsave = 0x7fffffffffffffffL;
                
                if (FromIndex > maxsave || ToIndex > maxsave) 
                {
                    MsgBox.Show(AlertType.ERROR, "Greater than maximum value!\r\n"
                            + "Try to save part by part.\r\n"
                            + "(Max. value for saving is: 9223372036854775806)");
                    return;
                } 
                else if (FromIndex > ToIndex) 
                {
                    MsgBox.Show(AlertType.ERROR, "FromIndex can not be greater than ToIndex.");
                    return;
                }
                else if (maxsubnet.compareTo(BigInteger.valueOf(maxsave)) < 0) 
                {
                    if (FromIndex > maxsubnet.longValueExact() 
                            || ToIndex > maxsubnet.longValueExact()) {
                        MsgBox.Show(AlertType.ERROR,
                                "FromIndex or ToIndex can not be greater than Max.Index#.");
                        return;
                    }
                }
                //
                StartEnd.subnetidx = BigInteger.valueOf(FromIndex);
                TotalBytes = BigInteger.ZERO;
                StartEnd.Start = StartEnd.LowerLimitAddress;
                StartEnd.End = StartEnd.UpperLimitAddress;

                BigInteger OnceTotalBytes = BigInteger.ZERO;
                BigInteger OnceDnsTotalBytes = BigInteger.ZERO;
                //
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"),
                        new FileChooser.ExtensionFilter("All files", "*.*")
                );

                if (incomingID == 0 || incomingID == 1) {
                    fileChooser.setInitialFileName(ss.replaceAll(":", "")
                            + "_prefix" + StartEnd.slash
                            + "to" + StartEnd.subnetslash + "_index"
                            + FromIndex + "to" + ToIndex);
                } else if (incomingID == 2) {
                    OnceTotalBytes = OnceDnsTotalBytes;
                    fileChooser.setInitialFileName("ReverseDNS_"
                            + ss.replaceAll(":", "")
                            + "_prefix" + StartEnd.subnetslash + "_index"
                            + FromIndex + "to" + ToIndex);
                }

                file = fileChooser.showSaveDialog(stage);

                if (file != null) {
                    service.restart();
                }
            }
        });
        buttonCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                service.cancel();
            }
        });
        buttonExit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bar.progressProperty().unbind();
                //labelPercent.textProperty().unbind(); CLOSED!
                labelStatus.textProperty().unbind();
                buttonSave.disableProperty().unbind();
                //
                service.cancel();
                IPv6SubnettingTool.RemoveStageItem(stage.hashCode());                
                stage.close();
            }
        });
    }

    private class FirstLineService extends Service<Long> {

        protected Task<Long> createTask() {
            return new Task<Long>() {
                protected Long call() throws IOException {
                    long i = 0;
                    try (FileWriter fileWriter = new FileWriter(file)) {

                        if (StartEnd.Start.compareTo(StartEnd.UpperLimitAddress) == 0) {
                            return i;
                        }

                        howmany = ToIndex - FromIndex + 1;
                        int perc = 0;
                        count = 0;
                        TotalBytes = BigInteger.ZERO;
                        StartEnd.subnetidx = BigInteger.valueOf(FromIndex);
                        String ss = "", se = "";

                        if (incomingID == 1) {
                            if (!is128Checked) {
                                StartEnd.subnetslash = 64;
                            } else if (is128Checked) {
                                StartEnd.subnetslash = 128;
                            }
                        }
                        StartEnd = v6ST.GoToSubnet(StartEnd, is128Checked);
                        for (i = 1; i <= howmany; i++) {
                            if (isCancelled()) {
                                count--;
                                //updateMessage(String.valueOf(i * 100 / howmany) + "%");
                                updateMessage(String.valueOf(count));
                                updateProgress(i, howmany);
                                break;
                            } else {
                                StartEnd = v6ST.Subnetting(StartEnd, is128Checked);
                                if (!is128Checked) {
                                    if (incomingID == 0 || incomingID == 1) {
                                        ss = v6ST.Kolonlar(StartEnd.Start);
                                        ss = ss.substring(0, 19);
                                        ss += "::";
                                        ss = v6ST.CompressAddress(ss);
                                        ss = "p" + StartEnd.subnetidx + "> " + ss + "/"
                                                + StartEnd.subnetslash;
                                        fileWriter.write(ss + "\r\n");
                                        TotalBytes = TotalBytes.add(BigInteger.valueOf(ss.length() + 2));
                                        //
                                        if (StartEnd.subnetslash != 64) {
                                            if (ckendaddr.isSelected()) {
                                                se = v6ST.Kolonlar(StartEnd.End);
                                                se = se.substring(0, 19);
                                                se += "::";
                                                se = v6ST.CompressAddress(se);
                                                se = "e" + StartEnd.subnetidx + "> " + se + "/"
                                                        + StartEnd.subnetslash;
                                                fileWriter.write(se + "\r\n");
                                                fileWriter.write("\r\n");
                                                TotalBytes = TotalBytes.add(BigInteger.valueOf(se.length() + 4));
                                            }
                                        }

                                    } else if (incomingID == 2) {
                                        String[] sa;
                                        int spaces = 0;

                                        sa = v6ST.DnsRev(StartEnd.Start, StartEnd.subnetslash, is128Checked);
                                        sa[0] = "p" + StartEnd.subnetidx + "> " + sa[0];
                                        spaces = sa[0].split(" ")[0].length() + 1;

                                        for (int n = 0; n < 8; n++) {
                                            if (sa[n] == null) {
                                                break;
                                            }
                                            if (n > 0) {
                                                sa[n] = String.format("%1$" + Integer.valueOf(sa[n].length() + spaces) + "s", sa[n]);
                                            }
                                            TotalBytes = TotalBytes.add(BigInteger.valueOf(sa[n].length() + 2));
                                            fileWriter.write(sa[n] + "\r\n");
                                        }
                                    }
                                } else if (is128Checked) {
                                    if (incomingID == 0 || incomingID == 1) {
                                        ss = v6ST.Kolonlar(StartEnd.Start);
                                        ss = v6ST.CompressAddress(ss);
                                        ss = "p" + StartEnd.subnetidx + "> " + ss + "/"
                                                + StartEnd.subnetslash;
                                        TotalBytes = TotalBytes.add(BigInteger.valueOf(ss.length() + 2));
                                        fileWriter.write(ss + "\r\n");
                                        //
                                        if (StartEnd.subnetslash != 128) {
                                            if (ckendaddr.isSelected()) {
                                                se = v6ST.Kolonlar(StartEnd.End);
                                                se = v6ST.CompressAddress(se);
                                                se = "e" + StartEnd.subnetidx + "> " + se + "/"
                                                        + StartEnd.subnetslash;
                                                fileWriter.write(se + "\r\n");
                                                fileWriter.write("\r\n");
                                                TotalBytes = TotalBytes.add(BigInteger.valueOf(se.length() + 4));
                                            }
                                        }

                                    } else if (incomingID == 2) {
                                        String[] sa;
                                        int spaces = 0;

                                        sa = v6ST.DnsRev(StartEnd.Start, StartEnd.subnetslash, is128Checked);
                                        sa[0] = "s" + StartEnd.subnetidx + "> " + sa[0];
                                        spaces = sa[0].split(" ")[0].length() + 1;

                                        for (int n = 0; n < 8; n++) {
                                            if (sa[n] == null) {
                                                break;
                                            }
                                            if (n > 0) {
                                                sa[n] = String.format("%1$" + Integer.valueOf(sa[n].length() + spaces) + "s", sa[n]);
                                            }

                                            TotalBytes = TotalBytes.add(BigInteger.valueOf(sa[n].length() + 2));
                                            fileWriter.write(sa[n] + "\r\n");
                                        }
                                    }
                                }
                                perc = (int) (i * 100 / howmany);
                                saveState.SavedLines = BigInteger.valueOf(count);
                                saveState.percentage = perc;
                                //updateMessage(String.valueOf(i * 100 / howmany) + "%");
                                updateMessage(String.valueOf(count));
                                updateProgress(i, howmany);

                                if (StartEnd.Start.equals(StartEnd.UpperLimitAddress)
                                        || StartEnd.subnetidx.equals(maxsubnet)) {
                                    break;
                                }
                                if (is128Checked) {
                                    StartEnd.Start = StartEnd.End.add(BigInteger.ONE);
                                } else if (!is128Checked) {
                                    StartEnd.Start = StartEnd.End.add(BigInteger.ONE.shiftLeft(64));
                                }

                            }
                            count++;
                        }
                        saveState.SavedLines = BigInteger.valueOf(count);
                        saveState.percentage = perc;
                        fileWriter.close();
                    } catch (IOException ex) {
                        Logger.getLogger(IPv6SubnettingTool.class.getName()).log(Level.SEVERE, null, ex);
                        MsgBox.Show(AlertType.ERROR, ex.getMessage());
                    }
                    return i;
                }
            };
        }
    }

    public GridPane addGrid() {
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setAlignment(Pos.BASELINE_CENTER);
        grid.setPadding(new Insets(0, 5, 0, 5));
        //
        lbsv.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.NORMAL, 12));        
        HBox hblbsv = new HBox();
        hblbsv.setAlignment(Pos.CENTER_LEFT);
        hblbsv.getChildren().add(lbsv);
        grid.add(hblbsv, 1, 0);
        //
        r0.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 12));
        HBox hbr0 = new HBox();
        hbr0.setAlignment(Pos.CENTER_RIGHT);
        hbr0.getChildren().add(r0);

        r1.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 12));
        r2.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 12));
        grid.add(hbr0, 0, 1);
        grid.add(r1, 1, 1);
        grid.add(r2, 1, 2);
        //
        Label empty = new Label("");
        grid.add(empty, 0, 2);
        //
        textmaxIndexNo.setEditable(false);
        textmaxIndexNo.setMaxWidth(270);
        textfromIndexNo.setMaxWidth(270);
        texttoIndexNo.setMaxWidth(270);
        HBox hbmaxindex = new HBox();
        hbmaxindex.setAlignment(Pos.CENTER_RIGHT);
        hbmaxindex.getChildren().add(maxIndex);
        grid.add(hbmaxindex, 0, 3);
        grid.add(textmaxIndexNo, 1, 3);
        HBox hbfromindex = new HBox();
        hbfromindex.setAlignment(Pos.CENTER_RIGHT);
        hbfromindex.getChildren().add(lbfromIndex);
        grid.add(hbfromindex, 0, 4);
        grid.add(textfromIndexNo, 1, 4);
        HBox hbtoindex = new HBox();
        hbtoindex.setAlignment(Pos.CENTER_RIGHT);
        hbtoindex.getChildren().add(lbtoIndex);
        grid.add(hbtoindex, 0, 5);
        grid.add(texttoIndexNo, 1, 5);
        //
        bar.setPrefWidth(270);
        HBox hbpbar = new HBox(5);
        hbpbar.getChildren().addAll(bar, labelPercent);
        grid.add(hbpbar, 1, 6);
        //
        HBox hblbstatus = new HBox();
        hblbstatus.setAlignment(Pos.CENTER_RIGHT);
        hblbstatus.getChildren().add(lbStatus);
        grid.add(hblbstatus, 0, 7);
        grid.add(labelStatus, 1, 7);
        //
        HBox hbindexno = new HBox();
        hbindexno.setAlignment(Pos.CENTER_RIGHT);        
        hbindexno.getChildren().add(savingNo);
        grid.add(hbindexno, 0, 8);
        grid.add(currIndex, 1, 8);
        //
        HBox hblbendaddr = new HBox();
        hblbendaddr.getChildren().add(lbckendaddr);
        grid.add(hblbendaddr, 1, 9);
        //
        HBox hbButtons = new HBox(10);
        buttonSave.setPrefWidth(60);
        buttonCancel.setPrefWidth(60);
        buttonExit.setPrefWidth(60);
        hbButtons.getChildren().addAll(buttonSave, buttonCancel, buttonExit);
        grid.add(hbButtons, 1, 10);
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

}