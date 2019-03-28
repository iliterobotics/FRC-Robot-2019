package us.ilite.robot.modules;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.flybotix.hfr.codex.Codex;
import com.team254.lib.util.DriveSignal;
import com.team254.lib.util.Util;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.lib.util.CheesyDriveHelper;
import us.ilite.common.types.ETargetingData;
import us.ilite.common.types.drive.ENeutralMode;
import us.ilite.lib.drivers.ECommonControlMode;
import us.ilite.lib.drivers.ECommonNeutralMode;

import java.util.Objects;

public class DriveMessage {

  public static final DriveMessage kNeutral = new DriveMessage(0.0, 0.0,
          ECommonControlMode.PERCENT_OUTPUT)
          .setNeutralMode(ECommonNeutralMode.BRAKE);

  private static CheesyDriveHelper mCheesyDriveHelper = new CheesyDriveHelper(SystemSettings.kCheesyDriveGains);

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



  /*
  Uses CheesyDrive to generate steering and throttle commands.
  This has the advantage of having derivative control (through "negative inertia")
  Clamps the maximum curvature of the drivetrain (wheel sensitivity), allowing the PID to be overagressive and compensate better for large lateral offsets
  Scales PID output by throttle, giving us better performance at low speeds
   */
  public static DriveMessage getCheesyDrive(double throttle, double turn) {
    boolean isQuickTurn = Math.abs(throttle) < Util.kEpsilon;

    DriveSignal cheesyOutput = mCheesyDriveHelper.cheesyDrive(throttle, turn, false);
    return new DriveMessage(cheesyOutput.getLeft(), cheesyOutput.getRight(), ECommonControlMode.PERCENT_OUTPUT).setNeutralMode(ECommonNeutralMode.BRAKE);
  }

  /*
  Implements the same clamping function as CheesyDrive.
  If throttle + turn saturates the output, the turn power being lost is applied to the other side of the drivetrain.
  This should be better when tracking targets at high speeds.
   */
  public static DriveMessage getClampedTurnDrive(double throttle, double turn) {

    double leftPwm = throttle + turn;
    double rightPwm = throttle - turn;

    if (leftPwm > 1.0) {
      rightPwm -=  (leftPwm - 1.0);
      leftPwm = 1.0;
    } else if (rightPwm > 1.0) {
      leftPwm -=  (rightPwm - 1.0);
      rightPwm = 1.0;
    } else if (leftPwm < -1.0) {
      rightPwm +=  (-1.0 - leftPwm);
      leftPwm = -1.0;
    } else if (rightPwm < -1.0) {
      leftPwm +=  (-1.0 - rightPwm);
      rightPwm = -1.0;
    }

    return new DriveMessage(leftPwm, rightPwm, ECommonControlMode.PERCENT_OUTPUT).setNeutralMode(ECommonNeutralMode.BRAKE);
  }

  /*
  Implements the same scaling function as CheesyDrive, where turn is scaled by throttle.
  This *should* give us better performance at low speeds + the benefits of "clamped turn" drive.
   */
  public static DriveMessage getCurvatureDrive(double throttle, double turn) {
    double adjustedTurn = Math.abs(throttle) * turn * SystemSettings.kTurnSensitivity;

    return DriveMessage.fromThrottleAndTurn(throttle, adjustedTurn).setNeutralMode(ECommonNeutralMode.BRAKE);
  }

  private DriveMessage getArcadeDrive(double throttle, double turn, Codex<Double, ETargetingData> targetData) {
//        mOutput *= targetData.get(ETargetingData.ta) * kTargetAreaScalar;
    return DriveMessage.fromThrottleAndTurn(throttle, turn).setNeutralMode(ECommonNeutralMode.BRAKE);
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
