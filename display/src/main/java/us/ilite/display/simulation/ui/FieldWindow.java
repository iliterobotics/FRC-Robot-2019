package us.ilite.display.simulation.ui;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.geometry.Translation2d;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import us.ilite.display.simulation.SimData;
import us.ilite.display.simulation.ISimulationListener;
import us.ilite.display.simulation.TrackingSimulation;
import us.ilite.robot.auto.paths.RobotDimensions;

public class FieldWindow extends Application implements ISimulationListener {

    private Image fieldImage;
    private Canvas fieldCanvas;
    private GraphicsContext fieldContext;
    private Text durationDisplay;
    private Text mouseXInches, mouseYInches;
    private Button playButton, pauseButton;

    private Translation2d fieldInchesToPixels;
    private RobotOutline robotOutline = new RobotOutline(new Translation2d(-RobotDimensions.kBackToCenter, -RobotDimensions.kSideToCenter),
                                                         new Translation2d(-RobotDimensions.kBackToCenter, RobotDimensions.kSideToCenter),
                                                         new Translation2d(RobotDimensions.kFrontToCenter, RobotDimensions.kSideToCenter),
                                                         new Translation2d(RobotDimensions.kFrontToCenter, -RobotDimensions.kSideToCenter));
    private DrawablePath robotPath = new DrawablePath(Color.BLUE);

    private final TrackingSimulation mTrackingSimulation;

    private Queue<SimData> drawQueue;
    private SimData nextDataToDraw = new SimData(new Pose2d(), new Pose2d());
    private UpdateThread updateThread;
    private boolean mIsPaused = false;

    private final double kDt;

    public FieldWindow(TrackingSimulation pTrackingSimulation, double pDt) {
        mTrackingSimulation = pTrackingSimulation;
        kDt = pDt;
        drawQueue = new LinkedList<>();
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

        playButton.setOnAction(e -> play());
        pauseButton.setOnAction(e -> pause());

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

        mTrackingSimulation.getDriveSimulation().addListener(this);
        mTrackingSimulation.simulate();

        updateThread = new UpdateThread();


        sidePane.getChildren().addAll(durationDisplay, playButton, pauseButton);
        bottomPane.getChildren().addAll(mouseXInches, mouseYInches);
        root.setCenter(fieldCanvas);
        root.setRight(sidePane);
        root.setBottom(bottomPane);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private class UpdateThread extends Thread {

        @Override
        public void run() {
            super.run();
            double lastTimePolled = System.currentTimeMillis();
            double lastTimeDrawn = System.currentTimeMillis();
            double startTime = System.currentTimeMillis();
            double currentTime = System.currentTimeMillis();


            while(!Thread.interrupted()) {

                if(mIsPaused) {
                    double pauseStartTime = System.currentTimeMillis();
                    // Without this print statement we can't break out of the loop
                    while (mIsPaused) {
                        System.out.println("Paused");
                    }
                    // Adjust our start time so time elapsed isn't off
                    startTime = startTime + (System.currentTimeMillis() - pauseStartTime);
                } else {
                    currentTime = System.currentTimeMillis();
                }

                // If we have to draw this iteration, don't clog up our timing by getting the next pose to draw
                // Draw @ 30 Hz
                if(currentTime - lastTimeDrawn > (1.0 / 30.0) * 1000.0) {
                    drawLatest();
                    setRunTime((currentTime - startTime) / 1000.0);
                    lastTimeDrawn = currentTime;
                } else {

                    if(drawQueue.isEmpty()) break;

                    // Update pose to draw @ same rate as simulation ran
                    if(currentTime - lastTimePolled >= (kDt * 1000)) {
                        nextDataToDraw = drawQueue.poll();
                        lastTimePolled = currentTime;
                    }
                }

            }

        }
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

    public void update(double timestamp, SimData simData) {
        setRunTime(timestamp);
        drawQueue.add(simData);
    }

    public void startDrawing() {
        updateThread.start();
    }

    public void drawLatest() {
        reset();

        Pose2d robotPose = normalizePoseToField(nextDataToDraw.current_pose);
        Pose2d targetPose = normalizePoseToField(nextDataToDraw.target_pose);

        robotOutline.draw(fieldContext, robotPose, fieldInchesToPixels);
        robotPath.draw(fieldContext, targetPose, fieldInchesToPixels);
    }

    private Pose2d normalizePoseToField(Pose2d pose) {
        Translation2d normalizedTranslation = new Translation2d(pose.getTranslation().x(), Math.abs(pose.getTranslation().y()));
        Rotation2d normalizedRotation = pose.getRotation();

        return new Pose2d(normalizedTranslation, normalizedRotation);
    }

    private void drawFieldImage() {
        fieldContext.drawImage(fieldImage, 0.0, 0.0);
    }

    private void setRunTime(double runTime) {
        durationDisplay.setText("Run Time: " + String.format("%.2f", runTime));
    }

    private void play() {
        if(drawQueue.isEmpty()) {
            updateThread = new UpdateThread();
            mTrackingSimulation.simulate();
            clear();
            startDrawing();
        }
        mIsPaused = false;
    }

    private void pause() {
        mIsPaused = true;
    }

}
