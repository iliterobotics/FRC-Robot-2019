package us.ilite.common.config;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

/**
 * Add your docs here.
 */
public class AbstractSystemSettingsUtilsTest {
    @Test
    public void testClassLoadingType() {
        TestObj testObj = new TestObj();
        assertEquals(500, TestObj.kControlLoopPeriod,0);
        assertEquals(500, TestObj.NETWORK_TABLE_UPDATE_RATE,0);
        AbstractSystemSettingsUtils.copyOverValues(PracticeBotSystemSettings.getInstance(), testObj);

        assertEquals(0.01, TestObj.kControlLoopPeriod,0);
        assertEquals(0.01, TestObj.NETWORK_TABLE_UPDATE_RATE,0);
    }
    @Test
    public void testPrintObject() {
        TestObj testObj = new TestObj();
        Map<String, String> allPropsAndVals = AbstractSystemSettingsUtils.getAllCurrentPropsAndValues(testObj);
        
        assertTrue(allPropsAndVals.containsKey("kControlLoopPeriod"));
        assertTrue(allPropsAndVals.containsKey("NETWORK_TABLE_UPDATE_RATE"));

        String kControl = allPropsAndVals.remove("kControlLoopPeriod");
        assertEquals(Double.toString(TestObj.kControlLoopPeriod), kControl);

        String network = allPropsAndVals.remove("NETWORK_TABLE_UPDATE_RATE");
        assertEquals(Double.toString(TestObj.kControlLoopPeriod), network);
        assertTrue(allPropsAndVals.isEmpty());
    }
}
