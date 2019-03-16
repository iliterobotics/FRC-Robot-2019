package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;

import java.util.Objects;

public class DriveMessage {

  public static final DriveMessage kNeutral = new DriveMessage(0.0, 0.0, ControlMode.PercentOutput).setNeutralMode(NeutralMode.Brake);

  public final double leftOutput, rightOutput;
  public ControlMode leftControlMode = ControlMode.PercentOutput, rightControlMode = ControlMode.PercentOutput;

  public DemandType leftDemandType = DemandType.ArbitraryFeedForward;
  public DemandType rightDemandType = DemandType.ArbitraryFeedForward;
  public double leftDemand = 0.0, rightDemand = 0.0;
  public NeutralMode leftNeutralMode = NeutralMode.Brake, rightNeutralMode = NeutralMode.Brake;

  public DriveMessage(double leftOutput, double rightOutput, ControlMode pControlMode) {
    this(leftOutput, rightOutput, pControlMode, pControlMode);
  }

  public DriveMessage(double leftOutput, double rightOutput, ControlMode leftControlMode, ControlMode rightControlMode) {
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
    return new DriveMessage(pThrottle + pTurn, pThrottle - pTurn, ControlMode.PercentOutput);
  }

  public DriveMessage setDemand(DemandType pDemandType, double pLeftDemand, double pRightDemand) {
    this.leftDemandType = pDemandType;
    this.rightDemandType = pDemandType;
    this.leftDemand = pLeftDemand;
    this.rightDemand = pRightDemand;
    return this;
  }

  public DriveMessage setNeutralMode(NeutralMode pLeftMode, NeutralMode pRightMode) {
    this.leftNeutralMode = pLeftMode;
    this.rightNeutralMode = pRightMode;
    return this;
  }

  public DriveMessage setNeutralMode(NeutralMode pMode) {
    this.leftNeutralMode = pMode;
    this.rightNeutralMode = pMode;
    return this;
  }

  public DriveMessage setControlMode(ControlMode pControlMode) {
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
            leftDemandType == that.leftDemandType &&
            rightDemandType == that.rightDemandType &&
            leftNeutralMode == that.leftNeutralMode &&
            rightNeutralMode == that.rightNeutralMode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(leftOutput, rightOutput, leftControlMode, rightControlMode, leftDemandType, rightDemandType, leftDemand, rightDemand, leftNeutralMode, rightNeutralMode);
  }

}
