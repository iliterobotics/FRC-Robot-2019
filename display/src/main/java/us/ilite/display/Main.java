package us.ilite.display;

import javafx.application.Application;
import javafx.stage.Stage;
import us.ilite.display.simulation.TrackingSimulation;
import us.ilite.display.simulation.ui.FieldWindow;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage pStage) throws InterruptedException {
        TrackingSimulation mTrackingSimulation = new TrackingSimulation(0.01);

        FieldWindow mFieldWindow = new FieldWindow(mTrackingSimulation, 0.01);

        mFieldWindow.start(pStage);
        // Sleep for a bit to allow simulation to fill draw queue
        Thread.sleep(1000);
        mFieldWindow.startDrawing();
    }
}
