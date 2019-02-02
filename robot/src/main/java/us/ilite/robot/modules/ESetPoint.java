package us.ilite.robot.modules;

public enum ESetPoint
{
  //Math these up
  FULLY_OUT(0), FULLY_UP(0), FULLY_DOWN(0);
  

  int mEncoderTicks;
  ESetPoint( int pEncoderTicks )
  {
    this.mEncoderTicks = pEncoderTicks;
  }

}