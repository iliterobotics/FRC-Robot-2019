package us.ilite.display;


import com.sun.prism.paint.Gradient;
import eu.hansolo.fx.regulators.FeedbackRegulator;
import eu.hansolo.fx.regulators.FeedbackRegulatorBuilder;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.addons.Indicator;
import eu.hansolo.tilesfx.skins.BarChartItem;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome.FontAwesome;

public class DriverStation extends Application {
    private static final double sTILE_SIZE_PX = 400;
    private final BarChartItem[] mPDPbarCharItems = new BarChartItem[16];

    @Override
    public void start(Stage stage) throws Exception {

        BorderPane root = new BorderPane();

        HBox[] hb = new HBox[8];
        for(int i = 0 ; i < hb.length; i++) {
            hb[i] = createIndicatorRow("PCM " + i, 100);
        }

        for(int i = 0; i < mPDPbarCharItems.length; i++) {
            mPDPbarCharItems[i] = new BarChartItem("PDP" + i, 0, Tile.GRAY);
        }
        Tile pdpTile = TileBuilder.create()
                .skinType(Tile.SkinType.BAR_CHART)
                .title("Power Distribution Panel")
                .barChartItems(mPDPbarCharItems)

                .decimals(0)
                .build();





        VBox right = new VBox(pdpTile);
//        right.getChildren().add(pdpTile);

        root.setLeft(createElevatorPane());
        root.setCenter(createDriveTrainPane());
        root.setRight(right);

        Scene scene = new Scene(root, 1920, 600);
        stage.setOnCloseRequest(e -> System.exit(0));
        stage.setScene(scene);
        stage.show();
    }

    private Pane createElevatorPane() {
        FeedbackRegulator elevatorPosition = FeedbackRegulatorBuilder.create()
                .minValue(0)
                .maxValue(81)
                .decimals(0)
                .textColor(Color.RED)
                .icon(FontAwesome.EXCLAMATION_TRIANGLE)
                .iconColor(Color.RED)
                .gradientStops(
                        new Stop(0.0, Color.BLACK),
                        new Stop(29d/81d, Color.BLACK),
                        new Stop(32d/81d, Color.LIMEGREEN),
                        new Stop(35d/81d, Color.BLACK),
                        new Stop(1.0, Color.BLACK)
                )
                .currentValue(31)
                .targetValue(51)
                .unit("")
                .build();

        Tile elevatorTile = TileBuilder.create()
                .title("Elevator Height")
                .prefSize(sTILE_SIZE_PX, sTILE_SIZE_PX)
                .skinType(Tile.SkinType.CUSTOM)
                .graphic(elevatorPosition)
                .text("Target Position: Cargo 2")
                .textAlignment(TextAlignment.RIGHT)
                .textSize(Tile.TextSize.SMALLER)
                .build();

        VBox left = new VBox(elevatorTile);
        VBox right = new VBox();

        HBox columns = new HBox(left,right);
        return columns;
    }

    private Pane createDriveTrainPane() {
        Gauge throttleGauge = GaugeBuilder.create()
                .skinType(Gauge.SkinType.BAR)
                .minValue(0)
                .maxValue(100)
                .build();

        Tile turn = TileBuilder.create()
                .value(0d)
                .minValue(-100)
                .maxValue(100)
                .title("Turn")
                .skinType(Tile.SkinType.GAUGE)
                .fillWithGradient(false)
                .prefSize(sTILE_SIZE_PX,sTILE_SIZE_PX)
                .build();

        Tile throttle = TileBuilder.create()
                .title("Throttle")
                .skinType(Tile.SkinType.CUSTOM)
                .graphic(throttleGauge)
                .prefSize(sTILE_SIZE_PX,2*sTILE_SIZE_PX)
                .build();

        VBox left = new VBox(throttle);
        VBox right = new VBox(turn);

        HBox columns = new HBox(left,right);
        return columns;
    }

    private static HBox createIndicatorRow(final String pText, double prefWidth) {
        Indicator indicator = new Indicator(Tile.BLUE, Tile.GRAY);
        Label label     = new Label(pText);
        label.setFont(Font.font(18));
        label.setTextFill(Tile.FOREGROUND);
        label.setAlignment(Pos.CENTER_RIGHT);
        label.setPrefWidth(prefWidth);

        HBox box = new HBox(50, indicator, label);
        box.setAlignment(Pos.CENTER);

        indicator.setOnMousePressed(e -> indicator.setOn(!indicator.isOn()));

        return box;
    }

    public static void main(String[] pArgs) {
        launch(pArgs);
    }
}
