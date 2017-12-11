/*
 * Copyright (c) 2010-2018, Yücel Güven
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
public final class ListAssignedfromDB {

//<editor-fold defaultstate="collapsed" desc="special initials/constants -Yücel">
    //
    Stage stage = new Stage();
    BorderPane root = new BorderPane();
    Scene scene = new Scene(root, 420, 420);
    GridPane grid = new GridPane();
    DatabaseUI dbUI = null;
    Label statusString = new Label("");
    //
    Label r0 = new Label("");
    Label r1 = new Label("Range> ");
    //
    Button buttonFirstPage = new Button("|<< FirstPage");
    Button buttonBack = new Button("<<");
    Button buttonFwd = new Button(">>");
    Button buttonLast = new Button(">>|");
    Label listcount = new Label("");    
    //
    ListView<String> prefixlist = new ListView<String>();
    //
    SEaddress seaddr = new SEaddress();
    String prefix = "";
    String end = "";
    short pflen = 0;
    short parentpflen = 0;
    String tmp_first = "";
    String tmp_last = "";
    String last_start = "";
    String db_FirstItem = "";
    String db_LastItem = "";
    int db_ItemCount = 0;
    int page_records = 0;
    final int records_perpage = 32;

    //Database
    static Connection MySQLconnection = null;
    public DBServerInfo dbserverInfo = new DBServerInfo();
    static Label lbdbstatus = new Label("");
    Boolean chks = Boolean.FALSE;
    Statement statement = null;
    ResultSet resultSet = null;
    public ObservableList<String> liste = FXCollections.observableArrayList();
    //
    ContextMenu contextMenu = new ContextMenu();
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    //
//</editor-fold>

    public ListAssignedfromDB(String inprefix, String inend, short inparentpflen,
            short inpflen, Boolean inchks, Connection sqlcon, DBServerInfo servinfo) {
        
        MySQLconnection = sqlcon;
        this.dbserverInfo = servinfo;
        //
        root.setCenter(this.addAnchorPane(addGrid()));
        root.setBottom(addStatusBar());        
        stage.setResizable(false);
        stage.setTitle("IPv6 Subnet Calculator - List Assigned from DB");
        stage.setScene(scene);
        stage.show();
        stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if (key.getCode() == KeyCode.ESCAPE) {
                    IPv6SubnettingTool.RemoveStageItem(stage.hashCode());                    
                    stage.close();
                }
            }
        });
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                IPv6SubnettingTool.RemoveStageItem(stage.hashCode());
            }
        });        
        //
        SettingsAndEvents();
        SetNewValues(inprefix, inend, inparentpflen, inpflen, inchks);
    }

    public void SetNewValues(String inprefix, String inend, short inparentpflen,
            short inpflen, Boolean inchks) {
        
        this.prefix = v6ST.CompressAddress(inprefix.split("/")[0]);
        this.end = v6ST.CompressAddress(inend.split("/")[0]);
        this.parentpflen = inparentpflen;
        this.pflen = inpflen;
        this.chks = inchks;
        this.page_records = 0;

        this.r0.setText("/" + inpflen + " Prefix Utilization.");
        this.r1.setText("Range> " + inprefix + "-" + inpflen);
        
        PreCalc();
        FirstAndLastInDB();
        buttonFirstPage.fire();
        this.listcount.setText("[" + this.page_records + " ent.]");
    }
    
    private void PreCalc() {
        this.seaddr.End = v6ST.FormalizeAddr(this.end);
        this.seaddr.slash = this.parentpflen;
        this.seaddr.subnetslash = this.pflen;
        this.seaddr = v6ST.EndStartAddresses(this.seaddr, this.chks);

        if (chks) {
            this.last_start = v6ST.Kolonlar(this.seaddr.Start);
        } else if (!chks) {
            this.last_start = v6ST.Kolonlar(this.seaddr.Start);
            this.last_start = this.last_start.substring(0, 19);
            this.last_start += "::";
        }
        this.last_start = v6ST.CompressAddress(this.last_start);
    }

    public int FirstAndLastInDB() {
        
        if (MySQLconnection == null) {
            return -1;
        }
        int r = 0;
        String MySQLcmd = "SELECT COUNT(*) FROM "
                + dbserverInfo.Tablename
                + " WHERE ( prefix >= inet6_aton('" + this.prefix + "')"
                + " AND prefix <= inet6_aton('" + this.end + "')"
                + " AND parentpflen= " + parentpflen + " AND pflen= " + pflen + " ) ";

        try {
            IPv6SubnettingTool.UpdateDbStatus();
            statement = MySQLconnection.createStatement();
            resultSet = statement.executeQuery(MySQLcmd);
            resultSet.next();
            this.db_ItemCount = Integer.parseInt(resultSet.getString(1));
            }
            catch (Exception ex) {
                MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                IPv6SubnettingTool.UpdateDbStatus();
            return -1;
        }

        MySQLcmd = "SELECT "
                + " INET6_NTOA(prefix), pflen, netname, person, organization, "
                + "`as-num`, phone, email, status, created, `last-updated` FROM "
                + dbserverInfo.Tablename
                + " WHERE ( prefix >= inet6_aton('" + this.prefix + "')"
                + " AND prefix <= inet6_aton('" + this.end + "')"
                + " AND parentpflen= " + parentpflen + " AND pflen= " + pflen + " ) "
                + " LIMIT " + this.records_perpage;

        try {
            statement = MySQLconnection.createStatement();
            resultSet = statement.executeQuery(MySQLcmd);
            if (resultSet.last()) {
                this.page_records = r = resultSet.getRow();
                resultSet.beforeFirst();
            }
            if (r > 0) {
                resultSet.next();
                this.db_FirstItem = resultSet.getString(1);
            }
        }
        catch (Exception ex) {
            MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
            IPv6SubnettingTool.UpdateDbStatus();
            return -1;
        }

        MySQLcmd = "SELECT "
                + " INET6_NTOA(prefix), pflen, netname, person, organization, "
                + "`as-num`, phone, email, status, created, `last-updated` FROM "
                + dbserverInfo.Tablename
                + " WHERE ( prefix <= inet6_aton('" + this.end + "')"
                + " AND parentpflen= " + parentpflen + " AND pflen= " + pflen + " ) "
                + " ORDER BY prefix DESC LIMIT " + this.records_perpage;

        try {
            statement = MySQLconnection.createStatement();
            resultSet = statement.executeQuery(MySQLcmd);
            if (resultSet.last()) {
                this.page_records = r = resultSet.getRow();
                resultSet.beforeFirst();
            }
            
            if (r > 0) {
                resultSet.next();
                this.db_LastItem = resultSet.getString(1);
            }
        }
        catch (Exception ex) {
            MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
            IPv6SubnettingTool.UpdateDbStatus();
            return -1;
        }

        return r;
    }

    public void SettingsAndEvents() {
        buttonFirstPage.setPrefWidth(90);
        buttonBack.setPrefWidth(40);
        buttonFwd.setPrefWidth(40);
        buttonLast.setPrefWidth(40);
        //
        r0.setStyle("-fx-text-fill: royalblue; -fx-font-weight: bold;");
        r0.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 12));
        r1.setStyle("-fx-text-fill: royalblue; -fx-font-weight: bold;");
        r1.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 12));
        statusString.setStyle("-fx-text-fill: royalblue;");
        lbdbstatus.setStyle("-fx-text-fill: royalblue;");
        //
        prefixlist.setPrefWidth(400);
        prefixlist.setPrefHeight(300);
        prefixlist.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        prefixlist.setContextMenu(contextMenu);
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
        prefixlist.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (t.getButton().equals(MouseButton.PRIMARY) && t.getClickCount() == 2) {
                    GetSelected();
                }
            }
        });
        prefixlist.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if (key.getCode() == KeyCode.ESCAPE) {
                    IPv6SubnettingTool.RemoveStageItem(stage.hashCode());                    
                    stage.close();
                }
                else if (key.getCode() == KeyCode.ENTER) {
                    GetSelected();
                }
            }
        });
        //
        MenuItem contextitemSelectall = new MenuItem("Select All");
        contextitemSelectall.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                prefixlist.setVisible(false);
                prefixlist.getSelectionModel().selectAll();
                prefixlist.setVisible(true);
            }
        });
        MenuItem contextitemCopy = new MenuItem("Copy");
        contextitemCopy.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                String tmp = "";
                for (String s : prefixlist.getSelectionModel().getSelectedItems()) {
                    tmp += s + "\r\n";
                }
                content.putString(tmp);
                clipboard.setContent(content);
            }
        });
        contextMenu.getItems().addAll(contextitemSelectall, contextitemCopy);
        //
        buttonFirstPage.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                tmp_last = "";
                PreCalc();
                int r = MySQLquery(1);

                if (r > 0) {
                    if (db_ItemCount > records_perpage) {
                        buttonBack.setDisable(true);
                        buttonFwd.setDisable(false);
                        buttonLast.setDisable(false);
                    } else {
                        buttonBack.setDisable(true);
                        buttonFwd.setDisable(true);
                        buttonLast.setDisable(true);
                    }
                } else {
                    buttonBack.setDisable(true);
                    buttonFwd.setDisable(true);
                    buttonLast.setDisable(true);
                }
                listcount.setText("[" + page_records + " ent.]");
            }
        });
        buttonBack.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int r = MySQLquery(2);
                if (r > 0) {
                    buttonFwd.setDisable(false);
                    buttonLast.setDisable(false);

                    if (db_FirstItem.equals(prefixlist.getItems().get(0).split(" ")[1].split("/")[0]))
                    {
                        buttonBack.setDisable(true);
                        buttonFwd.setDisable(false);                        
                        listcount.setText("[" + page_records + " ent.]");
                        return;
                    }
                }
                listcount.setText("[" + page_records + " ent.]");
            }
        });
        buttonFwd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int r = MySQLquery(3);
                if (r > 0) {
                    buttonBack.setDisable(false);

                    if (db_LastItem.equals(tmp_last)) {
                        buttonFwd.setDisable(true);
                        buttonLast.setDisable(true);
                        listcount.setText("[" + page_records + " ent.]");
                        return;
                    }
                }
                listcount.setText("[" + page_records + " ent.]");
            }
        });
        buttonLast.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tmp_first = last_start;
                int r = MySQLquery(4);
                if (r > 0) {
                    buttonBack.setDisable(false);
                    buttonFwd.setDisable(true);
                    buttonLast.setDisable(true);
                }
                listcount.setText("[" + page_records + " ent.]");
            }
        });
        
    }

    private void GetSelected() {
        IPv6SubnettingTool.UpdateDbStatus();
        if (MySQLconnection != null) {
            if (prefixlist.getSelectionModel().getSelectedIndex() % 11 == 0) {
                String selected = prefixlist.getSelectionModel().getSelectedItem().split(" ")[1].trim();
                String snet = selected.split("/")[0].trim();
                short plen = Short.parseShort(selected.split("/")[1]);
                if (dbUI == null) {
                dbUI = new DatabaseUI(snet, plen, parentpflen, MySQLconnection, dbserverInfo);
                IPv6SubnettingTool.stageList.add(new StageList(dbUI.toString(),
                        dbUI.stage, dbUI.stage.hashCode()));
                IPv6SubnettingTool.AddStageItem(dbUI.toString(), dbUI.stage.hashCode());
                } else {
                    if (!dbUI.stage.isShowing()) {
                        IPv6SubnettingTool.stageList.add(new StageList(dbUI.toString(),
                                dbUI.stage, dbUI.stage.hashCode()));
                        IPv6SubnettingTool.AddStageItem(dbUI.toString(), dbUI.stage.hashCode());
                    }
                    dbUI.buttonClear.fire();
                    dbUI.SetNewValues(snet, plen, parentpflen);
                    dbUI.StageShow();
                }
            }
        }

    }

    public int MySQLquery(int button) {

        if (MySQLconnection == null) {
            return -1;
        }

        if (this.tmp_last.equals("")) {
            this.tmp_last = this.prefix;
        }

        int r = 0;
        String MySQLcmd = "";
        this.prefixlist.getItems().clear();

        if (button == 1) // // First page
        {
            MySQLcmd = "SELECT "
                    + " INET6_NTOA(prefix), pflen, netname, person, organization, "
                    + "`as-num`, phone, email, status, created, `last-updated` FROM "
                    + dbserverInfo.Tablename
                    + " WHERE ( prefix >= inet6_aton('" + this.tmp_last + "')"
                    + " AND prefix <= inet6_aton('" + this.end + "')"
                    + " AND parentpflen= " + parentpflen + " AND pflen= " + pflen + " ) "
                    //+ " LIMIT 4 ";
                    + " LIMIT " + this.records_perpage;
        } else if (button == 2) // Backwd page
        {
            MySQLcmd = "SELECT "
                    + " INET6_NTOA(prefix), pflen, netname, person, organization, "
                    + "`as-num`, phone, email, status, created, `last-updated` FROM "
                    + dbserverInfo.Tablename
                    + " WHERE ( prefix < inet6_aton('" + this.tmp_first + "')"
                    + " AND prefix >= inet6_aton('" + this.prefix + "')"
                    + " AND parentpflen= " + parentpflen + " AND pflen= " + pflen + " ) "
                    //+ " ORDER BY prefix LIMIT 4 ";
                    + " ORDER BY prefix DESC LIMIT " + this.records_perpage;
        } else if (button == 3) // Fwd page
        {
            MySQLcmd = "SELECT "
                    + " INET6_NTOA(prefix), pflen, netname, person, organization, "
                    + "`as-num`, phone, email, status, created, `last-updated` FROM "
                    + dbserverInfo.Tablename
                    + " WHERE ( prefix > inet6_aton('" + this.tmp_last + "')"
                    + " AND prefix <= inet6_aton('" + this.end + "')"
                    + " AND parentpflen= " + parentpflen + " AND pflen= " + pflen + " ) "
                    //+ " LIMIT 4 ";
                    + " LIMIT " + this.records_perpage;
        } else if (button == 4) // Last page
        {
            MySQLcmd = "SELECT "
                    + " INET6_NTOA(prefix), pflen, netname, person, organization, "
                    + "`as-num`, phone, email, status, created, `last-updated` FROM "
                    + dbserverInfo.Tablename
                    + " WHERE ( prefix <= inet6_aton('" + this.tmp_first + "')"
                    + " AND parentpflen= " + parentpflen + " AND pflen= " + pflen + " ) "
                    + " ORDER BY prefix DESC LIMIT " + this.records_perpage;
        }

        try {
            IPv6SubnettingTool.UpdateDbStatus();
            statement = MySQLconnection.createStatement();
            resultSet = statement.executeQuery(MySQLcmd);

            if (resultSet.last()) {
                this.page_records = r = resultSet.getRow();
                resultSet.beforeFirst();
            }

            if (r > 0) {
                liste.clear();

                if (button == 1 || button == 3) {
                    while (resultSet.next()) {
                        liste.add("prefix:\t\t " + resultSet.getString(1) 
                                + "/" + resultSet.getString(2));
                        liste.add("netname:\t " + resultSet.getString(3));
                        liste.add("person:\t\t " + resultSet.getString(4));
                        liste.add("organization:\t " + resultSet.getString(5));
                        liste.add("as-num:\t\t " + resultSet.getString(6));
                        liste.add("phone:\t\t " + resultSet.getString(7));
                        liste.add("email:\t\t " + resultSet.getString(8));
                        liste.add("status:\t\t " + resultSet.getString(9));
                        liste.add("created:\t " + resultSet.getString(10));
                        liste.add("last-updated:\t " + resultSet.getString(11));
                        liste.add("");
                    }
                } else if (button == 2 || button == 4) {
                    String[] fs = new String[10];
                    while (resultSet.next()) {
                        liste.add("");
                        fs[0] = "prefix:\t\t " + resultSet.getString(1) 
                                + "/" + resultSet.getString(2);
                        fs[1] = "netname:\t " + resultSet.getString(3);
                        fs[2] = "person:\t\t " + resultSet.getString(4);
                        fs[3] = "organization:\t " + resultSet.getString(5);
                        fs[4] = "as-num:\t\t " + resultSet.getString(6);
                        fs[5] = "phone:\t\t " + resultSet.getString(7);
                        fs[6] = "email:\t\t " + resultSet.getString(8);
                        fs[7] = "status:\t\t " + resultSet.getString(9);
                        fs[8] = "created:\t " + resultSet.getString(10);
                        fs[9] = "last-updated:\t " + resultSet.getString(11);

                        for (int i = 9; i > -1; i--) {
                            liste.add(fs[i]);
                        }
                    }
                    javafx.collections.FXCollections.reverse(liste);
                }

                prefixlist.setItems(liste);
                this.tmp_first = liste.get(0).split(" ")[1].split("/")[0];
                this.tmp_last = liste.get(liste.size() - 11).split(" ")[1].split("/")[0];
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
        grid.setPadding(new Insets(0, 10, 0, 10));
        //
        grid.add(r0, 0, 0);
        grid.add(r1, 0, 1);
        grid.add(new Label(""), 0, 2);
        //
        HBox hblistcount = new HBox();
        hblistcount.setAlignment(Pos.BOTTOM_RIGHT);
        listcount.setStyle("-fx-text-fill: royalblue");
        hblistcount.setPrefWidth(170);        
        hblistcount.getChildren().add(listcount);
        //
        HBox hbButtons = new HBox(5);
        hbButtons.getChildren().addAll(buttonFirstPage, buttonBack, buttonFwd, buttonLast, hblistcount);
        grid.add(hbButtons, 0, 3);
        //
        grid.add(prefixlist, 0, 4);
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
        hbstatus.getChildren().addAll(lbdbstatus);
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
