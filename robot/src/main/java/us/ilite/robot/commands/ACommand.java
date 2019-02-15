package us.ilite.robot.commands;

public abstract class ACommand {

	public abstract void init(double pNow);
	public abstract boolean update(double pNow);
	public abstract void shutdown(double pNow);

}
