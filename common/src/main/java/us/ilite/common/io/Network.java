package us.ilite.common.io;

import java.util.function.Consumer;

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
    public static final Network INFO = new Network();
    private ConnectionInfo mConnectionInfo = null;

    private Network() {
        Consumer<ConnectionNotification> listener = conn -> {
            mConnectionInfo = conn.conn;
            System.out.println("=== Connection Info ===");
            System.out.println(mConnectionInfo.remote_ip + " : " + mConnectionInfo.remote_port);
        };
        NetworkTableInstance.getDefault().addConnectionListener(listener, true);
    }
}