
package us.ilite.robot.modules;

/**
 * Base class for data from searching for a target in a camera
 */
public interface ITargetingData {

    public double getTx();

    public double getTy();

    public double getTa();

    public double getTs();
    
    public double getTl();
    
    public double getTshort();
    
    public double getTlong();
    
    public double getTHoriz(); 
    
    public double getTvert();

    public boolean getTv();

}
