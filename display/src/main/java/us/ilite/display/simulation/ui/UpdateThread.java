package us.ilite.display.simulation.ui;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import us.ilite.common.Data;
import us.ilite.display.simulation.SimData;

import java.util.LinkedList;
import java.util.Queue;

public class UpdateThread extends Thread {

    private FieldWindow fieldWindow;
    private Queue<SimData> drawQueue;
    private SimData nextDataToDraw = null;
    private boolean mIsPaused = false;


    public UpdateThread(FieldWindow fieldWindow) {
        this.fieldWindow = fieldWindow;
        drawQueue = new LinkedList<>();
    }

    @Override
    public void run() {
        super.run();
        double lastTimeDrawn = System.currentTimeMillis();
        double currentTime = System.currentTimeMillis();

        while(!Thread.interrupted()) {
            SimData data = getFromNt();
            if(data != null) {
                fieldWindow.drawData(data);
            }
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private SimData getFromNt() {
        double x = Data.kSmartDashboard.getEntry("Odometry X").getDouble(-1.0);
        double y = Data.kSmartDashboard.getEntry("Odometry Y").getDouble(-1.0);
        double heading = Data.kSmartDashboard.getEntry("Odometry Heading").getDouble(-1.0);

        if(x == -1.0 || y == -1.0 || heading == -1.0) {
            return null;
        } else {
            return new SimData(
                    new Pose2d(x, y, Rotation2d.fromDegrees(heading)),
                    new Pose2d()
            );
        }

    }

    public void play() {
        mIsPaused = false;
    }

    public void pause() {
        mIsPaused = true;
    }

}
