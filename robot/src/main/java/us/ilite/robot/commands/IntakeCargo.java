package us.ilite.robot.commands;

import us.ilite.robot.modules.CargoSpit;

public class IntakeCargo extends FunctionalCommand {

    public IntakeCargo(CargoSpit pCargoSpit) {
        super(pCargoSpit::setIntaking, pCargoSpit::hasCargo);
    }

}
