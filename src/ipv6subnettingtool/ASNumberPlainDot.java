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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
public class ASNumberPlainDot {
    Stage stage = new Stage();
    GridPane asnGrid = new GridPane();
    Scene asnScene = new Scene(asnGrid, 280, 200);
    Button buttonConvert = new Button("Convert");
    Button buttonCancel = new Button("Cancel");
    TextField tfinput = new TextField("");
    TextField tfresult = new TextField("");

    final ToggleGroup group = new ToggleGroup();
    RadioButton rb1 = new RadioButton("from asplain to asdot");
    RadioButton rb2 = new RadioButton("from asdot to asplain");

    public ASNumberPlainDot() {
        //
        asnGrid.setHgap(5);
        asnGrid.setVgap(10);
        asnGrid.setPadding(new Insets(10, 10, 10, 10));
        //
        buttonConvert.setPrefWidth(60);
        buttonCancel.setPrefWidth(60);
        tfresult.setEditable(false);
        tfresult.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        tfinput.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        //
        rb1.setToggleGroup(group);
        rb2.setToggleGroup(group);
        rb1.setSelected(true);
        //
        asnGrid.add(new Label(" "), 0, 0);
        HBox hbt = new HBox();
        hbt.setAlignment(Pos.CENTER);
        Label title = new Label("AS Number Plain/Dot Conversion");
        title.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 14));
        title.setStyle("-fx-text-fill: royalblue; -fx-font-weight: bold;");
        hbt.getChildren().add(title);
        asnGrid.add(hbt, 0, 0, 2, 1);
        asnGrid.add(new Label("  AS No:"), 0, 1);
        asnGrid.add(tfinput, 1, 1);
        asnGrid.add(new Label("  Result:"), 0, 2);
        asnGrid.add(tfresult, 1, 2);
        //
        asnGrid.add(rb1, 1, 3);
        asnGrid.add(rb2, 1, 4);
        //
        HBox hbbutton = new HBox(20);
        hbbutton.getChildren().addAll(buttonConvert, buttonCancel);
        asnGrid.add(hbbutton, 1, 5);
        
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
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setTitle("IPv6 Subnet Calculator - ASN plain/dot Convertion");
        stage.setScene(asnScene);
        stage.show();
        //
        buttonCancel.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                IPv6SubnettingTool.RemoveStageItem(stage.hashCode());
                stage.close();
            }
        });
        buttonConvert.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tfresult.clear();
                String res = null;
                
                if (group.getSelectedToggle() != null) {
                    if (rb1.isSelected()) { // to asdot
                        res = v6ST.ConvertASnum(tfinput.getText().trim(), v6ST.toASdot);
                        if ( res != null ) {
                            tfresult.setText(res);
                        } else {
                            tfinput.requestFocus();
                            tfresult.setText(v6ST.errmsg);
                            MsgBox.Show(Alert.AlertType.ERROR, v6ST.errmsg);
                        }
                    } else if (rb2.isSelected()) { // to asplain
                        res = v6ST.ConvertASnum(tfinput.getText().trim(), v6ST.toASplain);
                        if (res != null) {
                            tfresult.setText(res);
                        } else {
                            tfinput.requestFocus();
                            tfresult.setText(v6ST.errmsg);
                            MsgBox.Show(Alert.AlertType.ERROR, v6ST.errmsg);
                        }
                    }
                }
            }
        });
        tfinput.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                    String newValue) {
                if (!newValue.matches("[0-9][.]")) { // only digits and dots
                    tfinput.setText(newValue.replaceAll("[^\\d\\.]", ""));
                }
            }
        });
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                if (group.getSelectedToggle() != null) {

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
        stage.centerOnScreen();
    }
    
}
