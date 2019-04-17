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
        double odomX = Data.kSmartDashboard.getEntry("Odometry X").getDouble(-1.0);
        double odomY = Data.kSmartDashboard.getEntry("Odometry Y").getDouble(-1.0);
        double odomHeading = Data.kSmartDashboard.getEntry("Odometry Heading").getDouble(-1.0);

        double targetX = Data.kSmartDashboard.getEntry("Target X").getDouble(-1.0);
        double targetY = Data.kSmartDashboard.getEntry("Target Y").getDouble(-1.0);
        double targetHeading = Data.kSmartDashboard.getEntry("Target Heading").getDouble(-1.0);

        if(odomX == -1.0 || odomY == -1.0 || odomHeading == -1 || targetX == -1.0 || targetY == -1.0 || targetHeading == -1.0) {
            return null;
        } else {
            return new SimData(
                    new Pose2d(odomX, odomY, Rotation2d.fromDegrees(odomHeading)),
                    new Pose2d(targetX, targetY, Rotation2d.fromDegrees(targetHeading))
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
