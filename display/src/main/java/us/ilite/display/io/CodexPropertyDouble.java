package us.ilite.display.io;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;
import com.flybotix.hfr.util.lang.EnumUtils;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * @author JesseK
 *
 *  A class which adds thread-safe listeners to a Codex.  This class extends Codex of doubles.
 *
 * @param <E>
 */
public class CodexPropertyDouble <E extends Enum<E> & CodexOf<Double>> extends Codex<Double,E> {

    private final DoubleProperty[] mProperties;
    private final SimpleBooleanProperty[] mIsSetProperties;
    private final Map<E, List<ChangeListener<? super Number>>> mChangeListeners = new HashMap<>();

    public CodexPropertyDouble(Class<E> pEnum) {
        super(pEnum);
        mProperties = new DoubleProperty[EnumUtils.getLength(pEnum)];
        mIsSetProperties = new SimpleBooleanProperty[EnumUtils.getLength(pEnum)];
        for(E e : EnumUtils.getEnums(pEnum)) {
            mChangeListeners.put(e, new ArrayList<>());
        }
    }

    public void set(E pData, Double pValue) {
        super.set(pData, pValue);
        Platform.runLater(() -> {
            mProperties[pData.ordinal()].set(pValue);
            mIsSetProperties[pData.ordinal()].set(isSet(pData));
            for(ChangeListener<? super Number> listener : mChangeListeners.get(pData)) {
                listener.changed(null, null, pValue);
            }
        });
    }

    public void set(int pOrdinal, Double pValue) {
        Platform.runLater(() -> {
            mProperties[pOrdinal].set(pValue);
            mIsSetProperties[pOrdinal].set(isSet(pOrdinal));
            E e = EnumUtils.getEnums(mMeta.getEnum(), true).get(pOrdinal);
            for(ChangeListener<? super Number> listener : mChangeListeners.get(e)) {
                listener.changed(null, null, pValue);
            }
        });
    }

    public void bindTo(E pData, DoubleProperty pObservable) {
        pObservable.bind(mProperties[pData.ordinal()]);
    }

    public DoubleProperty getProperty(E pData) {
        return mProperties[pData.ordinal()];
    }

    public void bindBiDirectional(E pData, DoubleProperty pProperty) {
        mProperties[pData.ordinal()].bindBidirectional(pProperty);
    }

    public void addListener(E pData, ChangeListener<? super Number> pListener) {
        mProperties[pData.ordinal()].addListener(pListener);
        mChangeListeners.get(pData).add(pListener);
    }

    public void bindToSetProperty(E pData, ObservableValue<Boolean> pObservable) {
        mIsSetProperties[pData.ordinal()].bind(pObservable);
    }

    public void bindBiDirectionalToSetProperty(E pData, Property<Boolean> pProperty) {
        mIsSetProperties[pData.ordinal()].bindBidirectional(pProperty);
    }

    public void addListenerToSetProperty(E pData, ChangeListener<Boolean> pListener) {
        mIsSetProperties[pData.ordinal()].addListener(pListener);
    }
}
