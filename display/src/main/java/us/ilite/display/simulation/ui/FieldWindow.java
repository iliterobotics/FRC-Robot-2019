package us.ilite.display.simulation.ui;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.geometry.Translation2d;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import us.ilite.display.simulation.SimData;
import us.ilite.display.simulation.SimRobot;
import us.ilite.robot.auto.paths.RobotDimensions;

import java.io.File;

public class FieldWindow extends Application {

    public static final double kDrawsPerSecond = 30.0;

    private Image fieldImage;
    private Canvas fieldCanvas;
    private GraphicsContext fieldContext;
    private Text simClock;
    private Text mouseXInches, mouseYInches;
    private Button playButton, pauseButton;
    private ToggleButton simToggle;
    private SimRobot mSimulation = new SimRobot(0.01);

    private Translation2d fieldInchesToPixels;
    private RobotOutline robotOutline = new RobotOutline(new Translation2d(-RobotDimensions.kBackToCenter, -RobotDimensions.kSideToCenter),
                                                         new Translation2d(-RobotDimensions.kBackToCenter, RobotDimensions.kSideToCenter),
                                                         new Translation2d(RobotDimensions.kFrontToCenter, RobotDimensions.kSideToCenter),
                                                         new Translation2d(RobotDimensions.kFrontToCenter, -RobotDimensions.kSideToCenter));
    private DrawablePath robotPath = new DrawablePath(Color.BLUE);

    private UpdateThread updateThread;

    public final double kDt;

    public FieldWindow(double kDt) {
        this.kDt = kDt;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        BorderPane root = new BorderPane();
        VBox sidePane = new VBox(5);
        HBox bottomPane = new HBox();
        Scene scene = new Scene(root, 800, 600);

        sidePane.setAlignment(Pos.TOP_CENTER);
        sidePane.setPadding(new Insets(10, 10, 10, 10));

        simClock = new Text("0.0");
        mouseXInches = new Text("X");
        mouseYInches = new Text("Y");
        playButton = new Button("Play");
        pauseButton = new Button("Pause");
        simToggle = new ToggleButton("Simulate");

        playButton.setOnAction(e -> {
            if(!updateThread.isAlive()) {
                updateThread = new UpdateThread(this);
                updateThread.start();
                simClock.setVisible(false);
                resetAll();
            } else {
                updateThread.resume();
            }
            if(simToggle.isSelected() && !mSimulation.isRunning()) {
                mSimulation = new SimRobot(0.01);
                mSimulation.start();
                simClock.setVisible(true);
                resetAll();
            } else {
                mSimulation.resume();
            }
        });
        pauseButton.setOnAction(e -> {
            updateThread.suspend();
            if(simToggle.isSelected()) mSimulation.suspend();
        });
        simToggle.setOnAction(e -> {
            if(!simToggle.isSelected()) {
                mSimulation.stop();
                updateThread.stop();
                simClock.setVisible(false);
                resetAll();
            }
        });
        simToggle.setSelected(false);

        try {
            fieldImage = new Image(new File("field.png").toURI().toURL().toExternalForm(), 640, 480, true, false);
        } catch (Exception pE) {
            pE.printStackTrace();
        }

        fieldCanvas = new Canvas(fieldImage.getWidth(), fieldImage.getHeight());
        fieldInchesToPixels = new Translation2d( fieldCanvas.getWidth() / (27.0 * 12.0), fieldCanvas.getHeight() / (27.0 * 12.0));
        fieldContext = fieldCanvas.getGraphicsContext2D();
        fieldContext.setLineWidth(2.0);

        fieldCanvas.setOnMouseMoved(e -> {
            double mouseXInchesVal = e.getX()/fieldInchesToPixels.x();
            double mouseYInchesVal = Math.abs(e.getY()) / fieldInchesToPixels.y();

            mouseXInches.setText("X: " + mouseXInchesVal);
            mouseYInches.setText("Y: " + mouseYInchesVal);
        });

        resetAll();

        updateThread = new UpdateThread(this);

        sidePane.getChildren().addAll(simClock, playButton, pauseButton, simToggle);
        bottomPane.getChildren().addAll(mouseXInches, mouseYInches);
        root.setCenter(fieldCanvas);
        root.setRight(sidePane);
        root.setBottom(bottomPane);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void resetField() {
        fieldContext.clearRect(0.0, 0.0, fieldCanvas.getWidth(), fieldCanvas.getHeight());
        drawFieldImage();
    }

    public void resetAll() {
        robotOutline.clear();
        robotPath.clear();
        drawSimTime(0.0);
        resetField();
    }

    public void drawData(SimData pNextDataToDraw) {
        resetField();
        Pose2d robotPose = normalizePoseToField(pNextDataToDraw.current_pose);
        Pose2d targetPose = normalizePoseToField(pNextDataToDraw.target_pose);

        robotOutline.draw(fieldContext, robotPose, fieldInchesToPixels, simToggle.isSelected());
        robotPath.draw(fieldContext, targetPose, fieldInchesToPixels);

        drawSimTime(mSimulation.getTime());
    }

    private Pose2d normalizePoseToField(Pose2d pose) {
        Translation2d normalizedTranslation = new Translation2d(pose.getTranslation().x(), pose.getTranslation().y());
        Rotation2d normalizedRotation = pose.getRotation();

        return new Pose2d(normalizedTranslation, normalizedRotation);
    }

    private void drawFieldImage() {
        fieldContext.drawImage(fieldImage, 0.0, 0.0);
    }

    private void drawSimTime(double runTime) {
        simClock.setText("Sim Time: " + String.format("%.2f", runTime));
    }

    public boolean shouldSimulate() {
        return simToggle.isSelected();
    }


}
