package us.ilite.display;

import edu.wpi.first.networktables.NetworkTableInstance;
import javafx.application.Application;
import javafx.stage.Stage;
import us.ilite.common.io.Network;
import us.ilite.display.simulation.Simulation;
import us.ilite.display.simulation.ui.FieldWindow;
import us.ilite.robot.HenryProfile;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage pStage) throws InterruptedException {

        Simulation mSimulation = new Simulation(new HenryProfile(), 0.01);
        FieldWindow mFieldWindow = new FieldWindow(mSimulation, 0.01);

        mFieldWindow.start(pStage);
        // Sleep for a bit to allow simulation to fill draw queue

        NetworkTableInstance.getDefault().startServer();
        NetworkTableInstance.getDefault().startClient("localhost");
        mSimulation.start();
        mFieldWindow.startDrawing();

    }
}
