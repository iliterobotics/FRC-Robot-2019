package us.ilite.common.io;

import java.util.function.Consumer;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

import edu.wpi.first.networktables.ConnectionInfo;
import edu.wpi.first.networktables.ConnectionNotification;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * This class is a helper class that resolves commonly-needed information based
 * upon the 3 different networks the FRC bot can see: - Ethernet (10.18.85) -
 * USB (172 network) - Wireless (10.18.85)
 * 
 * @param <ConnectionInfo>
 */
public class Network {
    private final ILog sLOG = Logger.createLog(Network.class);
    private static final Network INFO = new Network();
    private ConnectionInfo mConnectionInfo = null;

    public static Network getInstance() {
        return INFO;
    }

    /**
     * @return the current connection information.  This will be NULL during robotInit()
     * but NOT NULL during disabledInit(), auton, and teleop
     */
    public ConnectionInfo getConnectionInfo() {
        return mConnectionInfo;
    } 

    private Network() {
        Consumer<ConnectionNotification> listener = conn -> {
            mConnectionInfo = conn.conn;
            sLOG.info("=== Remote Connection Info ===");
            sLOG.info(mConnectionInfo.remote_ip + " : " + mConnectionInfo.remote_port);
        };
        NetworkTableInstance.getDefault().addConnectionListener(listener, true);
    }
}