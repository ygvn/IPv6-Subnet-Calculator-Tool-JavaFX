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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Yucel Guven
 */
public class Goto {
    Stage gotoStage = new Stage();
    GridPane gotoGrid = new GridPane();
    Scene gotoScene = new Scene(gotoGrid, 300, 150);
    Button buttonGoto = new Button("Go");
    Button buttonCancel = new Button("Cancel");
    TextField tfMax = new TextField();
    TextField gototf = new TextField("");
    Label lbin = new Label();

    public Goto(final int ID, final int idx) {
        
        gotoGrid.setHgap(5);
        gotoGrid.setVgap(3);
        gotoStage.initModality(Modality.APPLICATION_MODAL);
        gotoGrid.setPadding(new Insets(10, 10, 10, 10));
        //
        buttonGoto.setPrefWidth(60);
        buttonGoto.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (gototf.getText().trim().equals(""))
                    return;
                
                lbin.setText("");
                if (ID == 0) {
                    if (idx == 0) {
                        IPv6SubnettingTool.gotoasnValue =
                                new BigInteger(gototf.getText().trim());
                        if (IPv6SubnettingTool.gotoasnValue.compareTo(IPv6SubnettingTool.asnmax.subtract(BigInteger.ONE)) > 0) {
                           lbin.setText("Max. exceeded!");
                           return;
                        }
                    }
                    else if (idx == 1) {
                        IPv6SubnettingTool.gotopfxValue =
                                new BigInteger(gototf.getText().trim());
                        if (IPv6SubnettingTool.gotopfxValue.compareTo(IPv6SubnettingTool.prefixmax.subtract(BigInteger.ONE)) > 0) {
                            lbin.setText("Max. exceeded!");
                            return;
                        }
                    }
                    else if (idx == 2) {
                        if (v6ST.IsAddressCorrect(gototf.getText().trim())) {
                            IPv6SubnettingTool.findpfx = gototf.getText().trim();
                        }
                        else {
                            lbin.setText(v6ST.errmsg);
                            return;
                        }
                    }
                    gotoStage.close();
                }
            }
        });
        buttonCancel.setPrefWidth(60);
        buttonCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (ID == 0 && idx == 2) {
                    IPv6SubnettingTool.findpfx = "";
                }
                gotoStage.close();
        }
        });
        //
        gototf.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                    String newValue) {
                if (ID == 0 && idx == 2) // alfanumeric allowed for findPfx
                    return;
                else if (!newValue.matches("\\d*")) { // only digits
                    gototf.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        
        //
        if (ID == 0 && idx == 0) {
            lbin.setText("Address Space (Global Routing Prefix) Number:");
            tfMax.setText(String.valueOf(IPv6SubnettingTool.asnmax.subtract(BigInteger.ONE)));
        }
        else if (ID == 0 && idx == 1) {
            lbin.setText("Prefix Number:");
            tfMax.setText(String.valueOf(IPv6SubnettingTool.prefixmax.subtract(BigInteger.ONE)));
        }
        else if (ID == 0 && idx == 2) {
            lbin.setText("Find Prefix: (Please enter the prefix without slash '/')");
            tfMax.clear();
        }
        else if (ID == 1 && idx == 0) {
            
        }
        gotoGrid.add(lbin, 0, 0);

        if (ID == 0) {
            if (idx == 0)
                gototf.setText(String.valueOf(IPv6SubnettingTool.gotoasnValue));
            else if (idx == 1)
                gototf.setText(String.valueOf(IPv6SubnettingTool.gotopfxValue));
            else if (idx == 2)
                gototf.setText(IPv6SubnettingTool.findpfx);
        }
        gototf.setPrefWidth(300);
        gototf.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        HBox hbtf = new HBox();
        hbtf.getChildren().add(gototf);
        gotoGrid.add(hbtf, 0, 1);
        //
        HBox hb = new HBox(10);
        hb.setPadding(new Insets(5, 0, 5, 0));
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().addAll(buttonGoto, buttonCancel);
        gotoGrid.add(hb, 0, 2);
        //
        Label lbMax = new Label("Max:");
        gotoGrid.add(lbMax, 0, 3);
      
        tfMax.setEditable(false);
        tfMax.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        gotoGrid.add(tfMax, 0, 4);
        //
        gotoStage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if (key.getCode() == KeyCode.ESCAPE) {
                    gotoStage.close();
                }
            }
        });        
        //
        gotoStage.setResizable(false);
        gotoStage.sizeToScene();
        gotoStage.setTitle("IPv6 Subnet Calculator - Go to...");
        gotoStage.setScene(gotoScene);
        gotoStage.showAndWait();
        //
    }
}
