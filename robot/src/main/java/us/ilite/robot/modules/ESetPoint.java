package us.ilite.robot.modules;

public enum ESetPoint
{
  FULLY_OUT(), FULLY_UP(), FULLY_DOWN(), MANUAL_STATE(); //0 degrees(min) to 135 degrees(max)
  
  int mEncoderTicks;
  ESetPoint()
  {
    //this.mEncoderTicks = pEncoderTicks;
  }

}