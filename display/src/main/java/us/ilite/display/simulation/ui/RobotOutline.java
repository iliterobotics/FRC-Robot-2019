package us.ilite.display.simulation.ui;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.geometry.Translation2d;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RobotOutline extends ADrawable {

    private Translation2d[] outlinePoints;
    private DrawablePath leftPath = new DrawablePath(Color.GREEN);
    private DrawablePath rightPath = new DrawablePath(Color.PURPLE);

    public RobotOutline(Translation2d...outlinePoints) {
        this.outlinePoints = outlinePoints;
    }

    @Override
    public void draw(GraphicsContext gc, Pose2d pose, Translation2d aspectRatio) {
        Translation2d[] pointsToDraw = new Translation2d[outlinePoints.length];
        for(int pointIndex = 0; pointIndex < outlinePoints.length; pointIndex++) {
            pointsToDraw[pointIndex] = outlinePoints[pointIndex].rotateBy(pose.getRotation());
            pointsToDraw[pointIndex] = pointsToDraw[pointIndex].translateBy(pose.getTranslation());
            pointsToDraw[pointIndex] = new Translation2d(pointsToDraw[pointIndex].x() * aspectRatio.x(), pointsToDraw[pointIndex].y() * aspectRatio.y());
        }

        gc.setStroke(Color.BLACK);
        gc.beginPath();
        gc.moveTo(pointsToDraw[0].x(), pointsToDraw[0].y());
        for(int pointIndex = 1; pointIndex <= pointsToDraw.length; pointIndex++) {
            gc.lineTo(pointsToDraw[pointIndex % pointsToDraw.length].x(), pointsToDraw[pointIndex % pointsToDraw.length].y());
        }
        gc.stroke();
        gc.closePath();

        Translation2d leftSide = outlinePoints[0].translateBy(new Translation2d(33.91 / 2.0, 0)).rotateBy(pose.getRotation()).translateBy(pose.getTranslation());
        Translation2d rightSide = outlinePoints[1].translateBy(new Translation2d(33.91 / 2.0, 0)).rotateBy(pose.getRotation()).translateBy(pose.getTranslation());
        leftSide = new Translation2d(leftSide.x() * aspectRatio.x(), leftSide.y() * aspectRatio.y());
        rightSide = new Translation2d(rightSide.x() * aspectRatio.x(), rightSide.y() * aspectRatio.y());
        leftPath.draw(gc, new Pose2d(leftSide, Rotation2d.fromDegrees(0.0)));
        rightPath.draw(gc, new Pose2d(rightSide, Rotation2d.fromDegrees(0.0)));
    }

    public void clear() {
        leftPath.clear();
        rightPath.clear();
    }

}
