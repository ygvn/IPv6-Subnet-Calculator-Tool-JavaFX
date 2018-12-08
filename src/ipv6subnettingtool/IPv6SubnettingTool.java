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
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Yucel Guven
 */
public class IPv6SubnettingTool extends Application {

//<editor-fold defaultstate="collapsed" desc="special initials/constants -yucel">
    //  /64max= 18446744073709551615
    // /128max= 340282366920938463463374607431768211455
    final int ID = 0; // ID of this Form.
    int incomingID;
    //
    ListSubnetRange listSubnetRange = null;
    ListDnsReverses dnsr = null;
    SaveAsTxt saveTxt = null;
    StatsUsage statUsage = null;
    PrefixSubLevels prefixSublevels = null;
    ListAssignedfromDB listAssignedfromdb = null;
    DatabaseUI dbUI = null;
    WhoisQuery whoisquery = null;
    CompressAddress compressAddr = null;
    ASNumberPlainDot asnpldot = null;
    //
    public static List<StageList> stageList = new ArrayList<StageList>();
    //
    GridPane grid = new GridPane();
    BorderPane root = new BorderPane();
    Scene scene = new Scene(root, 480, 500);
    Stage primaryStage;
    private static Label lbdbstatus = new Label("db=Down");
    HBox hbstatusstr = new HBox();
    //
    SEaddress StartEnd = new SEaddress();
    SEaddress subnets = new SEaddress();
    SEaddress page = new SEaddress();
    final int upto = 128;
    //
    public static BigInteger gotoasnValue = BigInteger.ZERO;
    public static BigInteger gotopfxValue = BigInteger.ZERO;
    public static String findpfx = "";
    //
    Text status = new Text(">");
    final Label addr = new Label("Address:");
    TextField input = new TextField("2001:db8:abcd:1234::");
    final Button buttonFind = new Button("Find");
    final Label labelipv6 = new Label("IPv6:");
    TextField ipv6addr = new TextField("");
    final Button buttonReset = new Button("Reset");
    final Label lbstart = new Label("Start:");
    final Label lb128 = new Label("128bits");
    final Label lbprefix = new Label("Prefix:");
    final Label lbsub = new Label("Subnet");
    final Label lbckendaddr = new Label("End?");
    final Label lbyg = new Label("A Free IPv6 Subnet Calculator by Yücel Güven         ");    
    final Slider sd1 = new Slider(1, 64, 1);
    final Slider sd2 = new Slider(1, 64, 1);
    public static int sd1Value = 0, sd2Value = 0;
    static TextField tfstartAddr = new TextField("");
    static TextField tfendAddr = new TextField("");
    CheckBox ck128 = new CheckBox();
    CheckBox cksub = new CheckBox();
    CheckBox ckendaddr = new CheckBox();
    final Button buttonPrevSpace = new Button("<");
    final Button buttonNextSpace = new Button(">");
    final Label lbaddrsp = new Label("Global Routing Prefix#:");
    final Label lbend = new Label("End:");
    Label lbaddrspno = new Label("#");
    Text c1 = new Text("/");
    Text c2 = new Text("/");
    final Button buttonFirstPage = new Button("|<< Prefixes");
    final Button buttonBack = new Button("<<");
    final Button buttonFwd = new Button(">>");
    final Button buttonLast = new Button(">>|");
    Label listcount = new Label("");
    HBox hblistcount = new HBox();
    //
    ContextMenu contextMenu = new ContextMenu();
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    //
    ListView<String> prefixlist = new ListView<String>();
    //
    TextField txtbinary = new TextField();
    TextFlow tf = new TextFlow();
    Label lb1 = new Label();
    Label lb2 = new Label();
    Label lb3 = new Label();
    //---status bar
    Label statusString = new Label("");
    public static BigInteger prefixmax = BigInteger.ZERO;
    public static BigInteger asnmax = BigInteger.ZERO;
    //
    MenuItem saveAs = new MenuItem("Save as Text...");
    MenuItem subPrefixes = new MenuItem("subLevels of the prefix...");
    MenuItem menuItemstatsUsage = new MenuItem("Statistics/Utilization of this range...");
    MenuItem Gotopfx = new MenuItem("Prefix Number");
    MenuItem Findpfx = new MenuItem("Find prefix");
    MenuItem workwithSelected = new MenuItem("work with selected Prefix");
    MenuItem sendPrefixtoDB = new MenuItem("Send prefix to database...");
    static Menu menuStages = new Menu("Stages");
    final MenuItem ListDNSrevs = new MenuItem("List All DNS Reverse Zones");
    //
    BigInteger currentidx = BigInteger.ZERO;
    BigInteger pix = BigInteger.ZERO;
    final Canvas canvas = new Canvas(130, 25);
    GraphicsContext gc = canvas.getGraphicsContext2D();
    double clearX = 3, clearY = 14, clearW = 125, clearH = 7;
    //DataBase
    public static Connection MySQLconnection = null;
    public static DBServerInfo dbserverInfo = new DBServerInfo();
    private static Statement statement = null;
    private static ResultSet resultSet = null;
    //
    //</editor-fold>

    public static void UpdateDbStatus() {
        if (MySQLconnection == null) {
            lbdbstatus.setText("db=Down ");
            DBClose();
            MsgBox.Show(Alert.AlertType.ERROR, "No Database connection!\r\nTry to connect from main menu");
        } else { // not null
            try {
                try {
                    statement = MySQLconnection.createStatement();
                    resultSet = statement.executeQuery("INFO_IPv6SubnettingTool__ConnectionTest123__;");
                } catch (Exception ex) {
                    if (ex.toString().contains("MySQLSyntaxErrorException")) {
                        /* Do Nothing. Just testing the connection. Intentional Syntax Error. */
                    } else {
                        MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                    }
                }
                Boolean isClosed = false; // Assuming conn. is open
                try {
                    isClosed = MySQLconnection.isClosed();
                } catch (Exception ex) {
                    MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                }

                if (!isClosed) {
                    lbdbstatus.setText("db=UP");
                    StatsUsage.lbdbstatus.setText("db=UP ");
                    ListAssignedfromDB.lbdbstatus.setText("db=UP ");
                    DatabaseUI.lbdbstatus.setText("db=UP ");
                    PrefixSubLevels.lbdbstatus.setText("db=UP ");
                    ListSubnetRange.lbdbstatus.setText("db=UP ");
                    //
                    StatsUsage.MySQLconnection = MySQLconnection;
                    ListAssignedfromDB.MySQLconnection = MySQLconnection;
                    DatabaseUI.MySQLconnection = MySQLconnection;
                    PrefixSubLevels.MySQLconnection = MySQLconnection;
                    ListSubnetRange.MySQLconnection = MySQLconnection;
                } else {
                    lbdbstatus.setText("db=Closed ");
                    DBClose();
                    MsgBox.Show(Alert.AlertType.ERROR, "Database connection lost!\r\nTry to reconnect from the menu");
                }
            } catch (Exception ex) {
                MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                Logger.getLogger(IPv6SubnettingTool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }    

    public static void DBClose() {
        try {
            //local
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (MySQLconnection != null) {
                MySQLconnection.close();
                MySQLconnection = null;
            }
            // StatsUsage Form
            try {
                if (StatsUsage.MySQLconnection != null) {
                    StatsUsage.MySQLconnection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(IPv6SubnettingTool.class.getName()).log(Level.SEVERE, null, ex);
            }
            StatsUsage.MySQLconnection = null;
            StatsUsage.lbdbstatus.setText("db=Down ");

            //ListAssignedfromDB Form
            try {
                if (ListAssignedfromDB.MySQLconnection != null) {
                    ListAssignedfromDB.MySQLconnection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(IPv6SubnettingTool.class.getName()).log(Level.SEVERE, null, ex);
            }
            ListAssignedfromDB.MySQLconnection = null;
            ListAssignedfromDB.lbdbstatus.setText("db=Down ");

            //DatabaseUI Form
            try {
                if (DatabaseUI.MySQLconnection != null) {
                    DatabaseUI.MySQLconnection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(IPv6SubnettingTool.class.getName()).log(Level.SEVERE, null, ex);
            }
            DatabaseUI.MySQLconnection = null;
            DatabaseUI.lbdbstatus.setText("db=Down ");

            //PrefixSubLevels Form
            try {
                if (PrefixSubLevels.MySQLconnection != null) {
                    PrefixSubLevels.MySQLconnection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(IPv6SubnettingTool.class.getName()).log(Level.SEVERE, null, ex);
            }
            PrefixSubLevels.MySQLconnection = null;
            PrefixSubLevels.lbdbstatus.setText("db=Down ");
            
            // ListSubnetRange Form
            try {
                if (ListSubnetRange.MySQLconnection != null) {
                    ListSubnetRange.MySQLconnection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(IPv6SubnettingTool.class.getName()).log(Level.SEVERE, null, ex);
            }
            ListSubnetRange.MySQLconnection = null;
            ListSubnetRange.lbdbstatus.setText("db=Down ");
            
        } catch (Exception ex) {
            MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
        }
    }

    public static void HeaderStageItems() {
        menuStages.getItems().clear();
        MenuItem closeall = new MenuItem("Close All Stages");
        menuStages.getItems().addAll(new SeparatorMenuItem(), closeall, new SeparatorMenuItem());
        //
        closeall.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int t = menuStages.getItems().size() - 1;
                StageList tmp = null;

                for (int k = t; k >= 3; k--) {
                    menuStages.getItems().remove(menuStages.getItems().get(k));
                    tmp = stageList.get(k - 3);
                    tmp.stage.close();
                }
                stageList.clear();
                menuStages.setText("Stages " + "(" + stageList.size() + ")");
            }
        });
    }

    public static void AddStageItem(String w, int hc) {

        MenuItem witem = new MenuItem(w.split("[.]")[1].split("[@]")[0]);
        menuStages.getItems().add(witem);
        menuStages.setText("Stages " + "(" + stageList.size() + ")");
        witem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                for (StageList i : stageList) {
                    if (i.hashcode == hc) {
                        i.stage.toFront();
                        if (i.stage.isIconified()) {
                            i.stage.setIconified(false);
                        }
                        i.stage.centerOnScreen();
                    }
                }
            }
        });
    }

    public static void RemoveStageItem(int stagehc) {

        StageList tmp = new StageList();
        for (StageList i : stageList) {
            if (i.hashcode == stagehc) {
                tmp = i;
            }
        }
        stageList.remove(tmp);
        //
        menuStages.setVisible(false);
        HeaderStageItems();

        for (StageList i : stageList) {
            MenuItem mi = new MenuItem(i.name.split("[.]")[1].split("[@]")[0].split("[$]")[0]);
            menuStages.getItems().add(mi);
            mi.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    for (StageList j : stageList) {
                        if (j.hashcode == i.hashcode) {
                            j.stage.toFront();
                            if (j.stage.isIconified()) {
                                j.stage.setIconified(false);
                            }
                            j.stage.centerOnScreen();
                        }
                    }
                }
            });
        }
        menuStages.setText("Stages " + "(" + stageList.size() + ")");
        menuStages.setVisible(true);
    }

    public void UpdateStatus() {
        int diff = (int) sd2.getValue() - (int) sd1.getValue();
        prefixmax = BigInteger.ONE.shiftLeft(diff);
        asnmax = BigInteger.ONE.shiftLeft((int) sd1.getValue());

        this.statusString.setText(" Delta=[" + String.valueOf(diff) + "]     "
                + "Prefixes=[" + String.valueOf(prefixmax) + "]     "
                + "Address Spaces=[" + String.valueOf(asnmax) + "]");
    }

    public void UpdatePrintBin() {

        txtbinary.setText(v6ST.PrintBin(StartEnd, (int) sd1.getValue(), ck128.isSelected()));

        tf.getChildren().clear();
        lb1.setText("");
        lb2.setText("");
        lb3.setText("");
        int count1 = (int) sd1.getValue() + ((int) sd1.getValue() / 4);
        int count2 = (int) sd2.getValue() + ((int) sd2.getValue() / 4);

        txtbinary.selectRange(0, count1);
        lb1.setText(txtbinary.selectedTextProperty().getValue());
        lb1.setStyle("-fx-background-color: #FF0000;-fx-text-fill: #FFFFFF;");
        txtbinary.selectRange(count1, count2);
        lb2.setText(txtbinary.selectedTextProperty().getValue());
        lb2.setStyle("-fx-background-color: #40E0D0;-fx-text-fill: #000000;");
        txtbinary.selectRange(count2, txtbinary.getLength());
        lb3.setText(txtbinary.selectedTextProperty().getValue());

        tf.getChildren().addAll(lb1, lb2, lb3);
    }

    public void UpdateCount() {
        if (prefixlist.getItems().isEmpty()) {
            listcount.setVisible(false);
            //return;
        } else {
            listcount.setVisible(true);
            if (ckendaddr.isSelected()) {
                listcount.setText("[" + String.valueOf(prefixlist.getItems().size() / 3 ) + " ent.]");
            } 
            else {
                listcount.setText("[" + String.valueOf(prefixlist.getItems().size()) + " ent.]");
            }
        }

        gc.clearRect(clearX, clearY, clearW, clearH);
        if (!prefixlist.getItems().isEmpty()) {
            currentidx
                    = new BigInteger(prefixlist.getItems().get(0).split(">")[0].replace("p", ""), 10);

            if ((prefixmax.shiftRight(7)).compareTo(BigInteger.ONE) >= 0) {
                pix = prefixmax.shiftRight(7);
            } else {
                pix = BigInteger.ONE.shiftLeft(7);
            }

            if (pix.compareTo(BigInteger.ZERO) > 0) {
                if ((prefixmax.subtract(currentidx)).compareTo(BigInteger.ONE.shiftLeft(7)) <= 0) {
                    gc.fillRect(2, 13, 126, 8);
                } else {
                    gc.fillRect(2, 13, ((currentidx.add(BigInteger.ONE.shiftLeft(7))).divide(pix)).doubleValue(), 8);
                }
            }
        }
    }

    public void SettingsAndEvents() {
        
        HeaderStageItems();
        menuStages.setText("Stages " + "(" + stageList.size() +")");
        input.setPrefWidth(300);
        input.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        buttonFind.setPrefWidth(75);
        //
        ipv6addr.setPrefWidth(300);
        ipv6addr.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        ipv6addr.setEditable(false);
        buttonReset.setPrefWidth(75);
        //
        tfstartAddr.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        tfstartAddr.setEditable(false);
        tfstartAddr.setPrefWidth(200);
        //
        buttonPrevSpace.setPrefWidth(30);
        buttonNextSpace.setPrefWidth(30);
        //
        tfendAddr.setFont(Font.font(java.awt.Font.MONOSPACED, 12));
        tfendAddr.setEditable(false);
        tfendAddr.setPrefWidth(200);
        //
        lbaddrspno.setFont(Font.font("Helvetica", 11));
        lbaddrspno.setTextAlignment(TextAlignment.RIGHT);
        lbaddrspno.setPrefWidth(135);
        //
        c1.setFont(Font.font("Tahoma", FontWeight.BOLD, 12));
        c2.setFont(Font.font("Tahoma", FontWeight.BOLD, 12));
        //
        sd1.setBlockIncrement(1);
        sd1.setPrefWidth(380);
        sd1.setDisable(true);
        sd1Value = (int) sd1.getValue();
        sd2Value = (int) sd2.getValue();
        sd2.setBlockIncrement(1);
        sd2.setPrefWidth(380);
        sd2.setDisable(true);
        //
        lb1.setFont(Font.font("Tahoma", FontWeight.THIN, 9.5));
        lb2.setFont(Font.font("Tahoma", FontWeight.THIN, 9.5));
        lb3.setFont(Font.font("Tahoma", FontWeight.THIN, 9.5));
        //
        ck128.setIndeterminate(false);
        cksub.setIndeterminate(false);
        ckendaddr.setIndeterminate(false);
        //
        lb128.setFont(Font.font("Tahoma", FontWeight.THIN, 9));
        lb128.setGraphic(ck128);
        lb128.setContentDisplay(ContentDisplay.RIGHT);
        //
        lbsub.setFont(Font.font("Tahoma", FontWeight.THIN, 9));
        lbsub.setGraphic(cksub);
        lbsub.setContentDisplay(ContentDisplay.RIGHT);
        //
        lbckendaddr.setFont(Font.font("Tahoma", FontWeight.THIN, 9));
        lbckendaddr.setGraphic(ckendaddr);
        lbckendaddr.setContentDisplay(ContentDisplay.RIGHT);        
        //
        lbyg.setFont(Font.font("Tahoma", FontWeight.NORMAL, 7));
        lbyg.setDisable(true);
        //
        buttonFirstPage.setDisable(true);
        buttonBack.setDisable(true);
        buttonFwd.setDisable(true);
        buttonLast.setDisable(true);
        buttonReset.setDisable(true);
        buttonPrevSpace.setDisable(true);
        buttonNextSpace.setDisable(true);
        //
        ck128.setDisable(true);
        cksub.setDisable(true);
        ckendaddr.setDisable(true);
        //
        c1.setText("/" + (int) sd1.getValue());
        c2.setText("/" + (int) sd2.getValue());
        //
        prefixlist.setMaxWidth(380);
        prefixlist.setMaxHeight(190);
        prefixlist.setContextMenu(contextMenu);
        prefixlist.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listcount.setStyle("-fx-text-fill: royalblue");
        hblistcount.setPrefWidth(54);
        //
        gc.setFill(Color.LIGHTGREY);
        gc.setStroke(Color.LIGHTGREY);
        gc.setLineWidth(2);
        gc.strokeRect(1, 12, 128, 10);
        //
        /* EventHandlers: */
        input.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                    Boolean oldValue, Boolean newValue) {
                if (!newValue) {
                    Calculate(input.getText());
                    prefixlist.getItems().clear();
                    gc.clearRect(clearX, clearY, clearW, clearH);
                    ck128.setDisable(false);
                    cksub.setDisable(false);
                    buttonReset.setDisable(false);
                    buttonPrevSpace.setDisable(false);
                    buttonNextSpace.setDisable(false);
                    buttonBack.setDisable(true);
                    buttonFwd.setDisable(true);
                    buttonLast.setDisable(true);
                    gc.clearRect(clearX, clearY, clearW, clearH);
                    listcount.setVisible(false);

                } else if (newValue) {
                    ipv6addr.setText("");
                }
            }
        });
        //
        buttonFind.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Calculate(input.getText());
                prefixlist.getItems().clear();
                ck128.setDisable(false);
                cksub.setDisable(false);
                buttonReset.setDisable(false);
                buttonPrevSpace.setDisable(false);
                buttonNextSpace.setDisable(false);
                buttonBack.setDisable(true);
                buttonFwd.setDisable(true);
                buttonLast.setDisable(true);
                gc.clearRect(clearX, clearY, clearW, clearH);
                listcount.setVisible(false);
            }
        });
        //
        sd1.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                    Number oldValue, Number newValue) {
                sd1.setValue(newValue.intValue());
                sd2.setValue(newValue.intValue());
                sd1Value = newValue.intValue();
                sd2Value = newValue.intValue();
                buttonBack.setDisable(true);
                buttonFwd.setDisable(true);
                buttonLast.setDisable(true);
                ckendaddr.setDisable(true);
                listcount.setVisible(false);
                prefixlist.getItems().clear();

                Calculate(input.getText());
                c1.setText("/" + newValue.intValue());

                UpdatePrintBin();
                UpdateStatus();
                UpdateCount();
            }
        });
        //
        sd2.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable,
                    Number oldValue, Number newValue) {
                buttonBack.setDisable(true);
                buttonFwd.setDisable(true);
                buttonLast.setDisable(true);
                ckendaddr.setDisable(true);
                listcount.setVisible(false);
                prefixlist.getItems().clear();

                if (newValue.intValue() - (int) sd1.getValue() < 0) {
                    sd2.setValue(sd1.getValue());
                    return;
                }
                sd2.setValue(newValue.intValue());
                sd2Value = newValue.intValue();
                StartEnd.subnetslash = newValue.intValue();
                c2.setText("/" + newValue.intValue());

                UpdatePrintBin();
                UpdateStatus();
                UpdateCount();
            }
        });
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
        prefixlist.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (t.getButton().equals(MouseButton.PRIMARY) && t.getClickCount() == 2) {
                    if (prefixlist.getSelectionModel().getSelectedItem() != null) {
                        StartEnd.slash = (int) sd1.getValue();
                        StartEnd.subnetslash = (int) sd2.getValue();
                        if (listSubnetRange == null) {
                            listSubnetRange = new ListSubnetRange(StartEnd,
                                    prefixlist.getSelectionModel().getSelectedItem(),
                                    ck128.isSelected(), MySQLconnection, dbserverInfo);
                            stageList.add(new StageList(listSubnetRange.toString(),
                                    listSubnetRange.stage, listSubnetRange.stage.hashCode()));
                            AddStageItem(listSubnetRange.toString(), listSubnetRange.stage.hashCode());
                        } else {
                            if (!listSubnetRange.stage.isShowing()) {
                            stageList.add(new StageList(listSubnetRange.toString(),
                                    listSubnetRange.stage, listSubnetRange.stage.hashCode()));
                            AddStageItem(listSubnetRange.toString(), listSubnetRange.stage.hashCode());
                            }
                            listSubnetRange.SetNewValues(StartEnd,
                                    prefixlist.getSelectionModel().getSelectedItem(),
                                    ck128.isSelected());
                            listSubnetRange.StageShow();
                        }
                    }
                }
            }
        });
        //
        prefixlist.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if (key.getCode() == KeyCode.ESCAPE) {
                    Platform.exit();
                } else if (key.getCode() == KeyCode.ENTER) {
                    if (!prefixlist.getSelectionModel().isEmpty()
                            || prefixlist.getSelectionModel().getSelectedItem() != null) {
                        StartEnd.slash = (int) sd1.getValue();
                        StartEnd.subnetslash = (int) sd2.getValue();
                        if (listSubnetRange == null) {
                            listSubnetRange = new ListSubnetRange(StartEnd,
                                    prefixlist.getSelectionModel().getSelectedItem(),
                                    ck128.isSelected(), MySQLconnection, dbserverInfo);
                            stageList.add(new StageList(listSubnetRange.toString(),
                                    listSubnetRange.stage, listSubnetRange.stage.hashCode()));
                            AddStageItem(listSubnetRange.toString(), listSubnetRange.stage.hashCode());
                        } else {
                            if (!listSubnetRange.stage.isShowing()) {
                                stageList.add(new StageList(listSubnetRange.toString(),
                                        listSubnetRange.stage, listSubnetRange.stage.hashCode()));
                                AddStageItem(listSubnetRange.toString(), listSubnetRange.stage.hashCode());
                            }
                            listSubnetRange.SetNewValues(StartEnd,
                                    prefixlist.getSelectionModel().getSelectedItem(),
                                    ck128.isSelected());
                            listSubnetRange.StageShow();
                        }
                    }
                }
            }
        });
        //
        ck128.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean old_val, Boolean new_val) {
                if (new_val) {
                    ExpandStage();
                } else {
                    DefaultStage();
                }
                Calculate(input.getText());
            }
        });
        //
        cksub.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean old_val, Boolean new_val) {
                if (new_val) {
                    sd2.setDisable(false);
                    buttonFirstPage.setDisable(false);

                } else {
                    sd2.setDisable(true);
                    sd2.setValue((int) sd1.getValue());
                    buttonFirstPage.setDisable(true);
                    buttonBack.setDisable(true);
                    buttonFwd.setDisable(true);
                    buttonLast.setDisable(true);
                }
            }
        });
        //
        ckendaddr.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean old_val, Boolean new_val) {
                
                String first = prefixlist.getItems().get(0).split(" ")[1].split("/")[0];
                SEaddress tmpse = new SEaddress();
                tmpse.Start = v6ST.FormalizeAddr(first);
                tmpse = v6ST.Subnetting(tmpse, ck128.isSelected());

                prefixlist.getItems().clear();

                if (!ck128.isSelected() && (int) sd2.getValue() == 64
                    ||
                    ck128.isSelected() && (int) sd2.getValue() == 128
                    )
                {
                    ckendaddr.setSelected(false);
                    ckendaddr.setDisable(true);
                }
                else
                    ckendaddr.setDisable(false);

                tmpse.slash = (int) sd1.getValue();
                tmpse.subnetslash = (int) sd2.getValue();
                tmpse.upto = upto;
                tmpse.UpperLimitAddress = StartEnd.End;               

                subnets = v6ST.ListFirstPage(tmpse, ck128.isSelected(), ckendaddr.isSelected());
                page.End = subnets.End;

                prefixlist.setItems(subnets.liste);
                UpdateCount();
            }
        });        
        //
        buttonReset.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ck128.setSelected(false);
                ck128.setDisable(true);
                cksub.setSelected(false);
                cksub.setDisable(true);
                ckendaddr.setSelected(false);
                ckendaddr.setDisable(true);                
                sd1.setValue(1);
                sd1.setDisable(true);
                sd2.setValue(1);
                sd2.setDisable(true);
                ipv6addr.setText("");
                tfstartAddr.setText("");
                tfendAddr.setText("");
                lbaddrspno.setText("");
                buttonFirstPage.setDisable(true);
                buttonBack.setDisable(true);
                buttonFwd.setDisable(true);
                buttonLast.setDisable(true);
                listcount.setVisible(false);
                buttonPrevSpace.setDisable(true);
                buttonNextSpace.setDisable(true);
                prefixlist.getItems().clear();

            }
        });
        //
        buttonFirstPage.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!ck128.isSelected() && (int) sd2.getValue() == 64
                    ||
                    ck128.isSelected() && (int) sd2.getValue() == 128
                    )
                {
                    ckendaddr.setSelected(false);
                    ckendaddr.setDisable(true);
                }
                else
                    ckendaddr.setDisable(false);

                listcount.setVisible(true);
                prefixlist.getItems().clear();
                int delta = (int) sd2.getValue() - (int) sd1.getValue();

                StartEnd.slash = (int) sd1.getValue();
                StartEnd.subnetslash = (int) sd2.getValue();
                StartEnd.upto = upto;

                subnets = v6ST.ListFirstPage(StartEnd, ck128.isSelected(), ckendaddr.isSelected());
                page.End = subnets.End;

                prefixlist.setItems(subnets.liste);

                BigInteger maxsub = BigInteger.ONE.shiftLeft(delta);
                if (maxsub.compareTo(BigInteger.valueOf(upto)) <= 0) {
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
        //
        buttonBack.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                prefixlist.getItems().clear();
                subnets.slash = (int) sd1.getValue();
                subnets.subnetslash = (int) sd2.getValue();;
                subnets.upto = upto;
                subnets.LowerLimitAddress = StartEnd.LowerLimitAddress;
                subnets.UpperLimitAddress = StartEnd.UpperLimitAddress;

                subnets.End = page.End = page.Start.subtract(BigInteger.ONE);
                subnets = v6ST.ListPageBackward(subnets, ck128.isSelected(), ckendaddr.isSelected());
                page.Start = subnets.Start;

                prefixlist.setItems(subnets.liste);

                if (subnets.Start.equals(StartEnd.Start)) {
                    buttonBack.setDisable(true);
                    buttonFwd.setDisable(false);
                    buttonLast.setDisable(false);
                    UpdateCount();
                    return;
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
                prefixlist.getItems().clear();
                subnets.slash = (int) sd1.getValue();
                subnets.subnetslash = (int) sd2.getValue();;
                subnets.upto = upto;
                subnets.LowerLimitAddress = StartEnd.LowerLimitAddress;
                subnets.UpperLimitAddress = StartEnd.UpperLimitAddress;

                subnets.Start = page.Start = page.End.add(BigInteger.ONE);
                subnets = v6ST.ListPageForward(subnets, ck128.isSelected(), ckendaddr.isSelected());
                page.End = subnets.End;

                prefixlist.setItems(subnets.liste);

                if (subnets.End.equals(StartEnd.End)) {
                    buttonFwd.setDisable(true);
                    buttonLast.setDisable(true);
                    buttonBack.setDisable(false);
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
                prefixlist.getItems().clear();
                subnets.slash = (int) sd1.getValue();
                subnets.subnetslash = (int) sd2.getValue();
                subnets.upto = upto;
                subnets.LowerLimitAddress = StartEnd.LowerLimitAddress;
                subnets.UpperLimitAddress = StartEnd.UpperLimitAddress;

                subnets.End = page.End = StartEnd.UpperLimitAddress;
                subnets = v6ST.ListLastPage(subnets, ck128.isSelected(), ckendaddr.isSelected());
                page.Start = subnets.Start;

                prefixlist.setItems(subnets.liste);

                buttonFwd.setDisable(true);
                buttonLast.setDisable(true);
                buttonBack.setDisable(false);

                UpdateCount();
            }
        });

        buttonPrevSpace.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                listcount.setVisible(false);
                if (StartEnd.Start.equals(BigInteger.ZERO)) {
                    StartEnd.End = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
                } else {
                    StartEnd.End = StartEnd.Start.subtract(BigInteger.ONE);
                }

                StartEnd = v6ST.PrevSpace(StartEnd, ck128.isSelected());

                DisplayPrevNextSpace();
                UpdatePrintBin();
                gc.clearRect(clearX, clearY, clearW, clearH);
            }
        });

        buttonNextSpace.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                listcount.setVisible(false);
                if (StartEnd.End.equals(new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16))) {
                    StartEnd.Start = BigInteger.ZERO;
                } else {
                    StartEnd.Start = StartEnd.End.add(BigInteger.ONE);
                }

                StartEnd = v6ST.NextSpace(StartEnd, ck128.isSelected());

                DisplayPrevNextSpace();
                UpdatePrintBin();
                gc.clearRect(clearX, clearY, clearW, clearH);
            }
        });
        //--- Context Menu
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
        MenuItem contextitemListRevDNS = new MenuItem("List All DNS Reverse Zones");
        contextitemListRevDNS.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                ListDNSrevs.fire();
            }
        });        
        MenuItem contextitemGotopfx = new MenuItem("Go to Prefix...");
        contextitemGotopfx.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                Gotopfx.fire();
            }
        });
        MenuItem contextitemFindpfx = new MenuItem("Find Prefix...");
        contextitemFindpfx.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                Findpfx.fire();
            }
        });
        MenuItem contextitemworkWithSelected = new MenuItem("work with selected Prefix");
        contextitemworkWithSelected.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                workwithSelected.fire();
            }
        });
        MenuItem contextitemSendtodb = new MenuItem("Send Prefix to Database...");
        contextitemSendtodb.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                sendPrefixtoDB.fire();
            }
        });
        MenuItem contextitemsubPrefixes = new MenuItem("subLevels of the prefix...");
        contextitemsubPrefixes.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                subPrefixes.fire();
            }
        });
        MenuItem contextitemstatsUsage = new MenuItem("Statistics/Utilization of this range...");
        contextitemstatsUsage.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                menuItemstatsUsage.fire();
            }
        });
        MenuItem contextitemSaveas = new MenuItem("Save As Text...");
        contextitemSaveas.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                saveAs.fire();
            }
        });
        contextMenu.getItems().addAll(contextitemSelectall, contextitemCopy,
                contextitemSaveas, new SeparatorMenuItem(), contextitemListRevDNS,
                contextitemGotopfx, contextitemFindpfx, contextitemworkWithSelected,
                new SeparatorMenuItem(), contextitemSendtodb, contextitemsubPrefixes,
                contextitemstatsUsage);        
        contextMenu.setOnShowing(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                
                if (prefixlist.getItems().isEmpty()) {
                    contextitemSaveas.setDisable(true);
                    contextitemListRevDNS.setDisable(true);
                    contextitemGotopfx.setDisable(true);
                    contextitemFindpfx.setDisable(true);
                    contextitemworkWithSelected.setDisable(true);
                } else {
                    contextitemSaveas.setDisable(false);
                    contextitemListRevDNS.setDisable(false);
                    contextitemGotopfx.setDisable(false);
                    contextitemFindpfx.setDisable(false);
                    contextitemworkWithSelected.setDisable(false);
                }
                if (MySQLconnection == null) {
                    contextitemSendtodb.setDisable(true);
                    contextitemsubPrefixes.setDisable(true);
                    contextitemstatsUsage.setDisable(true);
                } else {
                    contextitemSendtodb.setDisable(false);
                    contextitemsubPrefixes.setDisable(false);
                    contextitemstatsUsage.setDisable(false);
                }
            }
        });
    } //Settings

    public void Calculate(String sin) {
        if (v6ST.IsAddressCorrect(sin)) {
            status.setText(v6ST.errmsg);
            StartEnd.Resultv6 = v6ST.FormalizeAddr(sin);
            String s = v6ST.Kolonlar(StartEnd.Resultv6);
            ipv6addr.setText(s);

            StartEnd.slash = (int) sd1.getValue();
            StartEnd.subnetslash = (int) sd2.getValue();

            StartEnd = v6ST.StartEndAddresses(StartEnd, ck128.isSelected());
            subnets.Start = StartEnd.Start;
            subnets.End = BigInteger.ZERO;
            subnets.LowerLimitAddress = StartEnd.LowerLimitAddress;
            subnets.UpperLimitAddress = StartEnd.UpperLimitAddress;

            String s0 = v6ST.Kolonlar(StartEnd.Start);
            String s1 = v6ST.Kolonlar(StartEnd.End);
            if (ck128.isSelected()) {
                tfstartAddr.setText(s0 + "/" + String.valueOf((int) sd1.getValue()));
                tfendAddr.setText(s1 + "/" + String.valueOf((int) sd2.getValue()));
            } else {
                /* 64 bits*/
                s0 = s0.substring(0, 19);
                tfstartAddr.setText(s0 + "::/"
                        + String.valueOf((int) sd1.getValue()));
                s1 = s1.substring(0, 19);
                tfendAddr.setText(s1 + "::/"
                        + String.valueOf((int) sd1.getValue()));
            }
            UpdateStatus();
            UpdatePrintBin();
            StartEnd = v6ST.NextSpace(StartEnd, ck128.isSelected());
            lbaddrspno.setText("#" + String.valueOf(StartEnd.subnetidx));
            sd1.setDisable(false);
        } else {
            status.setText(v6ST.errmsg);
            ck128.setSelected(false);
            sd1.setValue(1);
            sd1.setMax(64);
            cksub.setSelected(false);
            sd2.setValue(1);
            sd2.setDisable(true);
            sd2.setMax(64);
            StartEnd.Initialize();

            ipv6addr.setText("");
            tfstartAddr.setText("");
            tfendAddr.setText("");
        }
    }

    public void DisplayPrevNextSpace() {

        prefixlist.getItems().clear();
        buttonBack.setDisable(true);
        buttonFwd.setDisable(true);
        buttonLast.setDisable(true);

        lbaddrspno.setText("#" + String.valueOf(StartEnd.subnetidx));

        subnets.Start = StartEnd.Start;
        subnets.End = BigInteger.ZERO;
        StartEnd.Resultv6 = StartEnd.Start;

        String s = v6ST.Kolonlar(StartEnd.Start);

        if (ck128.isSelected()) {
            if (s.length() > 32) {
                s = s.substring(0, 39);
                s += "/" + (int) sd1.getValue();
            }
        } else if (!ck128.isSelected()) {
            if (s.length() > 16) {
                s = s.substring(0, 19);
                s += "::/" + (int) sd1.getValue();
            }
        }

        ipv6addr.setText(v6ST.Kolonlar(StartEnd.Start));
        tfstartAddr.setText(s);

        s = v6ST.Kolonlar(StartEnd.End);
        if (ck128.isSelected()) {
            if (s.length() > 32) {
                s = s.substring(0, 39);
                s += "/" + (int) sd1.getValue();
            }
        }
        if (!ck128.isSelected()) {
            if (s.length() > 16) {
                s = s.substring(0, 19);
                s += "::/" + (int) sd1.getValue();
            }
        }
        tfendAddr.setText(s);
    }

    public void ExpandStage() {
        this.primaryStage.setWidth(875);
        this.primaryStage.setHeight(530);
        tfstartAddr.setPrefWidth(380);
        tfendAddr.setPrefWidth(380);
        sd1.setPrefWidth(770);
        sd1.setMax(128);
        sd2.setPrefWidth(770);
        sd2.setMax(128);
        prefixlist.setMaxHeight(190);
        prefixlist.setMaxWidth(770);
        lbaddrspno.setPrefWidth(340);
        hblistcount.setPrefWidth(444);
        hbstatusstr.setPrefWidth(780);
    }

    public void DefaultStage() {
        this.primaryStage.setWidth(480);
        this.primaryStage.setHeight(530);
        tfstartAddr.setPrefWidth(200);
        tfendAddr.setPrefWidth(200);
        sd1.setPrefWidth(380);
        sd1.setMax(64);
        sd2.setPrefWidth(380);
        sd2.setMax(64);
        prefixlist.setMaxHeight(190);
        prefixlist.setMaxWidth(380);
        lbaddrspno.setPrefWidth(135);
        hblistcount.setPrefWidth(54);
        hbstatusstr.setPrefWidth(410);
    }

    public MenuBar addMenuBar() {
        MenuBar menuBar = new MenuBar();
        // --- Menu File
        Menu menuFile = new Menu("File");
        MenuItem exit = new MenuItem("Exit");
        menuFile.getItems().addAll(new SeparatorMenuItem(), saveAs, new SeparatorMenuItem(), exit);
        exit.setAccelerator(KeyCombination.keyCombination("Ctrl+X"));
        exit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                DBClose();
                Platform.exit();
            }
        });
        menuFile.setOnShowing(new EventHandler<Event>() {
            @Override
            public void handle(Event t) {
                if (!prefixlist.getItems().isEmpty()) {
                    saveAs.setDisable(false);
                } else {
                    saveAs.setDisable(true);
                }
            }
        });
        // --- Menu Tools
        Menu menuTools = new Menu("Tools");
        final MenuItem List64 = new MenuItem("List /64 Prefixes");
        final MenuItem List128 = new MenuItem("List /128 Addresses");
        MenuItem whoisQuery = new MenuItem("whois Query");
        MenuItem compress = new MenuItem("Compress/Uncompress address...");
        MenuItem asnplaindot = new MenuItem("AS Number plain/dot Conversion");
        menuTools.getItems().addAll(new SeparatorMenuItem(), List64, List128,
                ListDNSrevs, whoisQuery, compress, asnplaindot);
        //
        menuTools.setOnShowing(new EventHandler<Event>() {
            @Override
            public void handle(Event t) {
                if (!prefixlist.getItems().isEmpty()) {
                    ListDNSrevs.setDisable(false);
                    String s = prefixlist.getSelectionModel().getSelectedItem();

                    if (s != null) {
                        if (((int) sd2.getValue() == 64 && !ck128.isSelected())
                                || ((int) sd2.getValue() == 128 && ck128.isSelected())) {
                            List64.setDisable(true);
                            List128.setDisable(true);
                        } else {
                            if (!ck128.isSelected()) {
                                List64.setDisable(false);
                            } else {
                                List64.setDisable(true);
                            }

                            if (ck128.isSelected()) {
                                List128.setDisable(false);
                            } else {
                                List128.setDisable(true);
                            }
                        }
                    } else {
                        List64.setDisable(true);
                        List128.setDisable(true);
                    }
                } else {
                    List64.setDisable(true);
                    List128.setDisable(true);
                    ListDNSrevs.setDisable(true);
                }
            }
        });
        List64.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                String s = prefixlist.getSelectionModel().getSelectedItem();
                if ((!ck128.isSelected() && (int) sd2.getValue() == 64)
                        || (ck128.isSelected() && (int) sd2.getValue() == 128)
                        || s == null) {
                    return;
                } else {
                    StartEnd.slash = (int) sd1.getValue();
                    StartEnd.subnetslash = (int) sd2.getValue();
                    if (listSubnetRange == null) {
                        listSubnetRange = new ListSubnetRange(StartEnd,
                                prefixlist.getSelectionModel().getSelectedItem(),
                                ck128.isSelected(), MySQLconnection, dbserverInfo);
                        stageList.add(new StageList(listSubnetRange.toString(),
                                listSubnetRange.stage, listSubnetRange.stage.hashCode()));
                        AddStageItem(listSubnetRange.toString(), listSubnetRange.stage.hashCode());
                    } else {
                        if (!listSubnetRange.stage.isShowing()) {
                            stageList.add(new StageList(listSubnetRange.toString(),
                                    listSubnetRange.stage, listSubnetRange.stage.hashCode()));
                            AddStageItem(listSubnetRange.toString(), listSubnetRange.stage.hashCode());
                        }
                        listSubnetRange.SetNewValues(StartEnd,
                                prefixlist.getSelectionModel().getSelectedItem(),
                                ck128.isSelected());
                        listSubnetRange.StageShow();
                    }
                }
            }
        });
        List128.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                String s = prefixlist.getSelectionModel().getSelectedItem();
                if ((!ck128.isSelected() && (int) sd2.getValue() == 64)
                        || (ck128.isSelected() && (int) sd2.getValue() == 128)
                        || s == null) {
                    return;
                } else {
                    StartEnd.slash = (int) sd1.getValue();
                    StartEnd.subnetslash = (int) sd2.getValue();
                    if (listSubnetRange == null) {
                        listSubnetRange = new ListSubnetRange(StartEnd,
                                prefixlist.getSelectionModel().getSelectedItem(),
                                ck128.isSelected(), MySQLconnection, dbserverInfo);
                        stageList.add(new StageList(listSubnetRange.toString(),
                                listSubnetRange.stage, listSubnetRange.stage.hashCode()));
                        AddStageItem(listSubnetRange.toString(), listSubnetRange.stage.hashCode());
                    } else {
                        if (!listSubnetRange.stage.isShowing()) {
                            stageList.add(new StageList(listSubnetRange.toString(),
                                    listSubnetRange.stage, listSubnetRange.stage.hashCode()));
                            AddStageItem(listSubnetRange.toString(), listSubnetRange.stage.hashCode());
                        }
                        listSubnetRange.SetNewValues(StartEnd,
                                prefixlist.getSelectionModel().getSelectedItem(),
                                ck128.isSelected());
                        listSubnetRange.StageShow();
                    }
                }
            }
        });
        ListDNSrevs.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (!prefixlist.getItems().isEmpty()) {
                    StartEnd.slash = (int) sd1.getValue();
                    StartEnd.subnetslash = (int) sd2.getValue();
                    
                    if (dnsr == null) {
                        dnsr = new ListDnsReverses(StartEnd, ck128.isSelected());
                        stageList.add(new StageList(dnsr.toString(), dnsr.stage, dnsr.stage.hashCode()));
                        AddStageItem(dnsr.toString(), dnsr.stage.hashCode());
                    } else {
                        if (!dnsr.stage.isShowing()) {
                            stageList.add(new StageList(dnsr.toString(), dnsr.stage, dnsr.stage.hashCode()));
                            AddStageItem(dnsr.toString(), dnsr.stage.hashCode());
                        }
                        dnsr.SetNewValues(StartEnd, ck128.isSelected());
                        dnsr.StageShow();
                    }
                }
            }
        });
        whoisQuery.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (whoisquery == null) {
                    whoisquery = new WhoisQuery(v6ST.CompressAddress(ipv6addr.getText()));
                    stageList.add(new StageList(whoisquery.toString(),
                            whoisquery.stage, whoisquery.stage.hashCode()));
                    AddStageItem(whoisquery.toString(), whoisquery.stage.hashCode());
                } else {
                    if (!whoisquery.stage.isShowing()) {
                        stageList.add(new StageList(whoisquery.toString(),
                                whoisquery.stage, whoisquery.stage.hashCode()));
                        AddStageItem(whoisquery.toString(), whoisquery.stage.hashCode());
                    }
                    whoisquery.StageShow();
                }
            }
        });
        compress.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (compressAddr == null) {
                    compressAddr = new CompressAddress();
                stageList.add(new StageList(compressAddr.toString(),
                        compressAddr.stage, compressAddr.stage.hashCode()));
                AddStageItem(compressAddr.toString(), compressAddr.stage.hashCode());
                } else {
                    if (!compressAddr.stage.isShowing()) {
                        stageList.add(new StageList(compressAddr.toString(),
                                compressAddr.stage, compressAddr.stage.hashCode()));
                        AddStageItem(compressAddr.toString(), compressAddr.stage.hashCode());
                    }
                    compressAddr.StageShow();
                }
            }
        });
        asnplaindot.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (asnpldot == null) {
                    asnpldot = new ASNumberPlainDot();
                    stageList.add(new StageList(asnpldot.toString(), asnpldot.stage, asnpldot.stage.hashCode()));
                    AddStageItem(asnpldot.toString(), asnpldot.stage.hashCode());
                } else {
                    if (!asnpldot.stage.isShowing()) {
                        stageList.add(new StageList(asnpldot.toString(),
                                asnpldot.stage, asnpldot.stage.hashCode()));
                        AddStageItem(asnpldot.toString(), asnpldot.stage.hashCode());
                    }
                    asnpldot.StageShow();
                }
            }
        });
        // --- Menu Goto
        Menu menuGoto = new Menu("Go to...");
        MenuItem addrSpNo = new MenuItem("Addr.Space Number");
        menuGoto.getItems().addAll(new SeparatorMenuItem(), addrSpNo, Gotopfx,
                Findpfx, workwithSelected);
        menuGoto.setOnShowing(new EventHandler<Event>() {
            @Override
            public void handle(Event t) {
                if (prefixlist.getItems().isEmpty()) {
                    Gotopfx.setDisable(true);
                    Findpfx.setDisable(true);
                } else {
                    Gotopfx.setDisable(false);
                    Findpfx.setDisable(false);
                }
            }
        });
        addrSpNo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {

                String currentasnidx = lbaddrspno.getText().replace("#", "");
                gotoasnValue = StartEnd.subnetidx;

                Goto gasn = new Goto(ID, 0);

                if (currentasnidx.equals(String.valueOf(gotoasnValue))) {
                    return;
                } else {
                    lbaddrspno.setText("#" + String.valueOf(gotoasnValue));
                }

                StartEnd.subnetidx = gotoasnValue;

                StartEnd = v6ST.GoToAddrSpace(StartEnd, ck128.isSelected());
                StartEnd.Resultv6 = StartEnd.Start;

                prefixlist.getItems().clear();

                String s = v6ST.Kolonlar(StartEnd.Start);
                ipv6addr.setText(s);
                if (ck128.isSelected()) {
                    s = s + "/" + String.valueOf((int) sd1.getValue());
                }
                if (!ck128.isSelected()) {
                    s = s.substring(0, 19) + "::/" + String.valueOf((int) sd1.getValue());
                }

                tfstartAddr.setText(s);
                s = v6ST.Kolonlar(StartEnd.End);

                if (ck128.isSelected()) {
                    s = s + "/" + String.valueOf((int) sd1.getValue());
                }
                if (!ck128.isSelected()) {
                    s = s.substring(0, 19) + "::/" + String.valueOf((int) sd1.getValue());
                }
                tfendAddr.setText(s);
                UpdatePrintBin();
                buttonFwd.setDisable(true);
                buttonBack.setDisable(true);
                buttonLast.setDisable(true);
            }
        });
        Gotopfx.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {

                if (prefixlist.getItems().isEmpty()) {
                    return;
                }

                String currentpfxidx = prefixlist.getItems().get(0).split(">")[0].replace("p", "");
                gotopfxValue = new BigInteger(currentpfxidx);

                Goto gasn = new Goto(ID, 1);

                if (currentpfxidx.equals(String.valueOf(gotopfxValue))) {
                    return;
                }

                String ss = "", se = "";
                int count = 0;

                subnets.subnetidx = gotopfxValue;
                subnets.slash = (int) sd1.getValue();
                subnets.subnetslash = (int) sd2.getValue();
                subnets.Start = StartEnd.Start;
                subnets.Resultv6 = StartEnd.Resultv6;

                subnets = v6ST.GoToSubnet(subnets, ck128.isSelected());
                //
                page.Start = subnets.Start;
                page.End = BigInteger.ZERO;

                if (subnets.End.equals(StartEnd.End)) {
                    buttonFwd.setDisable(true);
                }

                prefixlist.getItems().clear();

                for (count = 0; count < upto; count++) {
                    subnets = v6ST.Subnetting(subnets, ck128.isSelected());

                    if (ck128.isSelected()) {
                        ss = v6ST.Kolonlar(subnets.Start);
                        ss = v6ST.CompressAddress(ss);
                        ss = "p" + subnets.subnetidx + "> " + ss + "/"
                                + String.valueOf((int) sd2.getValue());
                        prefixlist.getItems().add(ss);
                        //
                        if (ckendaddr.isSelected()) {
                            se = v6ST.Kolonlar(subnets.End);
                            se = v6ST.CompressAddress(se);
                            se = "e" + subnets.subnetidx + "> " + se + "/"
                                    + String.valueOf((int) sd2.getValue());
                            prefixlist.getItems().add(se);
                            prefixlist.getItems().add("");
                        }
                        
                    } else if (!ck128.isSelected()) {
                        ss = v6ST.Kolonlar(subnets.Start);
                        ss = ss.substring(0, 19);
                        ss = ss + "::";
                        ss = v6ST.CompressAddress(ss);
                        ss = "p" + subnets.subnetidx + "> " + ss + "/"
                                + String.valueOf((int) sd2.getValue());

                        prefixlist.getItems().add(ss);
                        //
                        if (ckendaddr.isSelected()) {
                            se = v6ST.Kolonlar(subnets.End);
                            se = se.substring(0, 19);
                            se = se + "::";
                            se = v6ST.CompressAddress(se);
                            se = "e" + subnets.subnetidx + "> " + se + "/"
                                    + String.valueOf((int) sd2.getValue());
                            prefixlist.getItems().add(se);
                            prefixlist.getItems().add("");
                        }
                    }

                    if (subnets.End.equals(StartEnd.End)) {
                        buttonFwd.setDisable(true);
                        break;
                    } else {
                        subnets.Start = subnets.End.add(BigInteger.ONE);
                    }
                }
                page.End = subnets.End;

                if (gotopfxValue.equals(BigInteger.ZERO)) {
                    buttonBack.setDisable(true);
                } else {
                    buttonBack.setDisable(false);
                }

                if (subnets.subnetidx.equals(prefixmax.subtract(BigInteger.ONE))) {
                    buttonFwd.setDisable(true);
                    buttonLast.setDisable(true);
                } else {
                    buttonFwd.setDisable(false);
                    buttonLast.setDisable(false);
                }
                UpdateCount();
                UpdatePrintBin();
            }
        });
        Findpfx.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {

                Goto gasn = new Goto(ID, 2);

                if (findpfx.equals("")
                        || (int) sd1.getValue() == (int) sd2.getValue()) {
                    return;
                }

                SEaddress seaddr = new SEaddress();
                seaddr.slash = (int) sd1.getValue();
                seaddr.subnetslash = (int) sd2.getValue();
                String ss = "", se = "";
                int count = 0;

                BigInteger Resv6 = v6ST.FormalizeAddr(findpfx);
                seaddr.Resultv6 = seaddr.Start = Resv6;

                if (seaddr.Resultv6.compareTo(StartEnd.Start) >= 0
                        && seaddr.Resultv6.compareTo(StartEnd.End) <= 0) {
                    // inside
                    BigInteger before = seaddr.Resultv6;

                    seaddr = v6ST.FindPrefixIndex(seaddr, ck128.isSelected());

                    subnets.subnetidx = seaddr.subnetidx;
                    subnets.slash = (int) sd1.getValue();
                    subnets.subnetslash = (int) sd2.getValue();
                    subnets.Start = StartEnd.Start;
                    subnets.Resultv6 = StartEnd.Resultv6;

                    subnets = v6ST.GoToSubnet(subnets, ck128.isSelected());

                    if (before.equals(subnets.Start)) {
                        page.Start = subnets.Start;
                        page.End = BigInteger.ZERO;

                        if (subnets.End.equals(StartEnd.End)) {
                            buttonFwd.setDisable(true);
                        }

                        prefixlist.getItems().clear();
                        for (count = 0; count < upto; count++) {
                            subnets = v6ST.Subnetting(subnets, ck128.isSelected());

                            if (ck128.isSelected()) {
                                ss = v6ST.Kolonlar(subnets.Start);
                                ss = v6ST.CompressAddress(ss);
                                ss = "p" + subnets.subnetidx + "> " + ss + "/"
                                        + String.valueOf((int) sd2.getValue());

                                prefixlist.getItems().add(ss);
                                //
                                if (ckendaddr.isSelected()) {
                                    se = v6ST.Kolonlar(subnets.End);
                                    se = v6ST.CompressAddress(se);
                                    se = "e" + subnets.subnetidx + "> " + se + "/"
                                            + String.valueOf((int) sd2.getValue());
                                    prefixlist.getItems().add(se);
                                    prefixlist.getItems().add("");
                                }                                
                                
                            } else if (!ck128.isSelected()) {
                                ss = v6ST.Kolonlar(subnets.Start);
                                ss = ss.substring(0, 19);
                                ss += "::";
                                ss = v6ST.CompressAddress(ss);
                                ss = "p" + subnets.subnetidx + "> " + ss + "/"
                                        + String.valueOf((int) sd2.getValue());
                                prefixlist.getItems().add(ss);
                                //
                                if (ckendaddr.isSelected()) {
                                    se = v6ST.Kolonlar(subnets.End);
                                    se = se.substring(0, 19);
                                    se = se + "::";
                                    se = v6ST.CompressAddress(se);
                                    se = "e" + subnets.subnetidx + "> " + se + "/"
                                            + String.valueOf((int) sd2.getValue());
                                    prefixlist.getItems().add(se);
                                    prefixlist.getItems().add("");
                                }
                            }

                            if (subnets.End.equals(StartEnd.End)) {
                                buttonFwd.setDisable(true);
                                break;
                            } else {
                                subnets.Start = subnets.End.add(BigInteger.ONE);
                            }
                        }
                        page.End = subnets.End;

                        if (seaddr.subnetidx.equals(BigInteger.ZERO)) {
                            buttonBack.setDisable(true);
                        } else {
                            buttonBack.setDisable(false);
                        }
                        if (subnets.subnetidx.equals(prefixmax.subtract(BigInteger.ONE))) {
                            buttonFwd.setDisable(true);
                            buttonLast.setDisable(true);
                        } else {
                            buttonFwd.setDisable(false);
                            buttonLast.setDisable(false);
                        }
                        UpdateCount();
                        UpdatePrintBin();
                    } else {
                        MsgBox.Show(Alert.AlertType.ERROR, "Prefix Not Found!");

                    }
                } else {
                    MsgBox.Show(Alert.AlertType.ERROR, "Out of [Start-End] interval!");
                }
            }
        });
        workwithSelected.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (!prefixlist.getItems().isEmpty() && prefixlist.getSelectionModel().getSelectedItem() != null) {
                    String selected = prefixlist.getSelectionModel().getSelectedItem().split(" ")[1];
                    String snet = selected.split("/")[0];
                    int plen = Integer.parseInt(selected.split("/")[1]);
                    input.setText(snet);
                    sd1.setValue((int) sd2.getValue());
                    buttonBack.setDisable(true);
                    buttonFwd.setDisable(true);
                    buttonLast.setDisable(true);
                    prefixlist.getItems().clear();

                    Calculate(snet);
                    StartEnd.slash = StartEnd.subnetslash = (int) sd1.getValue();
                }
            }
        });
        // --- Menu Database
        Menu menuDatabase = new Menu("Database");
        MenuItem dbcon = new MenuItem("Connect...");
        MenuItem dbclose = new MenuItem("Close");
        MenuItem dbstatus = new MenuItem("Status");
        MenuItem openDBform = new MenuItem("Open DB Form...");
        menuDatabase.getItems().addAll(new SeparatorMenuItem(), dbcon, dbclose, dbstatus,
                new SeparatorMenuItem(), openDBform, sendPrefixtoDB, subPrefixes, menuItemstatsUsage);
        menuDatabase.setOnShowing(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                if (MySQLconnection == null) {
                    openDBform.setDisable(true);
                    sendPrefixtoDB.setDisable(true);
                    subPrefixes.setDisable(true);
                    menuItemstatsUsage.setDisable(true);
                } else {
                    openDBform.setDisable(false);
                    sendPrefixtoDB.setDisable(false);
                    subPrefixes.setDisable(false);
                    menuItemstatsUsage.setDisable(false);
                }
            }
        });
        dbcon.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (MySQLconnection == null) {
                    DBConnectInfo dbconnectInfo = new DBConnectInfo();
                    if (dbserverInfo.ServerIP != null) {
                        ConnectToDBServer();
                    }
                }
            }
        });
        dbclose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (MySQLconnection != null) {
                    Optional<ButtonType> bt = MsgBox.Show(Alert.AlertType.CONFIRMATION, "Confirm to close DB connection?");
                    if (bt.get() == ButtonType.OK) {
                        try {
                            if (resultSet != null) {
                                resultSet.close();
                            }
                            if (statement != null) {
                                statement.close();
                            }
                            if (MySQLconnection != null) {
                                MySQLconnection.close();
                                MySQLconnection = null;
                            }
                            UpdateDbStatus();
                        } catch (Exception ex) {
                            Logger.getLogger(IPv6SubnettingTool.class.getName()).log(Level.SEVERE, null, ex);
                            MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                            UpdateDbStatus();
                        }
                    }
                } else if (MySQLconnection == null) {
                    MsgBox.Show(Alert.AlertType.INFORMATION, "There is no opened DB connection!");
                }
            }
        });
        dbstatus.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                UpdateDbStatus();
                if (MySQLconnection != null) {
                    try {
                        DatabaseMetaData dbs = MySQLconnection.getMetaData();
                        String sc = "";
                        if (!dbs.getConnection().isClosed()) {
                            sc = "Open";
                        } else {
                            sc = "Closed";
                        }
                        MsgBox.Show(Alert.AlertType.INFORMATION,
                                "Connection Status: " + sc + "\r\n"
                                + "Database Server Prod.Vers.: " + dbs.getDatabaseProductVersion() + "\r\n"
                                + "Connected Username: " + dbs.getUserName() + "\r\n"
                                + "ConnectionString: " + dbserverInfo.ConnectionString + "\r\n"
                                + "Driver Name: " + dbs.getDriverName() + "\r\n"
                                + "Driver Version: " + dbs.getDriverVersion() + "\r\n"
                        );
                    } catch (SQLException ex) {
                        UpdateDbStatus();
                        Logger.getLogger(IPv6SubnettingTool.class.getName()).log(Level.SEVERE, null, ex);
                        MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                    }
                }
            }
        });
        openDBform.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                UpdateDbStatus();
                if (MySQLconnection != null) {
                    sendPrefixtoDB.fire();
                }
            }
        });
        sendPrefixtoDB.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                UpdateDbStatus();
                if (MySQLconnection != null) {
                    String snet = "";
                    short plen = 0;
                    short parentpflen = (short) sd1.getValue();

                    if (prefixlist.getSelectionModel().getSelectedItem() != null) {
                        String selected = prefixlist.getSelectionModel().getSelectedItem().split(" ")[1].trim();
                        snet = selected.split("/")[0].trim();
                        plen = Short.parseShort(selected.split("/")[1].trim());
                    }
                    if (dbUI == null) {
                        dbUI = new DatabaseUI(snet, plen, parentpflen, MySQLconnection, dbserverInfo);
                        stageList.add(new StageList(dbUI.toString(), dbUI.stage, dbUI.stage.hashCode()));
                        AddStageItem(dbUI.toString(), dbUI.stage.hashCode());
                    } else {
                        if (!dbUI.stage.isShowing()) {
                            stageList.add(new StageList(dbUI.toString(),
                                    dbUI.stage, dbUI.stage.hashCode()));
                            AddStageItem(dbUI.toString(), dbUI.stage.hashCode());
                        }
                        dbUI.buttonClear.fire();
                        dbUI.SetNewValues(snet, plen, parentpflen);
                        dbUI.StageShow();
                    }
                }
            }
        });
        subPrefixes.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                UpdateDbStatus();
                if (MySQLconnection != null) {
                    if (prefixlist.getSelectionModel().getSelectedItem() != null) {
                        String selected = prefixlist.getSelectionModel().getSelectedItem().split(" ")[1];
                        String snet = selected.split("/")[0];
                        short plen = Short.parseShort(selected.split("/")[1]);
                        short ppflen = QuerySelectedPrefix(snet, plen);
                        if (ppflen == 0) {
                            MsgBox.Show(Alert.AlertType.INFORMATION, "Prefix not found in the database!");
                            return;
                        } else if (ppflen < 0) {
                            MsgBox.Show(Alert.AlertType.INFORMATION, "Database connection error!");
                            return;
                        }
                        String parentprefix = v6ST.FindParentNet(snet, ppflen, ck128.isSelected());
                        if (prefixSublevels == null) {
                            prefixSublevels = new PrefixSubLevels(snet, plen, parentprefix, ck128.isSelected(),
                                    (int) sd1.getValue(), (int) sd2.getValue(),
                                    MySQLconnection, dbserverInfo);
                            stageList.add(new StageList(prefixSublevels.toString(),
                                    prefixSublevels.stage, prefixSublevels.stage.hashCode()));
                            AddStageItem(prefixSublevels.toString(), prefixSublevels.stage.hashCode());
                        } else {
                            if (!prefixSublevels.stage.isShowing()) {
                                stageList.add(new StageList(prefixSublevels.toString(),
                                        prefixSublevels.stage, prefixSublevels.stage.hashCode()));
                                AddStageItem(prefixSublevels.toString(), prefixSublevels.stage.hashCode());
                            }
                            prefixSublevels.SetNewValues(snet, plen, parentprefix,
                                    (int) sd1.getValue(), (int) sd2.getValue(), ck128.isSelected());
                            prefixSublevels.StageShow();
                        }
                    }
                }
            }
        });
        menuItemstatsUsage.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                UpdateDbStatus();
                if (MySQLconnection != null) {
                    if (!tfstartAddr.getText().equals("") && !tfendAddr.getText().equals("")) {
                        if (statUsage == null) {
                            statUsage = new StatsUsage(tfstartAddr.getText(),
                                    tfendAddr.getText(), (short) sd1.getValue(), (short) sd2.getValue(),
                                    ck128.isSelected(), MySQLconnection, dbserverInfo);
                            stageList.add(new StageList(statUsage.toString(),
                                    statUsage.stage, statUsage.stage.hashCode()));
                            AddStageItem(statUsage.toString(), statUsage.stage.hashCode());
                        } else {
                            if (!statUsage.stage.isShowing()) {
                                stageList.add(new StageList(statUsage.toString(),
                                        statUsage.stage, statUsage.stage.hashCode()));
                                AddStageItem(statUsage.toString(), statUsage.stage.hashCode());
                            }                            
                            statUsage.StageShow();
                            statUsage.buttonRefresh.fire();
                        }
                    }
                }
            }
        });
        // --- Menu Help
        Menu menuHelp = new Menu("Help");
        MenuItem about = new MenuItem("About...");
        menuHelp.getItems().addAll(new SeparatorMenuItem(), about);
        about.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                About.Show();
            }
        });
        // --- addAll to menuBar
        menuBar.getMenus().addAll(menuFile, menuTools, menuGoto, menuDatabase,
                menuStages, menuHelp);
        menuBar.setUseSystemMenuBar(true);
        // --- End of Menu

        saveAs.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (saveTxt == null) {
                    saveTxt = new SaveAsTxt(StartEnd, ck128.isSelected());
                    stageList.add(new StageList(saveTxt.toString(), saveTxt.stage, saveTxt.stage.hashCode()));
                    AddStageItem(saveTxt.toString(), saveTxt.stage.hashCode());
                } else {
                    if (!saveTxt.stage.isShowing()) {
                        stageList.add(new StageList(saveTxt.toString(), saveTxt.stage, saveTxt.stage.hashCode()));
                        AddStageItem(saveTxt.toString(), saveTxt.stage.hashCode());
                    }
                    saveTxt.SetNewValues(StartEnd, ck128.isSelected());
                    saveTxt.StageShow();
                }
                
            }
        });
        //
        return menuBar;
    }

    public void ConnectToDBServer() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            try {
                Class.forName("com.mysql.jdbc.Driver");                
                MySQLconnection = DriverManager.getConnection("jdbc:mysql://"
                        + dbserverInfo.ServerIP.getHostAddress() + ":"
                        + String.valueOf(dbserverInfo.PortNum) + "/",
                        dbserverInfo.Username, dbserverInfo.Password.getText());

                UpdateDbStatus();
                MsgBox.Show(Alert.AlertType.INFORMATION, "Database Connected!");

            } catch (SQLException ex) {
                Logger.getLogger(IPv6SubnettingTool.class.getName()).log(Level.SEVERE, null, ex);
                MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
                UpdateDbStatus();
                return;
            }
            dbserverInfo.ConnectionString = "jdbc:mysql://"
                    + dbserverInfo.ServerIP.getHostAddress() + ":"
                    + String.valueOf(dbserverInfo.PortNum) + "/" + dbserverInfo.DBname;
            //
            // Database exist?
            this.statement = MySQLconnection.createStatement();
            this.resultSet = this.statement.executeQuery("SELECT SCHEMA_NAME FROM "
                    + "INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='"
                    + dbserverInfo.DBname + "';");

            // Create database if not exists:
            if (!this.resultSet.next()) {
                this.statement.executeUpdate("CREATE DATABASE IF NOT EXISTS "
                        + "`" + dbserverInfo.DBname + "`"
                        + " DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;");
                // Select/use database:
                this.resultSet = this.statement.executeQuery("USE "
                        + "`" + dbserverInfo.DBname + "`;");
                // Create table if not exists:
                this.statement.executeUpdate("CREATE TABLE IF NOT EXISTS "
                        + "`" + dbserverInfo.DBname + "`."
                        + "`" + dbserverInfo.Tablename + "`"
                        + " ( "
                        + "prefix VARBINARY(16), "
                        + "pflen TINYINT UNSIGNED, "
                        + "parentpflen TINYINT UNSIGNED, "
                        + "netname VARCHAR(40), "
                        + "person  VARCHAR(40), "
                        + "organization VARCHAR(60), "
                        + "`as-num` INT UNSIGNED, "
                        + "phone VARCHAR(40), "
                        + "email VARCHAR(40), "
                        + "status VARCHAR(40), "
                        + "`created` TIMESTAMP NOT NULL default '0000-00-00 00:00:00', "
                        + "`last-updated` TIMESTAMP NOT NULL default NOW() ON UPDATE NOW(), "
                        + "PRIMARY KEY(prefix, pflen) "
                        + "); ");
                // Triggers for timestamps: Triggers are assoc.with DBs.
                this.resultSet = this.statement.executeQuery("SELECT "
                        + "TRIGGER_NAME FROM information_schema.triggers "
                        + "where TRIGGER_NAME='trig_insert' AND TRIGGER_SCHEMA='"
                        + dbserverInfo.DBname + "';");
                if (!this.resultSet.next()) {
                    this.statement.executeUpdate(
                            "CREATE TRIGGER trig_insert BEFORE INSERT ON "
                            + "`" + dbserverInfo.DBname + "`."
                            + "`" + dbserverInfo.Tablename + "`"
                            + " FOR EACH ROW BEGIN SET NEW.`created`=IF(ISNULL(NEW.`created`) OR "
                            + "NEW.`created`='0000-00-00 00:00:00', CURRENT_TIMESTAMP, "
                            + "IF(NEW.`created` < CURRENT_TIMESTAMP, NEW.`created`, "
                            + "CURRENT_TIMESTAMP));SET NEW.`last-updated`=NEW.`created`; END;"
                    );
                }
                this.resultSet = this.statement.executeQuery("SELECT "
                        + "TRIGGER_NAME FROM information_schema.triggers "
                        + "where TRIGGER_NAME='trig_update' AND TRIGGER_SCHEMA='"
                        + dbserverInfo.DBname + "';");
                if (!this.resultSet.next()) {
                    this.statement.executeUpdate(
                            "CREATE trigger trig_update BEFORE UPDATE ON "
                            + "`" + dbserverInfo.DBname + "`."
                            + "`" + dbserverInfo.Tablename + "`"
                            + " FOR EACH ROW "
                            + "SET NEW.`last-updated` = IF(NEW.`last-updated` < OLD.`last-updated`, "
                            + "OLD.`last-updated`, CURRENT_TIMESTAMP);"
                    );
                }
                // Create index:                    
                this.resultSet = this.statement.executeQuery(
                        "SHOW INDEX from " + dbserverInfo.DBname
                        + " WHERE Key_name = 'idx_index';");
                if (!this.resultSet.next()) {
                    this.statement.executeUpdate(
                            " CREATE INDEX idx_index ON "
                            + "`" + dbserverInfo.DBname + "`."
                            + "`" + dbserverInfo.Tablename + "`"
                            + " (prefix, pflen) USING BTREE;"
                    );
                }
                //
            } else { // DB exists:
                this.resultSet = this.statement.executeQuery(
                        "USE " + "`" + dbserverInfo.DBname + "`;");
                // create table if not exists:
                this.statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS "
                        + "`" + dbserverInfo.DBname + "`."
                        + "`" + dbserverInfo.Tablename + "`"
                        + " ( "
                        + "prefix VARBINARY(16), "
                        + "pflen TINYINT UNSIGNED, "
                        + "parentpflen TINYINT UNSIGNED, "
                        + "netname VARCHAR(40), "
                        + "person  VARCHAR(40), "
                        + "organization VARCHAR(60), "
                        + "`as-num` INT UNSIGNED, "
                        + "phone VARCHAR(40), "
                        + "email VARCHAR(40), "
                        + "status VARCHAR(40), "
                        + "`created` TIMESTAMP NOT NULL default '0000-00-00 00:00:00', "
                        + "`last-updated` TIMESTAMP NOT NULL default NOW() ON UPDATE NOW(), "
                        + "PRIMARY KEY(prefix, pflen) "
                        + "); "
                );
                // Triggers for timestamps:
                this.resultSet = this.statement.executeQuery("SELECT "
                        + "TRIGGER_NAME FROM information_schema.triggers "
                        + "where TRIGGER_NAME='trig_insert' AND TRIGGER_SCHEMA='"
                        + dbserverInfo.DBname + "';");

                if (!this.resultSet.next()) {
                    this.statement.executeUpdate(
                            "CREATE TRIGGER trig_insert BEFORE INSERT ON "
                            + "`" + dbserverInfo.DBname + "`."
                            + "`" + dbserverInfo.Tablename + "`"
                            + " FOR EACH ROW BEGIN SET NEW.`created`=IF(ISNULL(NEW.`created`) OR "
                            + "NEW.`created`='0000-00-00 00:00:00', CURRENT_TIMESTAMP, "
                            + "IF(NEW.`created` < CURRENT_TIMESTAMP, NEW.`created`, "
                            + "CURRENT_TIMESTAMP));SET NEW.`last-updated`=NEW.`created`; END;"
                    );
                }
                this.resultSet = this.statement.executeQuery("SELECT "
                        + "TRIGGER_NAME FROM information_schema.triggers "
                        + "where TRIGGER_NAME='trig_update' AND TRIGGER_SCHEMA='"
                        + dbserverInfo.DBname + "';");
                if (!this.resultSet.next()) {
                    this.statement.executeUpdate(
                            "CREATE trigger trig_update BEFORE UPDATE ON "
                            + "`" + dbserverInfo.DBname + "`."
                            + "`" + dbserverInfo.Tablename + "`"
                            + " FOR EACH ROW "
                            + "SET NEW.`last-updated` = IF(NEW.`last-updated` < OLD.`last-updated`, "
                            + "OLD.`last-updated`, CURRENT_TIMESTAMP);"
                    );
                }
                // and index it if not indexed:
                this.resultSet = this.statement.executeQuery(
                        "SHOW INDEX from " 
                        + "`" + dbserverInfo.DBname + "`."
                        + "`" + dbserverInfo.Tablename + "`"
                        + " WHERE Key_name = 'idx_index';");
                if (!this.resultSet.next()) {
                    this.statement.executeUpdate(
                            " CREATE INDEX idx_index ON "
                            + "`" + dbserverInfo.DBname + "`."
                            + "`" + dbserverInfo.Tablename + "`"
                            + " (prefix, pflen) USING BTREE;"
                    );
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(IPv6SubnettingTool.class.getName()).log(Level.SEVERE, null, ex);
            MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
            UpdateDbStatus();
        }
    }

    private short QuerySelectedPrefix(String inprefix, short pflen) {
        if (MySQLconnection == null) {
            MsgBox.Show(Alert.AlertType.WARNING, "There is no opened DB connection!");
            return -1;
        }
        String MySQLcmd = "SELECT inet6_ntoa(prefix), pflen, parentpflen "
                + " from " 
                + "`" + dbserverInfo.DBname + "`."
                + "`" + dbserverInfo.Tablename + "`"
                + " WHERE ( prefix=inet6_aton('" + inprefix + "') "
                + " AND pflen=" + pflen + " );";
        try {
            statement = MySQLconnection.createStatement();
            resultSet = statement.executeQuery(MySQLcmd);
            short parentpflen = 0;
            while (resultSet.next()) {
                parentpflen = resultSet.getByte(3);
            }
            return parentpflen;
        } catch (Exception ex) {
            MsgBox.Show(Alert.AlertType.ERROR, ex.toString());
            return -1;
        }
    }

    public GridPane addGrid() {
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setAlignment(Pos.BASELINE_CENTER);
        grid.setPadding(new Insets(0, 5, 0, 5));

        grid.add(status, 1, 0);
        //
        HBox hb0 = new HBox();
        hb0.getChildren().add(addr);
        hb0.alignmentProperty().set(Pos.CENTER_RIGHT);
        grid.add(hb0, 0, 1);
        //
        HBox hb1 = new HBox(5);
        hb1.getChildren().addAll(input, buttonFind);
        grid.add(hb1, 1, 1);
        //
        HBox hb02 = new HBox();
        hb02.alignmentProperty().set(Pos.CENTER_RIGHT);
        hb02.getChildren().add(labelipv6);
        hb02.alignmentProperty().set(Pos.CENTER_RIGHT);
        grid.add(hb02, 0, 2);
        //
        HBox hb2 = new HBox(5);
        hb2.getChildren().addAll(ipv6addr, buttonReset);
        grid.add(hb2, 1, 2);
        //
        HBox hb03 = new HBox();
        hb03.setAlignment(Pos.CENTER_RIGHT);
        hb03.getChildren().add(lbstart);
        grid.add(hb03, 0, 3);
        //
        HBox aspn = new HBox();
        aspn.setAlignment(Pos.BOTTOM_CENTER);
        aspn.getChildren().add(lbaddrsp);
        HBox hb3 = new HBox(5);
        hb3.getChildren().addAll(tfstartAddr, buttonPrevSpace, buttonNextSpace, aspn);
        grid.add(hb3, 1, 3);
        //
        HBox hb04 = new HBox();
        hb04.setAlignment(Pos.CENTER_RIGHT);
        hb04.getChildren().add(lbend);
        grid.add(hb04, 0, 4);
        //
        HBox asn = new HBox();
        asn.setAlignment(Pos.CENTER_RIGHT);
        asn.getChildren().add(lbaddrspno);
        lbaddrspno.setStyle("-fx-border-color: #d3d3d3;");
        lbaddrspno.setPrefWidth(130);
        grid.add(asn, 1, 4);
        HBox hb4 = new HBox(5);
        hb4.getChildren().addAll(tfendAddr);
        grid.add(hb4, 1, 4);
        //
        HBox hblb128 = new HBox();
        hblb128.getChildren().add(lb128);
        hblb128.setAlignment(Pos.CENTER_RIGHT);
        grid.add(hblb128, 0, 5);
        //
        HBox hbtf = new HBox();
        hbtf.getChildren().add(tf);
        hbtf.setAlignment(Pos.BOTTOM_LEFT);
        grid.add(hbtf, 1, 5);
        //
        HBox hblbprefix = new HBox();
        hblbprefix.getChildren().add(lbprefix);
        hblbprefix.setAlignment(Pos.CENTER_RIGHT);
        grid.add(hblbprefix, 0, 6);
        //
        HBox hbsldr1 = new HBox(3);
        hbsldr1.getChildren().addAll(sd1, c1);
        hbsldr1.setAlignment(Pos.CENTER_LEFT);
        grid.add(hbsldr1, 1, 6);
        //
        HBox hblbsub = new HBox();
        hblbsub.getChildren().add(lbsub);
        hblbsub.setAlignment(Pos.CENTER_RIGHT);
        grid.add(hblbsub, 0, 7);
        //
        HBox sldr2 = new HBox(3);
        sldr2.getChildren().addAll(sd2, c2);
        grid.add(sldr2, 1, 7);
        //
        HBox hblbendaddr = new HBox();
        hblbendaddr.getChildren().add(lbckendaddr);
        hblbendaddr.setAlignment(Pos.BOTTOM_RIGHT); //.CENTER_RIGHT);
        grid.add(hblbendaddr, 0, 8);
        //
        hblistcount.getChildren().add(listcount);
        hblistcount.setAlignment(Pos.BOTTOM_RIGHT);
        //
        HBox btns = new HBox(3);
        btns.getChildren().addAll(buttonFirstPage, buttonBack, buttonFwd,
                buttonLast, canvas, hblistcount);
        grid.add(btns, 1, 8);
        //
        grid.add(prefixlist, 1, 9);
        //
        HBox hbyg = new HBox();
        hbyg.getChildren().add(lbyg);
        hbyg.setAlignment(Pos.BOTTOM_RIGHT);
        grid.add(hbyg, 1, 10);
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
        hbstatusstr.setPrefWidth(410);
        statusString.setStyle("-fx-text-fill: royalblue;");
        hbstatusstr.getChildren().add(statusString);

        HBox hbdbstatus = new HBox();
        lbdbstatus.setStyle("-fx-text-fill: royalblue;");
        hbdbstatus.setAlignment(Pos.CENTER_RIGHT);
        lbdbstatus.setPrefWidth(80);
        hbdbstatus.getChildren().add(lbdbstatus);

        hbstatus.getChildren().addAll(hbstatusstr, hbdbstatus);
        return hbstatus;
    }

    @Override
    public void start(Stage primaryStage) {

        this.StartEnd.ID = ID;
        //
        this.primaryStage = primaryStage;
        root.setTop(this.addMenuBar());
        root.setCenter(this.addAnchorPane(addGrid()));
        this.SettingsAndEvents();
        root.setBottom(addStatusBar());

        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.setTitle("IPv6 Subnet Calculator / Tool");
        primaryStage.setScene(scene);
        primaryStage.show();
        //
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if (key.getCode() == KeyCode.ESCAPE) {
                    DBClose();
                    Platform.exit();
                }
            }
        });
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                DBClose();
                Platform.exit();
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(IPv6SubnettingTool.class, args);
    }
}
