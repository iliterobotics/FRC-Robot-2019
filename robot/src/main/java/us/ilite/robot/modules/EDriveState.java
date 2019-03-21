package us.ilite.robot.modules;

/**
 * Describes the different states the drivetrain may be in. If we do more Talon-based control (motion profiling, motion magic, etc.)
 * that require us to keep track of our state for configuration purposes, we can add those states here.
 */
public enum EDriveState {

    NORMAL,
    PATH_FOLLOWING,
    TARGET_ANGLE_LOCK

}
