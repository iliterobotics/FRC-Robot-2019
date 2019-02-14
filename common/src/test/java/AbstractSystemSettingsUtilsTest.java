package us.ilite.common.config;

import static org.junit.Assert.assertEquals;

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
}
