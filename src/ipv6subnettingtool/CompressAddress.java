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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
public class CompressAddress {
    SEaddress seaddress = new SEaddress();
    Stage stage = new Stage();
    BorderPane root = new BorderPane();
    Scene scene = new Scene(root, 470, 320);
    GridPane grid = new GridPane();
    //
    Label status = new Label("> ");
    Label lbipv6addr = new Label("IPv6 Address:");
    TextField textIPv6addr = new TextField("");
    Button buttonFind = new Button("Find");
    Label lbCompressed = new Label("Compressed:");
    TextField textCompressed = new TextField("");
    Label lbUncompressed = new Label("Uncompressed:");
    TextField textUncompressed = new TextField("");
    Label lbFull = new Label("Full:");
    TextField textFull = new TextField("");
    Label lbRevDNS = new Label("Reverse DNS:");
    TextField textRevDNS = new TextField("");
    Label lbInt = new Label("Integer:");
    TextField textInt = new TextField("");
    Label lbHex = new Label("Hex:");
    TextField textHex = new TextField("");
    Label lbBin = new Label("Binary:");
    //TextField textBin = new TextField("");
    TextArea textaBin = new TextArea("");

    public void SettingsAndEvents() {
        textIPv6addr.setPrefWidth(300);
        buttonFind.setPrefWidth(50);
        //
        textIPv6addr.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        textCompressed.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        textUncompressed.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        textFull.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        textInt.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        textHex.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        textaBin.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        //
        textCompressed.setEditable(false);
        textUncompressed.setEditable(false);
        textFull.setEditable(false);
        textRevDNS.setEditable(false);
        textInt.setEditable(false);
        textHex.setEditable(false);
        textaBin.setPrefWidth(300); textaBin.setPrefHeight(70);
        textaBin.setEditable(false);
        textaBin.setWrapText(true);
        //
        buttonFind.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                textIPv6addr.setText(textIPv6addr.getText().trim());
                Calculate(textIPv6addr.getText());
            }
        });
        //
    }
    
    public void Calculate(String sin) {

        if (v6ST.IsAddressCorrect(sin)) {
                status.setText(v6ST.errmsg);
                BigInteger BigResv6 = v6ST.FormalizeAddr(sin);
                String Resv6 = v6ST.Kolonlar(v6ST.FormalizeAddr(sin));
                textCompressed.setText(v6ST.CompressAddress(Resv6));
                
                String[] formal = Resv6.split(":");
                String tmp = "";
                for (String s : formal)
                {
                    tmp += String.format("%x", Integer.parseUnsignedInt(s, 16)) + ":";
                }
                textUncompressed.setText(tmp.substring(0, tmp.length() - 1));

                textFull.setText(Resv6);
                //
                seaddress.Resultv6 = seaddress.Start = BigResv6;
                //
                textRevDNS.setText(v6ST.DnsRev(BigResv6, 128, Boolean.TRUE)[0]);
                textInt.setText(String.valueOf(BigResv6));
                textHex.setText("0x" + String.format("%x", BigResv6));
                //
                String sbin = v6ST.PrintBin(seaddress, 128, Boolean.TRUE);
                textaBin.setText(sbin.substring(0, 40) + "\r\n"
                        + sbin.substring(40, 80) + "\r\n"
                        + sbin.substring(80, 120) + "\r\n"
                        + sbin.substring(120, 159)
                );
            }
            else
            {
                status.setText(v6ST.errmsg);
                textCompressed.clear();
                textUncompressed.clear();
                textFull.clear();
                textRevDNS.clear();
                textInt.clear();
                textHex.clear();
                textaBin.clear();
            }
    }
    
    public CompressAddress() {
        
        //
        SettingsAndEvents();
        root.setCenter(this.addAnchorPane(addGrid()));
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setTitle("IPv6 Subnet Calculator - Compress/Uncompress");
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
    //
    public GridPane addGrid() {
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setAlignment(Pos.BASELINE_CENTER);
        grid.setPadding(new Insets(0, 10, 0, 10));
        //
        status.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 12));
        grid.add(status, 1, 0);
        //
        HBox hb0 = new HBox();
        hb0.setAlignment(Pos.CENTER_RIGHT);
        hb0.getChildren().addAll(lbipv6addr);
        grid.add(hb0, 0, 1);
        //
        HBox hb1 = new HBox(5);
        hb1.getChildren().addAll(textIPv6addr, buttonFind);
        grid.add(hb1, 1, 1);
        //
        HBox hb2 = new HBox();
        hb2.setAlignment(Pos.CENTER_RIGHT);
        hb2.getChildren().add(lbCompressed);
        grid.add(hb2, 0, 2); grid.add(textCompressed, 1, 2);
        //
        HBox hb3 = new HBox();
        hb3.setAlignment(Pos.CENTER_RIGHT);
        hb3.getChildren().add(lbUncompressed);
        grid.add(hb3, 0, 3); grid.add(textUncompressed, 1, 3);
        //
        HBox hb4 = new HBox();
        hb4.setAlignment(Pos.CENTER_RIGHT);
        hb4.getChildren().add(lbFull);
        grid.add(hb4, 0, 4); grid.add(textFull, 1, 4);
        //
        HBox hb5 = new HBox();
        hb5.setAlignment(Pos.CENTER_RIGHT);
        hb5.getChildren().add(lbHex);
        grid.add(hb5, 0, 5); grid.add(textHex, 1, 5);
        //
        HBox hb6 = new HBox();
        hb6.setAlignment(Pos.CENTER_RIGHT);
        hb6.getChildren().add(lbInt);
        grid.add(hb6, 0, 6); grid.add(textInt, 1, 6);
        //
        HBox hb7 = new HBox();
        hb7.setAlignment(Pos.TOP_RIGHT);
        hb7.getChildren().add(lbBin);
        grid.add(hb7, 0, 7); grid.add(textaBin, 1, 7);
        //
        HBox hb8 = new HBox();
        hb8.setAlignment(Pos.CENTER_RIGHT);
        hb8.getChildren().add(lbRevDNS);
        grid.add(hb8, 0, 8); grid.add(textRevDNS, 1, 8);

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
