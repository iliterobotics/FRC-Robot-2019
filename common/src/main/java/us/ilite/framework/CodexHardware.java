package us.ilite.framework;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;

public class CodexHardware<V, E extends Enum<E> & CodexOf<V>> {

    private final Codex<V, E> mCodex;

    private final HashMap<E, CANSparkMax>

    public CodexHardware(Codex<V, E> pCodex) {
        mCodex = pCodex;
    }



}
