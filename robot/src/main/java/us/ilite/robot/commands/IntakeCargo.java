package us.ilite.robot.commands;

import us.ilite.robot.modules.CargoSpitSingle;

public class IntakeCargo extends FunctionalCommand {

    public IntakeCargo() {
        super( CargoSpitSingle.getInstance()::setIntaking, CargoSpitSingle.getInstance()::hasCargo);
    }

}
