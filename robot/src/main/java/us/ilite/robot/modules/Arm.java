package us.ilite.robot.modules;

import us.ilite.common.config.SystemSettings.ArmPosition;
import us.ilite.robot.loops.Loop;

public abstract class Arm extends Loop
{

    public abstract void setArmPosition( ArmPosition position );

    public abstract void setArmAngle( double angle );

    public abstract double getCurrentArmAngle();

    /**
     * This is used for direct output control, may go away later
     * @param desiredOutput
     */
    public abstract void setDesiredOutput( double desiredOutput );

}
