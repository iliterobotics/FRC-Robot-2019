package us.ilite.display;

import edu.wpi.first.networktables.NetworkTableInstance;
import javafx.application.Application;
import javafx.stage.Stage;
import us.ilite.display.simulation.SimRobot;
import us.ilite.display.simulation.ui.FieldWindow;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage pStage) {

        FieldWindow mFieldWindow = new FieldWindow(0.01);

        mFieldWindow.start(pStage);
        // Sleep for a bit to allow simulation to fill draw queue

        NetworkTableInstance.getDefault().startServer();
        NetworkTableInstance.getDefault().startClient("localhost");

    }
}
