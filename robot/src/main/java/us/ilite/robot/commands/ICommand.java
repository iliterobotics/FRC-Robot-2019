package us.ilite.robot.commands;

public interface ICommand {
	void init(double pNow);
	boolean update(double pNow);
	void shutdown(double pNow);
}
