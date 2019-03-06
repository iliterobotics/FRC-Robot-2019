package us.ilite.display.auton;

import com.flybotix.hfr.util.lang.EnumUtils;
import com.google.gson.Gson;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Callback;
import us.ilite.common.AutonSelectionData;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.auton.EStartingPosition;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

// **2018 imports**
// import us.ilite.frc.common.config.SystemSettings;
// import us.ilite.frc.common.input.EDriverControlMode;
// import us.ilite.frc.common.types.ECross;
// import us.ilite.frc.common.types.ECubeAction;
// import us.ilite.frc.common.types.EStartingPosition;
// import us.ilite.frc.common.util.CSVLogger;


public class AutonConfigDisplay extends Application {

    // private CSVLogger logger = new CSVLogger();

    // private Integer[] preferredCubeActions = new Integer[]{-1, -1, -1, -1};
    private double mDelay = 0.0;
    private static Integer mStartingPosition;
    private static Integer mCargoAction;
    private static Integer mHatchAction;
    // private static Integer mAutonPath = EDriverControlMode.values()[0].ordinal();

    // private String awesomeCss = AutonConfigDisplay.class.getResource("AwesomeStyle.css").toExternalForm();
    // private String iliteCss = this.getClass().getResource("ILITEStyle".css").toExternalForm();

    private Gson mGson = new Gson();


    public static void main(String[] pArgs) {
        launch(pArgs);
    }

    @Override
    public void start(Stage primaryStage) throws Exception { //Starts Program
        setDefaults();

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 800, 600);

        // This is dumb and changes background
        // scene.getStylesheets().add(iliteCss);
        // scene.setOnMouseClicked(e -> {
        // if(scene.getStylesheets().contains(awesomeCss)) {
        // playSound("./airhorn.mp3");
        // }
        // });

        Button send = new Button("Send"); //Send Button
        send.setOnAction(e -> {
            sendData();
        });

        Button mode = new Button("Enhanced Mode");
        mode.setOnAction(e -> {
            // if(scene.getStylesheets().contains(awesomeCss)) {
            // mode.setText("Enhanced Mode");
            // scene.getStylesheets().clear();
            // scene.getStylesheets().add(iliteCss);
            // } else {
            // mode.setText("Judge's Mode");
            // scene.getStylesheets().clear();
            // scene.getStylesheets().add(awesomeCss);
            //     setFieldImage("./field.png");
            // }

        });

        TextField delayText = new TextField();
        delayText.setOnAction(e -> {
            mDelay = Double.parseDouble(delayText.getText());
        });
        Label delayLabel = new Label("Delay");

        HBox selectionBoxes = new HBox(
                //This is the dropdown for selecting autonomous type
                labeledDropdown(EStartingPosition.class),
                labeledDropdown(EHatchAction.class),
                labeledDropdown(ECargoAction.class),
                delayLabel,
                delayText);

        HBox modeOptions = new HBox(mode, send);
        modeOptions.setMargin(send, new Insets(0, 40, 0, 20));

        Thread dataSender = new Thread(() -> {
            while (!Thread.interrupted()) {
                sendData();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e1) {
                    System.err.println("Thread sleep interrupted");
                }
            }
        });
        dataSender.start();

        // logger.start();

        selectionBoxes.setSpacing(10d);
        root.setCenter(selectionBoxes);
        root.setBottom(modeOptions);
        BorderPane.setAlignment(selectionBoxes, Pos.CENTER);
        BorderPane.setAlignment(modeOptions, Pos.BOTTOM_LEFT);

        primaryStage.setTitle("ILITE Autonomous Configuration");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();

    }

    private static <E extends Enum<E>> VBox labeledDropdown(Class<E> pEnumeration) {
        List<E> enums = EnumUtils.getEnums(pEnumeration, true);
        Label label = new Label(toPrettyCase(pEnumeration.getSimpleName().substring(1)));
        label.setTextAlignment(TextAlignment.CENTER);
        ComboBox<E> combo = new ComboBox<>(FXCollections.observableArrayList(enums));
        combo.setOnAction(
                event -> {
                    System.out.println("Action triggered!");
                    String enumName = pEnumeration.getSimpleName();
                    if (enumName.equals(EStartingPosition.class.getSimpleName())) {
                        mStartingPosition = combo.getSelectionModel().getSelectedItem().ordinal();
                        System.out.println("Updating position: " + mStartingPosition);
                    }
                    if (enumName.equals(ECargoAction.class.getSimpleName())) {
                        mCargoAction = combo.getSelectionModel().getSelectedItem().ordinal();
                        System.out.println("Updating cargo action: " + mCargoAction);
                    }
                    if (enumName.equals(EHatchAction.class.getSimpleName())) {
                        mHatchAction = combo.getSelectionModel().getSelectedItem().ordinal();
                        System.out.println("Updating hatch action: " + mHatchAction);
                    }
                }
        );
        if (enums.size() > 0) combo.setValue(enums.get(0));
        VBox result = new VBox(label, combo);
        return result;
    }

    private <E extends Enum<E>> VBox labeledCheckboxDropdown(Class<E> pEnumeration, Object[] preferenceArray) {
        List<E> enums = EnumUtils.getEnums(pEnumeration, true);
        Label label = new Label(toPrettyCase(pEnumeration.getSimpleName().substring(1)));
        label.setTextAlignment(TextAlignment.CENTER);
        ListView<String> listView = new ListView<>();
        for (E e : enums) {
            listView.getItems().add(e.name());
        }
        listView.setCellFactory(CheckBoxListCell.forListView(new Callback<String, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(String item) {
                BooleanProperty observable = new SimpleBooleanProperty();
                observable.addListener(e -> {
                    // if(observable.get()) {
                    //   preferenceArray[listView.getItems().indexOf(item)] = ECubeAction.valueOf(item).ordinal();
                    // } else {
                    // 	preferenceArray[listView.getItems().indexOf(item)] = -1;
                    // }
                    System.out.println(Arrays.toString(preferenceArray));
                });
                return observable;
            }
        }));

        Button up = new Button("Up");
        Button down = new Button("Down");
        up.setOnAction(e -> swapEntriesUp(listView, preferenceArray));
        down.setOnAction(e -> swapEntriesDown(listView, preferenceArray));
        up.setMinWidth(60);
        down.setMinWidth(60);
        HBox buttons = new HBox(up, down);
        buttons.setMargin(up, new Insets(10, 40, 10, 40));
        buttons.setMargin(down, new Insets(10, 40, 10, 40));
        VBox result = new VBox(label, listView, buttons);
        return result;
    }

    private static String toPrettyCase(String pInput) {
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(pInput.charAt(0)));
        for (int i = 1; i < pInput.length(); i++) {
            if (Character.isUpperCase(pInput.charAt(i))) {
                sb.append(' ');
            }
            sb.append(pInput.charAt(i));
        }
        return sb.toString();
    }

    private void sendData() {
        SystemSettings.AUTON_TABLE.putNumber(ECargoAction.class.getSimpleName(), mCargoAction);
        SystemSettings.AUTON_TABLE.putNumber(EHatchAction.class.getSimpleName(), mHatchAction);
        SystemSettings.AUTON_TABLE.putNumber(EStartingPosition.class.getSimpleName(), mStartingPosition);

        AutonSelectionData test = null;
        String jsonData = mGson.toJson(test);

        //   SystemSettings.AUTON_TABLE.putDouble(SystemSettings.AUTO_DELAY_KEY, mDelay);
        //   SystemSettings.AUTON_TABLE.putNumber(ECross.class.getSimpleName(), mCross);
        //   SystemSettings.AUTON_TABLE.putNumber(EStartingPosition.class.getSimpleName(), mStartingPosition);
        //   SystemSettings.DRIVER_CONTROL_TABLE.putNumber(EDriverControlMode.class.getSimpleName(), mDriverControlMode);
    }

    private static void swapEntriesUp(ListView listView, Object[] outputArray) {
        ObservableList list = listView.getItems();
        Object selectedItem = listView.getSelectionModel().getSelectedItem();
        int selectedIndex = list.indexOf(selectedItem);
        Object temp = selectedItem;

        if (selectedIndex - 1 >= 0) {
            list.set(selectedIndex, list.get(selectedIndex - 1));
            list.set(selectedIndex - 1, temp);
            outputArray[selectedIndex] = -1;
            outputArray[selectedIndex - 1] = -1;
            listView.getSelectionModel().select(selectedIndex - 1);
        }
        listView.setItems(list);
    }

    private static void swapEntriesDown(ListView listView, Object[] outputArray) {
        ObservableList list = listView.getItems();
        Object selectedItem = listView.getSelectionModel().getSelectedItem();
        int selectedIndex = list.indexOf(selectedItem);
        Object temp = selectedItem;

        if (selectedIndex + 1 < list.size()) {
            list.set(selectedIndex, list.get(selectedIndex + 1));
            list.set(selectedIndex + 1, temp);
            outputArray[selectedIndex] = -1;
            outputArray[selectedIndex + 1] = -1;
            listView.getSelectionModel().select(selectedIndex + 1);
        }
        listView.setItems(list);
    }

    private static void setFieldImage(String path) {
        try {
            Image field = new Image(new File(path).toURI().toURL().toExternalForm());
            ImageView fieldView = new ImageView(field);
            fieldView.setX(400);
            fieldView.setY(200);
            fieldView.setFitHeight(400);
            fieldView.setFitWidth(600);
            fieldView.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("File not found.");
        }
    }

    private static void playSound(String sound) {
        // cl is the ClassLoader for the current class, ie. CurrentClass.class.getClassLoader();
        URL file = AutonConfigDisplay.class.getResource(sound);
        final Media media = new Media(file.toString());
        final MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }

    private void setDefaults() {
        if (EStartingPosition.values().length > 0) mStartingPosition = EStartingPosition.values()[0].ordinal();
        if (ECargoAction.values().length > 0) mCargoAction = ECargoAction.values()[0].ordinal();
        if (EHatchAction.values().length > 0) mHatchAction = EHatchAction.values()[0].ordinal();
    }

}
