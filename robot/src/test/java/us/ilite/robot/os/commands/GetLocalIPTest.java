/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package us.ilite.robot.os.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
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
    @Test
    public void testReaderIOException() throws IOException{
        BufferedReader br = mock(BufferedReader.class);
        when(br.readLine()).thenThrow(IOException.class);
        Optional<String> returnVal = GetLocalIP.getIPFromInputStream(br);
        assertNotNull(returnVal);
        assertTrue(returnVal.isEmpty());
    }
    @Test
    public void testReaderNoIPs() throws IOException{
        BufferedReader br = mock(BufferedReader.class);
        //We want to return a bogus string first and then null so there is not an infinite loop
        when(br.readLine()).thenReturn("Test String",(String)null);
        Optional<String> returnVal = GetLocalIP.getIPFromInputStream(br);
        assertNotNull(returnVal);
        assertTrue(returnVal.isEmpty());
    }

    @Test
    public void testReaderMultipleIpsSame() throws IOException{
        BufferedReader br = mock(BufferedReader.class);
        //We want to return a bogus string first and then null so there is not an infinite loop
        when(br.readLine()).thenReturn("10.18.85.186","10.18.85.186",(String)null);
        Optional<String> returnVal = GetLocalIP.getIPFromInputStream(br);
        assertNotNull(returnVal);
        assertFalse(returnVal.isEmpty());
    }
}
