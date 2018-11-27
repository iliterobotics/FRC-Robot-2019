package ui;

import javafx.scene.canvas.GraphicsContext;
import us.ilite.common.lib.geometry.Pose2d;
import us.ilite.common.lib.geometry.Translation2d;

public abstract class ADrawable {

    public abstract void draw(GraphicsContext gc, Pose2d pose, Translation2d aspectRatio);
    public void draw(GraphicsContext gc, Pose2d pose){}


}
