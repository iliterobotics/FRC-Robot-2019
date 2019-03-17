package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import us.ilite.common.types.drive.ENeutralMode;
import us.ilite.lib.drivers.ECommonControlMode;
import us.ilite.lib.drivers.ECommonNeutralMode;

import java.util.Objects;

public class DriveMessage {

  public static final DriveMessage kNeutral = new DriveMessage(0.0, 0.0,
          ECommonControlMode.PERCENT_OUTPUT)
          .setNeutralMode(ECommonNeutralMode.BRAKE);

  public final double leftOutput, rightOutput;
  public ECommonControlMode leftControlMode = ECommonControlMode.PERCENT_OUTPUT;
  public ECommonControlMode rightControlMode = ECommonControlMode.PERCENT_OUTPUT;

  public double leftDemand = 0.0, rightDemand = 0.0;
  public ECommonNeutralMode leftNeutralMode = ECommonNeutralMode.BRAKE, rightNeutralMode = ECommonNeutralMode.BRAKE;

  public DriveMessage(double leftOutput, double rightOutput, ECommonControlMode pControlMode) {
    this(leftOutput, rightOutput, pControlMode, pControlMode);
  }

  public DriveMessage(double leftOutput, double rightOutput, ECommonControlMode leftControlMode, ECommonControlMode rightControlMode) {
    this.leftOutput = leftOutput;
    this.rightOutput = rightOutput;
    this.leftControlMode = leftControlMode;
    this.rightControlMode = rightControlMode;
  }

  /**
   * Tell the drive train to go and turn.  Both are scalars from -1.0 to 1.0.
   * @param pThrottle - positive = forward, negative = reverse
   * @param pTurn - positive = right, negative = left
   * @return an open loop drivetrain message
   */
  public static DriveMessage fromThrottleAndTurn(double pThrottle, double pTurn) {
    return new DriveMessage(pThrottle + pTurn, pThrottle - pTurn, ECommonControlMode.PERCENT_OUTPUT);
  }

  public DriveMessage setDemand(double pLeftDemand, double pRightDemand) {
    this.leftDemand = pLeftDemand;
    this.rightDemand = pRightDemand;
    return this;
  }

  public DriveMessage setNeutralMode(ECommonNeutralMode pLeftMode, ECommonNeutralMode pRightMode) {
    this.leftNeutralMode = pLeftMode;
    this.rightNeutralMode = pRightMode;
    return this;
  }

  public DriveMessage setNeutralMode(ECommonNeutralMode pMode) {
    this.leftNeutralMode = pMode;
    this.rightNeutralMode = pMode;
    return this;
  }

  public DriveMessage setControlMode(ECommonControlMode pControlMode) {
    this.leftControlMode = this.rightControlMode = pControlMode;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DriveMessage that = (DriveMessage) o;
    return Double.compare(that.leftOutput, leftOutput) == 0 &&
            Double.compare(that.rightOutput, rightOutput) == 0 &&
            Double.compare(that.leftDemand, leftDemand) == 0 &&
            Double.compare(that.rightDemand, rightDemand) == 0 &&
            leftControlMode == that.leftControlMode &&
            rightControlMode == that.rightControlMode &&
            leftNeutralMode == that.leftNeutralMode &&
            rightNeutralMode == that.rightNeutralMode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(leftOutput, rightOutput, leftControlMode, rightControlMode, leftDemand, rightDemand, leftNeutralMode, rightNeutralMode);
  }

}
