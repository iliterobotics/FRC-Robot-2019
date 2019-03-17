package us.ilite.framework;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;

public abstract class HardwareCodex<T, V extends IHardwareValue<T>, E extends Enum<E> & CodexOf<V>> {

    private final Codex<V, E> mCodex;

//    private final Map<E, CANSparkMax> mSparkMaxMap = new HashMap<>();
//    private final Map<E, TalonSRX> mTalonSrxMap = new HashMap<>();
//    private final Map<E, VictorSPX> mVictorSpxMap = new HashMap<>();
//
//    private final Map<E, DigitalInput> mDigitalInputMap = new HashMap<>();
//    private final Map<E, DigitalOutput> mDigitalOutputMap = new HashMap<>();
//    private final Map<E, Relay> mRelayMap = new HashMap<>();
//    private final Map<E, Solenoid> mSolenoidMap = new HashMap<>();
//    private final Map<E, DoubleSolenoid> mDoubleSolenoid = new HashMap<>();
//    private final Map<E, AnalogInput> mAnalogInput = new HashMap<>();
//    private final Map<E, AnalogOutput> mAnalogOutput = new HashMap<>();

    public HardwareCodex(Codex<V, E> pCodex) {
        mCodex = pCodex;
    }

    /**
     * Override this to bind Codex values to setters and getters
     */
    public abstract void configureHardwareMappings();

    public void updateHardwareMappings() {
        for(E key : mCodex.meta().getEnum().getEnumConstants()) {
            mCodex.get(key).update();
        }
    }

    public String getEnumNameForClass(Class<E> pEnum) {
        String enumClassName = pEnum.getSimpleName();
        return enumClassName.replaceAll("/\\B([A-Z])/'", "_$1");
    }



}
