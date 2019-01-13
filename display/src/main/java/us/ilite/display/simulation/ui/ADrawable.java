package us.ilite.display.simulation.ui;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Translation2d;

import javafx.scene.canvas.GraphicsContext;

public abstract class ADrawable {

    public abstract void draw(GraphicsContext gc, Pose2d pose, Translation2d aspectRatio);
    public void draw(GraphicsContext gc, Pose2d pose){}


}
