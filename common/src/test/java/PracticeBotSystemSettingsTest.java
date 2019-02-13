/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package us.ilite.common.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Add your docs here.
 */
public class PracticeBotSystemSettingsTest {
    @Test
    public void testClassLoadingType() {
        TestObj testObj = new TestObj();
        assertEquals(500, TestObj.kControlLoopPeriod,0);
        assertEquals(500, TestObj.NETWORK_TABLE_UPDATE_RATE,0);
        PracticeBotSystemSettings.copyOverValues(testObj);

        assertEquals(0.01, TestObj.kControlLoopPeriod,0);
        assertEquals(0.01, TestObj.NETWORK_TABLE_UPDATE_RATE,0);
    }
}
