package us.ilite.display.testing;

import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import edu.wpi.first.networktables.*;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import us.ilite.common.config.SystemSettings;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Allows editing SystemSettings fields posted by NetworkTables. Keeps a history of edited fields,
 * allowing configurations to be saved and loaded as well as undone and redone.
 *
 * See https://dzone.com/articles/editable-tables-in-javafx
 */
public class TestingDisplay extends Application {

    private static final ILog mLog = Logger.createLog(TestingDisplay.class);
    private static final NetworkTableInstance kNetworkTableInst = NetworkTableInstance.getDefault();

    private NetworkTable mSettingsNetworkTable = kNetworkTableInst.getTable(SystemSettings.class.getSimpleName().toUpperCase());

    // Main Settings
    private VBox mSettingsBox = new VBox();

    private SettingsMenuBar mMenuBar;
    private ActionBar mActionBar;
    private SettingsTable mSettingsTable;


    private BorderPane mSceneLayout = new BorderPane();

    public static void main(String[] args) {
        Logger.setLevel(ELevel.WARN);
        kNetworkTableInst.startClientTeam(1885);

        Thread connectionThread = new Thread(() -> {
            while(!Thread.interrupted()) {

                if(!kNetworkTableInst.isConnected()) {
                    mLog.error("Not connected. Retrying...");
                    kNetworkTableInst.startClientTeam(1885);
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException pE) {
                    pE.printStackTrace();
                }
            }
        });

        connectionThread.start();

//        kNetworkTableInst.startServer();
//        Thread debugThread = new Thread(new Runnable() {
//            SystemSettings settings = new SystemSettings();
//
//            @Override
//            public void run() {
//                settings.writeToNetworkTables();
//                while(!Thread.interrupted()) {
//                    settings.loadFromNetworkTables();
////                    mLog.debug("kDriveVelocity_kP: ", SystemSettings.kDriveVelocity_kP);
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//        });
//
//        debugThread.start();

        launch(args);
    }

    @Override
    public void start(Stage pStage) throws Exception {

        mSettingsTable = new SettingsTable(mSettingsNetworkTable);
        mMenuBar = new SettingsMenuBar(pStage, mSettingsNetworkTable);
        mActionBar = new ActionBar(mSettingsTable);


        mSettingsBox.getChildren().addAll(mActionBar.getRootNode(), mSettingsTable.getRootNode());

        mSceneLayout.setTop(mMenuBar.getRootNode());
        mSceneLayout.setCenter(mSettingsBox);

        Scene scene = new Scene(mSceneLayout, 800, 600);
        scene.getStylesheets().add("ILITEStyle.css");
        pStage.setTitle("ILITE Testing Display");
        pStage.setScene(scene);
        pStage.setOnCloseRequest(e -> System.exit(0));
        pStage.show();
    }

    /**
     *
     * @param pTable
     * @return A list of entries for a specified Network Table
     */
    public static List<NetworkTableEntry> getEntryList(NetworkTable pTable) {
        return pTable.getKeys().stream().map(key -> pTable.getEntry(key)).collect(Collectors.toList());
    }

}
