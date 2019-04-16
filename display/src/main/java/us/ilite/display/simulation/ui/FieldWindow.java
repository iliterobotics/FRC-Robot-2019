package us.ilite.display.simulation.ui;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.geometry.Translation2d;
import javafx.application.Application;
import javafx.geometry.Insets;
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
    private Text durationDisplay;
    private Text mouseXInches, mouseYInches;
    private Button playButton, pauseButton;
    private ToggleButton simToggle;
    private SimRobot mSimulation;

    private Translation2d fieldInchesToPixels;
    private RobotOutline robotOutline = new RobotOutline(new Translation2d(-RobotDimensions.kBackToCenter, -RobotDimensions.kSideToCenter),
                                                         new Translation2d(-RobotDimensions.kBackToCenter, RobotDimensions.kSideToCenter),
                                                         new Translation2d(RobotDimensions.kFrontToCenter, RobotDimensions.kSideToCenter),
                                                         new Translation2d(RobotDimensions.kFrontToCenter, -RobotDimensions.kSideToCenter));
    private DrawablePath robotPath = new DrawablePath(Color.BLUE);

    private UpdateThread updateThread;

    public final double kDt;

    public FieldWindow(SimRobot mSimulation, double kDt) {
        this.mSimulation = mSimulation;
        this.kDt = kDt;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        BorderPane root = new BorderPane();
        VBox sidePane = new VBox();
        HBox bottomPane = new HBox();
        Scene scene = new Scene(root, 800, 600);

        durationDisplay = new Text("0.0");
        mouseXInches = new Text("X");
        mouseYInches = new Text("Y");
        playButton = new Button("Play");
        pauseButton = new Button("Pause");
        simToggle = new ToggleButton("Simulate");

        playButton.setOnAction(e -> {
            updateThread.play();
            if(!mSimulation.isRunning()) mSimulation.start();
        });
        pauseButton.setOnAction(e -> {
            updateThread.pause();
            mSimulation.stop();
        });
        simToggle.setSelected(false);

        VBox.setMargin(sidePane, new Insets(10, 10, 10, 10));

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

        reset();

        updateThread = new UpdateThread(this);


        sidePane.getChildren().addAll(durationDisplay, playButton, pauseButton, simToggle);
        bottomPane.getChildren().addAll(mouseXInches, mouseYInches);
        root.setCenter(fieldCanvas);
        root.setRight(sidePane);
        root.setBottom(bottomPane);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void reset() {
        fieldContext.clearRect(0.0, 0.0, fieldCanvas.getWidth(), fieldCanvas.getHeight());
        drawFieldImage();
    }

    public void clear() {
        robotOutline.clear();
        robotPath.clear();
        reset();
    }

    public void startDrawing() {
        updateThread.start();
//        Thread thread = new Thread(() -> {
//            while(true) {
//                SimData data = new SimData(mSimulation.mDrive.getDriveController().getCurrentPose(), mSimulation.mDrive.getDriveController().getTargetPose());
//                drawData(data);
////                System.out.println(data.current_pose);
//                try {
//                    Thread.sleep(16);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread.start();

    }

    public void drawData(SimData pNextDataToDraw) {
        reset();
        Pose2d robotPose = normalizePoseToField(pNextDataToDraw.current_pose);
        Pose2d targetPose = normalizePoseToField(pNextDataToDraw.target_pose);

        robotOutline.draw(fieldContext, robotPose, fieldInchesToPixels);
        robotPath.draw(fieldContext, targetPose, fieldInchesToPixels);
    }

    private Pose2d normalizePoseToField(Pose2d pose) {
        Translation2d normalizedTranslation = new Translation2d(pose.getTranslation().x(), pose.getTranslation().y());
        Rotation2d normalizedRotation = pose.getRotation();

        return new Pose2d(normalizedTranslation, normalizedRotation);
    }

    private void drawFieldImage() {
        fieldContext.drawImage(fieldImage, 0.0, 0.0);
    }

    private void setRunTime(double runTime) {
        durationDisplay.setText("Run Time: " + String.format("%.2f", runTime));
    }

    public boolean shouldSimulate() {
        return simToggle.isSelected();
    }


}
