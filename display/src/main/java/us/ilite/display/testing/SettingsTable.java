package us.ilite.display.testing;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Pair;
import us.ilite.common.io.Network;

import static us.ilite.display.testing.TestingDisplay.getEntryList;

public class SettingsTable implements IDisplayComponent {

    private final ILog mLog = Logger.createLog(SettingsTable.class);
    private final NetworkTable mSettingsNetworkTable;

    private TableView<NetworkTableEntry> mSettingsTableView = new TableView();
    private TableColumn<NetworkTableEntry, String> mEntryNameColumn = new TableColumn<>("Name");
    private TableColumn<NetworkTableEntry, String> mEntryValueColumn = new TableColumn<>("Value");

    public SettingsTable(NetworkTable pNetworkTable) {
        mSettingsNetworkTable = pNetworkTable;
    }

    @Override
    public Node getRootNode() {

        mSettingsNetworkTable.addEntryListener((table, key, entry, value, flags) -> {
            mLog.debug("Change");
            refresh();
        }, EntryListenerFlags.kNew | EntryListenerFlags.kUpdate | EntryListenerFlags.kLocal);

        NetworkTableInstance.getDefault().addConnectionListener((pConnectionNotification) -> {
            refresh();
            mLog.error("Reconnected to NT");
        }, true);

        mEntryNameColumn.setCellValueFactory(cellDataFeatures -> new SimpleStringProperty(NetworkTable.basenameKey(cellDataFeatures.getValue().getName())));
//        mEntryValueColumn.setCellValueFactory(cellDataFeatures -> new ObservableEntryStringValue(cellDataFeatures.getValue()).getValue());
        mEntryValueColumn.setCellValueFactory(cellDataFeatures -> new SimpleStringProperty(cellDataFeatures.getValue().getString("Default Value")));

        mEntryValueColumn.setEditable(true);
        mEntryValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        mEntryValueColumn.setOnEditCommit(networkTableEntryStringCellEditEvent -> {
            int valueRow = networkTableEntryStringCellEditEvent.getTablePosition().getRow();
            String entryKey = mEntryNameColumn.getCellData(valueRow);
            String newValue = networkTableEntryStringCellEditEvent.getNewValue();
            mLog.warn("Edit commit for entry: ", entryKey, " with value: ", newValue);

            set(entryKey, newValue);
        });

        mSettingsTableView.sort();
        mSettingsTableView.setEditable(true);
        mSettingsTableView.getSelectionModel().cellSelectionEnabledProperty().set(true);
        mSettingsTableView.getSelectionModel().selectionModeProperty().setValue(SelectionMode.MULTIPLE);

        mSettingsTableView.setItems(FXCollections.observableArrayList(getEntryList(mSettingsNetworkTable)));
        mSettingsTableView.getColumns().addAll(mEntryNameColumn, mEntryValueColumn);

        return mSettingsTableView;
    }

    private void set(Pair<String, String> pEntry) {
        set(pEntry.getKey(), pEntry.getValue());
    }

    private void set(String pEntryKey, String pValue) {
        mSettingsNetworkTable.getEntry(pEntryKey).setString(pValue);
        mSettingsTableView.refresh();
    }



    public ObservableList<NetworkTableEntry> moveToTop(ObservableList<NetworkTableEntry> pItemsToMove) {
        pItemsToMove.forEach(itemToMove -> {
            mSettingsTableView.getItems().remove(itemToMove);
            mSettingsTableView.getItems().add(0, itemToMove);
        });

        return pItemsToMove;
    }

    public ObservableList<NetworkTableEntry> selectAll(ObservableList<NetworkTableEntry> pItemsToSelect) {
        pItemsToSelect.forEach(itemToSelect -> mSettingsTableView.getSelectionModel().select(itemToSelect));

        return pItemsToSelect;
    }

    public ObservableList<NetworkTableEntry> searchFor(String pSearchPattern) {
        ObservableList<NetworkTableEntry> searchResults = FXCollections.observableArrayList();

        if(!pSearchPattern.isEmpty()) {
            for(NetworkTableEntry entry : mSettingsTableView.getItems()) {
                String entryName = mEntryNameColumn.getCellObservableValue(entry).getValue().toLowerCase();
                if(entryName.contains(pSearchPattern)) {
                    searchResults.add(entry);
                }
            }
        }

        return searchResults;
    }

    public void scrollToTop() {
        mSettingsTableView.scrollTo(0);
    }

    public void clearSelection() {
        mSettingsTableView.getSelectionModel().clearSelection();
    }

    public void refresh() {
        mSettingsTableView.refresh();
    }

}
