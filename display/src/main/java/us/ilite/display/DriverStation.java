package us.ilite.display;


import eu.hansolo.fx.charts.SankeyPlot;
import eu.hansolo.fx.charts.SankeyPlotBuilder;
import eu.hansolo.fx.charts.data.PlotItem;
import eu.hansolo.fx.regulators.FeedbackRegulator;
import eu.hansolo.fx.regulators.FeedbackRegulatorBuilder;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.addons.Indicator;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.commons.lang3.EnumUtils;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.manipulator.EElevator;
import us.ilite.common.types.sensor.EPowerDistPanel;

import java.util.*;

public class DriverStation extends Application {
    private static final double sSCREEN_WIDTH = 1920;
    private static final double sSCREEN_HEIGHT = 750;
    private static final double sBUTTON_BAR_HEIGHT_PX = 80;
    private static final double sTILE_SIZE_HEIGHT_PX = (sSCREEN_HEIGHT-sBUTTON_BAR_HEIGHT_PX)/2;
    private static final double sTILE_SIZE_WIDTH_PX = sSCREEN_WIDTH/6d;

    private final  IliteCodexReceiver mData =  IliteCodexReceiver.getInstance();

    @Override
    public void start(Stage stage) throws Exception {

        BorderPane root = new BorderPane();

        root.setLeft(createElevatorPane());
        root.setCenter(createPDPPane());

        Scene scene = new Scene(root, sSCREEN_WIDTH, sSCREEN_HEIGHT);
        stage.setOnCloseRequest(e -> {
            IliteCodexReceiver.getInstance().disconnect();
            System.exit(0);
        });
        stage.setScene(scene);
        stage.show();
        stage.setX(0);
        stage.setY(0);
    }

    private Stop[] mElevatorGradient = new Stop[] {
            new Stop(0.0, Color.BLACK),
            new Stop(29d/81d, Color.BLACK),
            new Stop(32d/81d, Color.LIMEGREEN),
            new Stop(35d/81d, Color.BLACK),
            new Stop(1.0, Color.BLACK)
    };


    private Pane createElevatorPane() {
        FeedbackRegulator elevatorPosition = FeedbackRegulatorBuilder.create()
                .minValue(0)
                .maxValue(81)
                .decimals(0)
                .textColor(Color.RED)
                .icon(FontAwesome.EXCLAMATION_TRIANGLE)
                .iconColor(Color.RED)
                .gradientStops(mElevatorGradient)
                .currentValue(31)
                .targetValue(51)
                .unit("")
                .build();

        Tile elevatorTile = TileBuilder.create()
                .title("Elevator Height")
                .prefSize(sTILE_SIZE_WIDTH_PX, sTILE_SIZE_HEIGHT_PX)
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

    private final Map<String, PlotItem> mPlotItems = new HashMap<>();
    private static final String
        LEFT_DRIVE_TRAIN = "Left Drive Train",
        RIGHT_DRIVE_TRAIN = "Right Drive Train",
        ELEVATOR = "Elevator",
        CLIMBER_4_BAR = "4-Bar Climber",
        INTAKE_ROLLER = "Intake Roller",
        INTAKE_WRIST = "Intake Wrist",
        CARGO_SHOOTER = "Cargo Shooter",
        SYSTEM = "System",
        PDP = "Power Distribution Panel"
    ;

    private static final String[] ALL_SUBSYSTEMS = new String[] {
            LEFT_DRIVE_TRAIN,RIGHT_DRIVE_TRAIN,ELEVATOR,CLIMBER_4_BAR,INTAKE_ROLLER,INTAKE_WRIST,
            CARGO_SHOOTER,SYSTEM
    };
    SankeyPlot pdpPlot;
    final List<PlotItem> sortedPlots = new ArrayList<>();

    private Pane createPDPPane() {
//        0	    40	NEO MAX	    9	4-bar 1
//        1	    40	TalonSRX	1	Left DT 1
//        2	    40	Victor SPX	3	Left DT 2
//        3	    40	Victor SPX	5	Left DT 3
//        5	    30	Victor SPX	13	Cargo Spit
//        6	    30	Talon SRX	16	Intake Wrist
//        9	    30	NEO MAX	    15	Elevator
//        10	30	Victor SPX	14	Cargo Spit 2
//        11	30	Victor SPX	12	Intake roller - lower
//        12	40	Victor SPX	6	Right DT 3
//        13	40	Victor SPX	4	Right DT 2
//        14	40	Talon SRX	2	Right DT 1
//        15	40	NEO MAX	    10	4-bar 2
        createPlot(LEFT_DRIVE_TRAIN, Color.BLUE);
        createPlot("PDP1", Color.BLUE);
        createPlot("PDP2", Color.BLUE);
        createPlot("PDP3", Color.BLUE);
        createPlot(RIGHT_DRIVE_TRAIN, Color.GREEN);
        createPlot("PDP12", Color.GREEN);
        createPlot("PDP13", Color.GREEN);
        createPlot("PDP14", Color.GREEN);
        createPlot(ELEVATOR, Color.PURPLE);
        createPlot("PDP9", Color.PURPLE);
        createPlot(CLIMBER_4_BAR, Color.ORANGE);
        createPlot("PDP0", Color.ORANGE);
        createPlot("PDP15", Color.ORANGE);
        createPlot(INTAKE_WRIST, Color.CYAN);
        createPlot("PDP6", Color.CYAN);
        createPlot(CARGO_SHOOTER, Color.YELLOW);
        createPlot("PDP5", Color.YELLOW);
        createPlot("PDP10", Color.YELLOW);
        createPlot(SYSTEM, Color.GRAY);
        createPlot("PDP4", Color.GRAY);
        createPlot("PDP7", Color.GRAY);
        createPlot("PDP8", Color.GRAY);
        createPlot(INTAKE_ROLLER, Color.CYAN);
        createPlot("PDP11", Color.CYAN);
        createPlot(PDP, Color.BLACK);

        pdpPlot = SankeyPlotBuilder.create()
                .showFlowDirection(true)
                .items(sortedPlots)
                .prefSize(4*sTILE_SIZE_WIDTH_PX, 2*sTILE_SIZE_HEIGHT_PX)
                .decimals(0)
                .useItemColor(true)
                .streamFillMode(SankeyPlot.StreamFillMode.GRADIENT)
                .textColor(Color.WHITE)
                .build();

        Tile pdpTile = TileBuilder.create()
                .skinType(Tile.SkinType.CUSTOM)
                .graphic(pdpPlot)
                .title("Power System")
                .prefSize(sTILE_SIZE_WIDTH_PX*4, sTILE_SIZE_HEIGHT_PX*2)
                .text("Peak Total Current (A): 157")
                .textAlignment(TextAlignment.RIGHT)
                .textSize(Tile.TextSize.SMALLER)
                .build();

        setPDPData();
        VBox left = new VBox(pdpTile);
//        VBox right = new VBox();

        HBox columns = new HBox(left);
        return columns;
    }

    private void createPlot(String pName, Color pColor) {
        mPlotItems.put(pName, new PlotItem(pName, pColor));
        sortedPlots.add(mPlotItems.get(pName));
    }

    private Pane createDriveTrainPane() {

        Tile turn = TileBuilder.create()
                .value(0d)
                .minValue(-100)
                .maxValue(100)
                .title("Turn")
                .skinType(Tile.SkinType.GAUGE)
                .fillWithGradient(false)
                .prefSize(sTILE_SIZE_WIDTH_PX,sTILE_SIZE_HEIGHT_PX)
                .build();

        Gauge throttleGauge = GaugeBuilder.create()
                .skinType(Gauge.SkinType.BAR)
                .minValue(0)
                .maxValue(100)
                .build();
        Tile throttle = TileBuilder.create()
                .title("Throttle")
                .skinType(Tile.SkinType.CUSTOM)
                .graphic(throttleGauge)
                .prefSize(sTILE_SIZE_WIDTH_PX,sTILE_SIZE_HEIGHT_PX)
                .build();

        VBox left = new VBox(throttle);
        VBox right = new VBox(turn);

        HBox columns = new HBox(left,right);
        columns.setPrefHeight(2*sTILE_SIZE_HEIGHT_PX);
        columns.setPrefWidth(4*sTILE_SIZE_WIDTH_PX);
        columns.setPadding(new Insets(5));
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

    private void setPDPData() {
//        0	40	NEO MAX	9	4-bar 1
//        1	40	TalonSRX	1	Left DT 1
//        2	40	Victor SPX	3	Left DT 2
//        3	40	Victor SPX	5	Left DT 3
//        5	30	Victor SPX	13	Cargo Spit
//        6	30	Talon SRX	16	Intake Wrist
//        9	30	NEO MAX	15	Elevator
//        10	30	Victor SPX	14	Cargo Spit 2
//        11	30	Victor SPX	12	Intake roller - lower
//        12	40	Victor SPX	6	Right DT 3
//        13	40	Victor SPX	4	Right DT 2
//        14	40	Talon SRX	2	Right DT 1
//        15	40	NEO MAX	10	4-bar 2

        double total=0;
        double climber=0;
        double leftdt=0;
        double rightdt=0;
        double intakewrist=0;
        double intakeroller=0;
        double cargospit = 0;
        double elevator = 0;
        double system = 0;

        for(PlotItem pi : mPlotItems.values()) {
            pi.clearOutgoing();
        }

        for(int i = 0; i < 16; i++) {
            double p = mData.get(EPowerDistPanel.values()[i]);
            mPlotItems.get("PDP" + i).setValue(p);
            total += p;
            switch(i) {
                case 0:
                case 15:
                    climber += p;
                    break;
                case 1:
                case 2:
                case 3:
                    leftdt += p;
                    break;
                case 12:
                case 13:
                case 14:
                    rightdt += p;
                    break;
                case 6:
                    intakewrist += p;
                    break;
                case 11:
                    intakeroller += p;
                    break;
                case 10:
                case 5:
                    cargospit += p;
                    break;
                case 9:
                    elevator += p;
                    break;
                default:
                    system += p;
            }
        }

        mPlotItems.get(INTAKE_ROLLER).setValue(intakeroller);
        mPlotItems.get(PDP).setValue(total);
        mPlotItems.get(CLIMBER_4_BAR).setValue(climber);
        mPlotItems.get(LEFT_DRIVE_TRAIN).setValue(leftdt);
        mPlotItems.get(RIGHT_DRIVE_TRAIN).setValue(rightdt);
        mPlotItems.get(INTAKE_WRIST).setValue(intakewrist);
        mPlotItems.get(CARGO_SHOOTER).setValue(cargospit);
        mPlotItems.get(ELEVATOR).setValue(elevator);
        mPlotItems.get(SYSTEM).setValue(system);


        mPlotItems.get(INTAKE_ROLLER).addToOutgoing(mPlotItems.get("PDP11"),mPlotItems.get("PDP11").getValue());
        mPlotItems.get(SYSTEM).addToOutgoing(mPlotItems.get("PDP4"),mPlotItems.get("PDP4").getValue());
        mPlotItems.get(SYSTEM).addToOutgoing(mPlotItems.get("PDP7"),mPlotItems.get("PDP7").getValue());
        mPlotItems.get(SYSTEM).addToOutgoing(mPlotItems.get("PDP8"),mPlotItems.get("PDP8").getValue());
        mPlotItems.get(ELEVATOR).addToOutgoing(mPlotItems.get("PDP9"),mPlotItems.get("PDP9").getValue());
        mPlotItems.get(CARGO_SHOOTER).addToOutgoing(mPlotItems.get("PDP5"),mPlotItems.get("PDP5").getValue());
        mPlotItems.get(CARGO_SHOOTER).addToOutgoing(mPlotItems.get("PDP10"),mPlotItems.get("PDP10").getValue());
        mPlotItems.get(INTAKE_WRIST).addToOutgoing(mPlotItems.get("PDP6"),mPlotItems.get("PDP6").getValue());
        mPlotItems.get(RIGHT_DRIVE_TRAIN).addToOutgoing(mPlotItems.get("PDP12"),mPlotItems.get("PDP12").getValue());
        mPlotItems.get(RIGHT_DRIVE_TRAIN).addToOutgoing(mPlotItems.get("PDP13"),mPlotItems.get("PDP13").getValue());
        mPlotItems.get(RIGHT_DRIVE_TRAIN).addToOutgoing(mPlotItems.get("PDP14"),mPlotItems.get("PDP14").getValue());
        mPlotItems.get(LEFT_DRIVE_TRAIN).addToOutgoing(mPlotItems.get("PDP1"),mPlotItems.get("PDP1").getValue());
        mPlotItems.get(LEFT_DRIVE_TRAIN).addToOutgoing(mPlotItems.get("PDP2"),mPlotItems.get("PDP2").getValue());
        mPlotItems.get(LEFT_DRIVE_TRAIN).addToOutgoing(mPlotItems.get("PDP3"),mPlotItems.get("PDP3").getValue());
        mPlotItems.get(CLIMBER_4_BAR).addToOutgoing(mPlotItems.get("PDP0"),mPlotItems.get("PDP1").getValue());
        mPlotItems.get(CLIMBER_4_BAR).addToOutgoing(mPlotItems.get("PDP15"),mPlotItems.get("PDP15").getValue());

        for(String s : ALL_SUBSYSTEMS) {
            mPlotItems.get(PDP).addToOutgoing(mPlotItems.get(s), mPlotItems.get(s).getValue());
        }


    }

    public static void main(String[] pArgs) {
        // Start the receivers before we start the display
        IliteCodexReceiver.getInstance();

        System.out.println("Listening for Codex on " + SystemSettings.sCODEX_COMMS_PORT);

        // Start the display
        launch(pArgs);
    }
}
