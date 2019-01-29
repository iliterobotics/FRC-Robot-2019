package us.ilite.lib.drivers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

import org.junit.Test;
import us.ilite.lib.drivers.GetLocalIP;

/**
 * Unit test class to test {@link GetLocalIP}
 */
public class GetLocalIPTest {

    /**
     * Method to test the {@link GetLocalIP#getIPFromInputStream(BufferedReader)}. 
     * 
     * Parameter: Null
     * Expected behavior: The method should return an empty optional indiciating that 
     * no IP address could be found
     */
    @Test
    public void testNullReader() {
        Optional<String> returnVal = GetLocalIP.getIPFromInputStream(null);
        assertNotNull(returnVal);
        assertTrue(returnVal.isEmpty());
    }

    /**
     * Method to test the {@link GetLocalIP#getIPFromInputStream(BufferedReader)}. 
     * 
     * Parameter: mocked buffered reader that will throw an IllegalArgumentException when the 
     * readLine method is called. 
     * Expected behavior: The method should return an empty optional indiciating that 
     * no IP address could be found
     */
    @Test
    public void testReaderIOException() throws IOException{
        BufferedReader br = mock(BufferedReader.class);
        when(br.readLine()).thenThrow(IOException.class);
        Optional<String> returnVal = GetLocalIP.getIPFromInputStream(br);
        assertNotNull(returnVal);
        assertTrue(returnVal.isEmpty());
    }
    /**
     * Method to test the {@link GetLocalIP#getIPFromInputStream(BufferedReader)}. 
     * 
     * Parameter: mocked BufferedReader that will first return a string with no IP 
     * and then a null string so that the method reading the buffered reader does not 
     * get stuck in an infinite loop. 
     * Expected behavior: The method should return an empty optional indiciating that 
     * no IP address could be found
     */
    @Test
    public void testReaderNoIPs() throws IOException{
        BufferedReader br = mock(BufferedReader.class);
        //We want to return a bogus string first and then null so there is not an infinite loop
        when(br.readLine()).thenReturn("Test String",(String)null);
        Optional<String> returnVal = GetLocalIP.getIPFromInputStream(br);
        assertNotNull(returnVal);
        assertTrue(returnVal.isEmpty());
    }

    /**
     * Method to test the {@link GetLocalIP#getIPFromInputStream(BufferedReader)}. 
     * 
     * Parameter: mocked bufferreader that will first return a valid IP address the first
     * time and will return the same IP address in the next iteration. The final iteration will 
     * return a null so that the buffered reader reading logic does not get stuck in an infinite loops
     * Expected behavior: The method should return a single IP that matches the value. 
     */
    @Test
    public void testReaderMultipleIpsSame() throws IOException{
        BufferedReader br = mock(BufferedReader.class);
        //We want to return a bogus string first and then null so there is not an infinite loop
        when(br.readLine()).thenReturn("10.18.85.186","10.18.85.186",(String)null);
        Optional<String> returnVal = GetLocalIP.getIPFromInputStream(br);
        assertNotNull(returnVal);
        assertFalse(returnVal.isEmpty());
        assertEquals("10.18.85.186", returnVal.get());
    }

    /**
     * Method to test the {@link GetLocalIP#getIPFromInputStream(BufferedReader)}. 
     * 
     * Parameter: mocked bufferreader that will first return a valid IP address the first
     * time and will return a different IP address in the next iteration. The final iteration will 
     * return a null so that the buffered reader reading logic does not get stuck in an infinite loops
     * Expected behavior: The method should return a single IP that matches the first IP
     **/
    @Test
    public void testReaderMultipleIpsDifferent() throws IOException{
        BufferedReader br = mock(BufferedReader.class);
        //We want to return a bogus string first and then null so there is not an infinite loop
        when(br.readLine()).thenReturn("10.18.85.186","10.18.85.187",(String)null);
        Optional<String> returnVal = GetLocalIP.getIPFromInputStream(br);
        assertNotNull(returnVal);
        assertFalse(returnVal.isEmpty());
        assertEquals("10.18.85.186", returnVal.get());
    }
}
