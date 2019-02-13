package us.ilite.robot.modules;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import us.ilite.common.Data;
import us.ilite.lib.drivers.Clock;
import us.ilite.robot.driverinput.DriverInput;

public class CargoSpitTest {

    private Data mData;
    private Clock mClock;
    private ModuleList mModuleList;
    private DriverInput mDriverInput;
    private Superstructure mSuperStructure;


    @Before
    public void setup() {

        mData = new Data();
        mClock = new Clock().simulated();
        mModuleList = new ModuleList();

    }
}
