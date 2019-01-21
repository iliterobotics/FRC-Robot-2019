package us.ilite.robot.modules.targetData;

import java.util.Optional;

import us.ilite.robot.modules.ITargetingData;

public final class NoOpTargetDataProvider implements ITargetDataProvider{

    @Override
    public Optional<ITargetingData> getTargetingData() {
        return Optional.empty();
    }

    public static ITargetDataProvider getDefault() { 
        return INSTANCE_HOLDER.noOpInstance;
    }

    private NoOpTargetDataProvider() {

    }

    private static class INSTANCE_HOLDER {
        private static ITargetDataProvider noOpInstance = new NoOpTargetDataProvider();
    }
}
