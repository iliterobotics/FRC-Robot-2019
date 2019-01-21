package us.ilite.robot.modules;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Function;

import com.team254.lib.geometry.Translation2d;

import org.junit.Before;
import org.junit.Test;

import us.ilite.common.config.SystemSettings.VisionTarget;

public class LimelightTest {
    Limelight limelight;

    @Before
    public void init() {
        limelight = new Limelight();
    }

    @Test
    public void testMe() { 
        Function<VisionTarget,Double> distanceCalculator = mock(Function.class);
        when(distanceCalculator.apply(any(VisionTarget.class))).thenReturn(-5d);
        Function<Void,Double> approachAngleCalculator = mock(Function.class);
        Optional<Translation2d> calcDist = limelight.calcTargetLocation(VisionTarget.CargoPort, distanceCalculator, approachAngleCalculator);

        assertNotNull(calcDist);
        assertTrue(calcDist.isEmpty());
    }
}