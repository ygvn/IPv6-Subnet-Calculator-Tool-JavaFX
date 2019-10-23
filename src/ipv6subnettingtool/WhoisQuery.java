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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
public class WhoisQuery {
    private final int defaultport = 43; // service name is 'nicname'
    Stage stage = new Stage();
    BorderPane root = new BorderPane();
    Scene scene = new Scene(root, 460, 420);
    GridPane grid = new GridPane();
    final ProgressBar bar = new ProgressBar(0);
    WhoisQuery.FirstLineService service = new WhoisQuery.FirstLineService();
    //
    Label lbIPv6addr = new Label("IPv6 Address:");
    TextField textIPv6addr = new TextField("");
    Label lbQueryserver = new Label("Query Server:");
    Button buttonSearch = new Button("Search");
    Button buttonExit = new Button("Exit");
    TextArea textaResult = new TextArea();
    final ObservableList<String> servers = FXCollections.observableArrayList(
            "RIPE whois.ripe.net", "ARIN whois.arin.net",
            "LACNIC whois.lacnic.net", "APNIC whois.apnic.net",
            "AFRINIC whois.afrinic.net", "IANA whois.iana.org",
            "GNIC whois.nic.gov"
    );
    final ComboBox<String> cbServers = new ComboBox<>(servers);

    public WhoisQuery(String sin) {

        textIPv6addr.setText(sin);
        //
        SettingsAndEvents();
        root.setCenter(this.addAnchorPane(addGrid()));
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setTitle("IPv6 Subnet Calculator - whois Query");
        stage.setScene(scene);
        stage.show();
        stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if (key.getCode() == KeyCode.ESCAPE) {
                    service.cancel();
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
        
        textIPv6addr.setPrefWidth(300);
        textaResult.setPrefWidth(420);
        textaResult.setPrefHeight(290);
        textaResult.setEditable(false);
        //
        bar.setProgress(-1);
        bar.setVisible(false);
        //
        buttonSearch.setPrefWidth(75);
        buttonExit.setPrefWidth(75);
        //
        cbServers.getSelectionModel().select(0);
        //
        buttonSearch.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                textaResult.clear();
                bar.visibleProperty().unbind();
                buttonSearch.disableProperty().unbind();
                //
                bar.visibleProperty().bind(service.runningProperty());
                buttonSearch.disableProperty().bind(service.runningProperty());
                //
                service.restart();
            }
        });
        buttonExit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
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
                    String whois_response = "";
                    try {
                        String key = textIPv6addr.getText().trim() + "\r\n";
                        byte[] keyb = key.getBytes();
                        String server = (String)cbServers.getSelectionModel().getSelectedItem();
                        server = server.split(" ")[1].trim();
                        //
                        Socket socket = new Socket(server, defaultport);
                        try (OutputStream writeToStream = socket.getOutputStream(); 
                                BufferedInputStream readFromStream = new  BufferedInputStream(socket.getInputStream())) 
                        {
                            //
                            writeToStream.write(keyb);
                            writeToStream.flush();
                            int in;
                            while ((in = readFromStream.read()) != -1) {
                                whois_response += (char)in;
                                if (isCancelled()) {
                                    writeToStream.close();
                                    readFromStream.close();
                                    socket.close();
                                }
                            }
                            //
                            writeToStream.close();
                            readFromStream.close();
                            socket.close();
                            //
                            textaResult.setText(whois_response);
                        }
                    }
                    catch(IOException ex) {
                        MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                        textaResult.setText("");
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
        grid.setPadding(new Insets(10, 0, 10, 20));

        HBox hb0 = new HBox();
        hb0.setAlignment(Pos.CENTER_RIGHT);
        hb0.getChildren().add(lbIPv6addr);
        grid.add(hb0, 0, 0); grid.add(textIPv6addr, 1, 0);
        //
        HBox hb1 = new HBox();
        hb1.setAlignment(Pos.CENTER_RIGHT);
        hb1.getChildren().add(lbQueryserver);
        grid.add(hb1, 0, 1);
        HBox hb2 = new HBox(10);
        hb2.setAlignment(Pos.CENTER_LEFT);
        hb2.getChildren().addAll(cbServers, buttonSearch, buttonExit);
        grid.add(hb2, 1, 1);
        grid.add(new Label(""), 0, 2, 2, 1);
        grid.add(textaResult, 0, 3, 2, 1);
        //
        grid.add(bar, 0, 4, 2, 1);
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
