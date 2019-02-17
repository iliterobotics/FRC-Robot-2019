package us.ilite.common.io;


import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.flybotix.hfr.io.MessageProtocols;
import com.flybotix.hfr.io.sender.ADataSender;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class NTSender extends ADataSender {
     private final Executor mThreads = Executors.newFixedThreadPool(1);
     
     private NetworkTable mTable = null;
    
     @Override
     protected boolean usesNetAddress() {
       return false;
     }
     
     @Override
     protected void establishConnection(String addr) {
       NetworkTableInstance.getDefault().getTable(addr);
       startSendThread();
     }
    
     @Override
     protected void establishConnection(InetAddress addr) {
       // Not used
     }
    
     private void startSendThread() {
       mThreads.execute(() -> {
         while(mStatus.isConnected()) {
           ByteBuffer msg = null;
           try {
             Thread.sleep((long)(1000d/MessageProtocols.MAX_PACKET_RATE_HZ));
             msg = mMessageQ.removeFirst();
             if(msg != null) {
               byte[] arr = msg.array();
               mTable.getEntry(MessageProtocols.NT_ELEMENT_NAME).setRaw(arr);
             }
           } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
           }
         }
       });
     }
}