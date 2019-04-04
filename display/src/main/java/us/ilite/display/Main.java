package us.ilite.display;

import javafx.application.Application;
import javafx.stage.Stage;
import us.ilite.display.simulation.Simulation;
import us.ilite.display.simulation.ui.FieldWindow;
import us.ilite.robot.HenryProfile;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage pStage) throws InterruptedException {

        FieldWindow mFieldWindow = new FieldWindow(0.01);
        Simulation mSimulation = new Simulation(new HenryProfile(), mFieldWindow, 0.01);

        mSimulation.simulate();

        mFieldWindow.start(pStage);
        // Sleep for a bit to allow simulation to fill draw queue
        Thread.sleep(1000);
        mFieldWindow.startDrawing();
    }
}
