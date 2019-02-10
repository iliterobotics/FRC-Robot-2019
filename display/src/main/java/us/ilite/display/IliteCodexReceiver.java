package us.ilite.display;

import com.flybotix.hfr.codex.CodexOf;
import com.flybotix.hfr.codex.CodexReceiver;
import com.flybotix.hfr.io.MessageProtocols;
import com.flybotix.hfr.io.receiver.IReceiveProtocol;
import javafx.beans.property.Property;
import us.ilite.common.Data;
import us.ilite.common.types.ETargetingData;
import us.ilite.common.types.drive.EDriveData;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.display.io.CodexPropertyDouble;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class IliteCodexReceiver {

    private final IReceiveProtocol mCommsReceiver;
    private final Executor mThreadPool;

    private Map<Class, ReciverPropertyMap> mProperties = new HashMap<>();

    public <E extends Enum<E> & CodexOf<Double>> void bind(Property<Double> pProperty, Class<E> pEnum, E pDataElement) {
        mProperties.get(pEnum).mProperty.bind(pDataElement, pProperty);
    }

    private IliteCodexReceiver() {
        mCommsReceiver = MessageProtocols.createReceiver(MessageProtocols.EProtocol.UDP, 7778, null);
        mThreadPool = Executors.newFixedThreadPool(1);

        mProperties.put(EDriveData.class, new ReciverPropertyMap<>(EDriveData.class, mCommsReceiver, mThreadPool));
        mProperties.put(ELogitech310.class, new ReciverPropertyMap<>(ELogitech310.class, mCommsReceiver, mThreadPool));
        mProperties.put(ETargetingData.class, new ReciverPropertyMap<>(ETargetingData.class, mCommsReceiver, mThreadPool));
    }

    private class ReciverPropertyMap<E extends Enum<E> & CodexOf<Double>> {

        final CodexReceiver<Double, E> mReceiver;
        final CodexPropertyDouble<E> mProperty;

        private ReciverPropertyMap(Class<E> pEnum, IReceiveProtocol pProtocol, Executor pThreadPool) {
            mReceiver = new CodexReceiver<>(pEnum,pProtocol);
            mProperty = new CodexPropertyDouble<>(pEnum, pThreadPool);
        }
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
