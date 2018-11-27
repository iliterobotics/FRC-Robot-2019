package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;

public class DriveMessage {

  public static final DriveMessage kNeutral = new DriveMessage(0.0, 0.0, ControlMode.PercentOutput);

  public final double leftOutput, rightOutput;
  public final ControlMode leftControlMode, rightControlMode;

  public DemandType leftDemandType = DemandType.ArbitraryFeedForward;
  public DemandType rightDemandType = DemandType.ArbitraryFeedForward;
  public double leftDemand, rightDemand;
  public NeutralMode leftNeutralMode, rightNeutralMode;

  public DriveMessage(double leftOutput, double rightOutput, ControlMode pControlMode) {
    this(leftOutput, rightOutput, pControlMode, pControlMode);
  }

  public DriveMessage(double leftOutput, double rightOutput, ControlMode leftControlMode, ControlMode rightControlMode) {
    this.leftOutput = leftOutput;
    this.rightOutput = rightOutput;
    this.leftControlMode = leftControlMode;
    this.rightControlMode = rightControlMode;
    this.leftNeutralMode = NeutralMode.Brake;
    this.rightNeutralMode = NeutralMode.Brake;
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

}
