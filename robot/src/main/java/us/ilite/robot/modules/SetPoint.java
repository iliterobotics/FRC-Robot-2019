package us.ilite.robot.modules;

public enum SetPoint
{
  FULLY_OUT(0), FULLY_UP(0), FULLY_DOWN(0);
  private int mNumTicks;
  SetPoint( int pParam )
  {
    mNumTicks = pParam;
  }

}