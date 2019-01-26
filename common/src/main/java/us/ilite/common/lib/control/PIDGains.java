package us.ilite.common.lib.control;

public class PIDGains {
    public final double mP;
    public final double mI;
    public final double mD;
    public final double mF;

    public PIDGains(double kP, double kI, double kD) {
        this(kP, kI, kD, 0d);
    }

    public PIDGains(double kP, double kI, double kD, double kF) {
        mP = kP;
        mI = kI;
        mD = kD;
        mF = kF;
    }
}