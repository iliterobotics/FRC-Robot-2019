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
            if(mIsPaused) {
                // Without this print statement we can't break out of the loop
                while (true) {
                    if (mIsPaused) break;
                    System.out.println("Paused");
                }
            } else {
                currentTime = System.currentTimeMillis();
                // If we have to draw this iteration, don't clog up our timing by getting the next pose to draw
                // Draw @ 30 Hz
                if(currentTime - lastTimeDrawn > (1.0 / FieldWindow.kDrawsPerSecond) * 1000.0) {
                    if(drawQueue.peek() != null) {
//                        System.out.println(nextDataToDraw.current_pose);
                        fieldWindow.drawData(drawQueue.poll());
                    }
                    lastTimeDrawn = currentTime;
                } else {
                    SimData data = getFromNt();
                    drawQueue.add(data);
                }
            }

            try {
                Thread.sleep(1);
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
