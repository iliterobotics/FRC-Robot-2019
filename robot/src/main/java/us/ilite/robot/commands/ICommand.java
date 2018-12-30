package us.ilite.robot.commands;

/**
 * A simple command that can be initialized, updated, and shutdown.
 */
public interface ICommand {

	/**
	 * Initialize is run once, before the command starts being updated. Any code recording
	 * initial states or sensor values should go here.
	 * @param pNow The current time.
	 */
	void init(double pNow);

	/**
	 * Update is called periodically until it indicates the command is completed by returning true.
	 * Setting any outputs (drivetrain, etc.) and getting any new inputs (gyro, encoders, etc.) should go here.
	 * @param pNow The current time.
	 * @return Whether the command is finished updating.
	 */
	boolean update(double pNow);

	/**
	 * Shutdown is called once update returns true, indicating the command is completed. Code stopping movement
	 * commanded in update and any other cleanup tasks should be put here.
	 * @param pNow
	 */
	void shutdown(double pNow);
}
