package us.ilite.display.testing;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import edu.wpi.first.networktables.NetworkTableEntry;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import javax.swing.*;

public class ActionBar implements IDisplayComponent {

    private final ILog mLog = Logger.createLog(ActionBar.class);

    private SettingsTable mSettingsTable;

    private HBox mActionBar = new HBox();
    private TextField mSearchField = new TextField();
    private Button mSearchFieldClearButton = new Button("Clear");
    private Button mRefreshButton = new Button("Refresh");
    private Button mHistoryButton = new Button("Show History");

    public ActionBar(SettingsTable pSettingsTable) {
        this.mSettingsTable = pSettingsTable;
    }

    @Override
    public Node getRootNode() {

        mSearchField.textProperty().addListener((observableValue, s, t1) -> {
            mLog.debug("Searching with term: ", observableValue.getValue());

            String searchPattern = observableValue.getValue().toLowerCase();
            ObservableList<NetworkTableEntry> searchResults = mSettingsTable.searchFor(searchPattern);

            mSettingsTable.clearSelection();
            mSettingsTable.moveToTop(searchResults);
            mSettingsTable.selectAll(searchResults);
            mSettingsTable.scrollToTop();
        });

        mSearchFieldClearButton.setOnAction(actionEvent -> mSearchField.clear());
        mRefreshButton.setOnAction(actionEvent -> mSettingsTable.refresh());

        mActionBar.getChildren().addAll(mSearchField, mSearchFieldClearButton, mRefreshButton);

        return mActionBar;
    }
}
