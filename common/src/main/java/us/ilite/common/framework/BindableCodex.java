package us.ilite.common.framework;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexMetadata;
import com.flybotix.hfr.codex.CodexOf;

import java.util.ArrayList;
import java.util.List;

/**
 * A Codex whose values are bound to the functions in a certain class.
 * @param <C> The type of class.
 * @param <V> The data type the Codex holds.
 * @param <E> The enumeration type defining the Codex keys.
 */
public class BindableCodex<C, V, E extends Enum<E> & CodexOf<V>> extends Codex<V, E> {

    protected Class<C> mTargetClass;
    protected List<GetterSetter<V>> mFunctions;

    public interface Getter<T> {
        T get();
    }

    public interface Setter<T> {
        void set(T pValue);
    }

    /**
     * Defines an interface for a value that can be retreived and set.
     * @param <T> The type of the parameter for the setter and return type for the getter.
     */
    public class GetterSetter<T> implements Getter<T>, Setter<T> {

        private final Getter<T> mGetter;
        private final Setter<T> mSetter;

        public GetterSetter(Getter<T> pGetter, Setter<T> pSetter) {
            mGetter = pGetter;
            mSetter = pSetter;
        }

        @Override
        public T get() {
            return mGetter.get();
        }

        @Override
        public void set(T pValue) {
            mSetter.set(pValue);
        }

        public Getter<T> getGetter() {
            return mGetter;
        }

        public Setter<T> getSetter() {
            return mSetter;
        }

    }

    public BindableCodex(V pDefaultValue, CodexMetadata<E> pMeta, Class<C> pTargetClass) {
        super(pDefaultValue, pMeta);
        mTargetClass = pTargetClass;
        mFunctions = new ArrayList<>();

    }

    public BindableCodex(V pDefaultValue, Class<E> pEnum, Class<C> pTargetClass) {
        this(pDefaultValue, CodexMetadata.empty(pEnum), pTargetClass);
    }

    public BindableCodex(Class<E> pEnum, Class<C> pTargetClass) {
        this(null, pEnum, pTargetClass);
    }

    public void bind(E pEnum, Getter<V> pGetter, Setter<V> pSetter) {
        mFunctions.set(pEnum.ordinal(), new GetterSetter<>(pGetter, pSetter));
    }

    public void updateBoundValues() {
        for(E key : mMeta.getEnum().getEnumConstants()) {
            // Update codex value with getter
            set(key, getFunction(key).get());
            
        }
    }

    public GetterSetter<V> getFunction(E pKey) {
        return mFunctions.get(pKey.ordinal());
    }

}
