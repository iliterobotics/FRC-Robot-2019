package us.ilite.display;

import com.flybotix.hfr.codex.CodexOf;
import com.flybotix.hfr.codex.CodexReceiver;
import com.flybotix.hfr.io.MessageProtocols;
import com.flybotix.hfr.io.receiver.IReceiveProtocol;
import org.apache.commons.lang3.EnumUtils;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.common.types.manipulator.EElevator;
import us.ilite.common.types.sensor.EPowerDistPanel;

import java.util.HashMap;
import java.util.Map;

public class IliteCodexReceiver {

    private final IReceiveProtocol mCommsReceiver;

    private Map<Class, CodexReceiver> mReceivers = new HashMap<>();

    private Map<String, Double> mCachedValues = new HashMap<>();

    public <E extends Enum<E> & CodexOf<Double>> CodexReceiver getReceiver(Class<E> pEnum) {
        return mReceivers.get(pEnum);
    }

    /**
     * @param pDataElement The data element to return
     * @param <E> The enumeration element
     * @return double value of the data elment
     */
    public <E extends Enum<E> & CodexOf<Double>> Double get(E pDataElement) {
        return mCachedValues.get(pDataElement.name());
    }

    private IliteCodexReceiver() {
        mCommsReceiver = MessageProtocols.createReceiver(MessageProtocols.EProtocol.UDP, SystemSettings.sCODEX_COMMS_PORT, null);
        create(EDriveData.class, mCommsReceiver);
        create(EPowerDistPanel.class, mCommsReceiver);
        create(ELogitech310.class, mCommsReceiver);
        create(EElevator.class, mCommsReceiver);
        create(EPowerDistPanel.class, mCommsReceiver);

        // Test data
        for(EPowerDistPanel power : EPowerDistPanel.values()) {
            mCachedValues.put(power.name(), power.BREAKER_VALUE);
        }
    }

    private final <E extends Enum<E> & CodexOf<Double>> void create(Class<E> pEnum, IReceiveProtocol pProtocol) {
        CodexReceiver<Double, E> rec = new CodexReceiver<Double,E>(pEnum, pProtocol);
        mReceivers.put(EDriveData.class, rec);
        rec.addListener(codex -> {
            for(E e : EnumUtils.getEnumList(pEnum)) {
                mCachedValues.put(e.name(), codex.get(e));
            }
            System.out.println(codex);
        });
    }

    public void disconnect() {
        mCommsReceiver.disconnect();
    }

    private static IliteCodexReceiver INSTNACE;

    public static IliteCodexReceiver getInstance() {
        if(INSTNACE == null) INSTNACE = new IliteCodexReceiver();
        return INSTNACE;
    }
}
