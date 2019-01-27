/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package us.ilite.robot.os.commands;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

/**
 * Add your docs here.
 */
public class GetLocalIPTest {

    @Test
    public void testNullReader() {
        Optional<String> returnVal = GetLocalIP.getIPFromInputStream(null);
        assertNotNull(returnVal);
        assertTrue(returnVal.isEmpty());
    }
    @Test
    public void testEmptyReader() {
        Optional<String> returnVal = GetLocalIP.getIPFromInputStream(null);
        assertNotNull(returnVal);
        assertTrue(returnVal.isEmpty());
    }
}
