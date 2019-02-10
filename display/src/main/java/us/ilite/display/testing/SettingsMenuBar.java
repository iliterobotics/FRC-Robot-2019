package us.ilite.display.testing;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.PersistentException;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class SettingsMenuBar implements IDisplayComponent {

    private final NetworkTable mSettingsNetworkTable;
    private final Stage mPrimaryStage;

    private MenuBar mMenuBar = new MenuBar();
    private Menu mFileDropdown = new Menu("File");
    private MenuItem mSaveToFileItem = new MenuItem("Save Configuration");
    private MenuItem mLoadFromFileItem = new MenuItem("Load Configuration");

    public SettingsMenuBar(Stage pPrimaryStage, NetworkTable pSettingsNetworkTable) {
        this.mPrimaryStage = pPrimaryStage;
        this.mSettingsNetworkTable = pSettingsNetworkTable;
    }

    @Override
    public Node getRootNode() {
        mSaveToFileItem.setOnAction(actionEvent -> {
            File selectedFile = openFileSaveChooser("Save Configuration", mPrimaryStage);
            try {
                if(selectedFile != null) mSettingsNetworkTable.saveEntries(selectedFile.getAbsolutePath());
            } catch (PersistentException e) {
                e.printStackTrace();
            }
        });

        mLoadFromFileItem.setOnAction(actionEvent -> {
            File selectedFile = openFileLoadChooser("Load Configuration", mPrimaryStage);
            try {
                if(selectedFile != null) mSettingsNetworkTable.loadEntries(selectedFile.getAbsolutePath());
            } catch (PersistentException e) {
                e.printStackTrace();
            }
        });

        mMenuBar.getMenus().addAll(mFileDropdown);
        mFileDropdown.getItems().addAll(mSaveToFileItem, mLoadFromFileItem);

        return mMenuBar;
    }

    private File openFileSaveChooser(String pTitle, Stage pStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(pTitle);
        return fileChooser.showSaveDialog(pStage);
    }

    private File openFileLoadChooser(String pTitle, Stage pStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(pTitle);
        return fileChooser.showOpenDialog(pStage);
    }

}
