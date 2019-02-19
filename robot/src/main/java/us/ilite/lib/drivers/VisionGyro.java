package us.ilite.lib.drivers;

import java.util.Arrays;

import com.flybotix.hfr.codex.Codex;
import com.team254.lib.geometry.Rotation2d;

import us.ilite.common.Data;
import us.ilite.common.types.ETargetingData;

public class VisionGyro extends IMU {

    private static final double[] kCollisionGains = {1.0};

    private Codex<Double, ETargetingData> mTargetingData;
    private Rotation2d mGyroOffsetX = new Rotation2d();
    private Rotation2d mGyroOffsetY = new Rotation2d();

    public VisionGyro(Data pData) {
        super(kCollisionGains);
        mTargetingData = pData.limelight;
    }

    @Override
    public double getYaw() {
        return getX().rotateBy(mGyroOffsetX).getDegrees();
    }

    @Override
    public double getPitch() {
        return getY().rotateBy(mGyroOffsetY).getDegrees();
    }

    @Override
    public double getRoll() {
        return 0;
    }

    @Override
    public void zeroAll() {
        mGyroOffsetX = getX();
        mGyroOffsetY = getY();
    }

    @Override
    protected double getRawAccelX() {
        return 0;
    }

    @Override
    protected double getRawAccelY() {
        return 0;
    }

    private Rotation2d getX() {
        Double x = mTargetingData.get(ETargetingData.tx);

        if(x != null) {
            return Rotation2d.fromDegrees(x);
        }
        
        return new Rotation2d();
    }

    private Rotation2d getY() {
        Double y = mTargetingData.get(ETargetingData.ty);

        if(y != null) {
            return Rotation2d.fromDegrees(y);
        }
        
        return new Rotation2d();
    }

    @Override
    protected void updateSensorCache(double pTimestampNow) {

    }



}
