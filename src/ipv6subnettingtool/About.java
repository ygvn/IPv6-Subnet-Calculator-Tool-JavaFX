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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Yucel Guven
 */
public final class About {
    public static final String version = "3.3";
    /**
     * About 
     */
    public About() { }
    
    public static void Show() {
        final Stage stage = new Stage();
        GridPane grid = new GridPane();
        Button button = new Button("OK");
        Scene scene = new Scene(grid, 300, 245);
        //
        Label label0 = new Label("       ");
        Label label1 = new Label("IPv6 Subnet Calculator / IPv6 Ağ Hesap Makinesi");
        Label label2 = new Label("Version " + version);
        Label label3 = new Label("Copyright (c) 2010-2020 Yucel Guven");
        Label label4 = new Label("yucelguven@hotmail.com");
        Label label5 = new Label("");
        Label label6 = new Label("IPv4> FF.FF.FF.FF");
        Label label7 = new Label("          4294967295");
        Label label8 = new Label("");
        Label label9  = new Label("IPv6> FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF");
        Label label10 = new Label("340282366920938463463374607431768211455");
        Label label11 = new Label("");
        //
        grid.add(label0, 0, 0);
        grid.add(label1, 1, 1);
        grid.add(label2, 1, 2);
        grid.add(label3, 1, 3);
        grid.add(label4, 1, 4);
        grid.add(label5, 1, 5);
        grid.add(label6, 1, 6);
        grid.add(label7, 1, 7);
        grid.add(label8, 1, 8);
        grid.add(label9, 1, 9);
        grid.add(label10, 1, 10);
        grid.add(label11, 1, 11);
        HBox hb = new HBox();
        hb.setAlignment(Pos.CENTER_RIGHT);
        button.setPrefWidth(70);
        hb.getChildren().add(button);
        grid.add(hb, 1, 12);
        //
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                stage.close();
            }
        });
        stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if (key.getCode() == KeyCode.ESCAPE) {
                    stage.close();
                }
            }
        });
        //
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setTitle("IPv6 Subnet Calculator - About");
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }
}
