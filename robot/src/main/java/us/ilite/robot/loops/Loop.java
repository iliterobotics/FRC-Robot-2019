package us.ilite.robot.loops;

import us.ilite.robot.modules.Module;

/**
 * An extension of a Module that runs a separate update method called at high frequency.
 */
public abstract class Loop extends Module {

    public abstract void loop(double pNow);

}
