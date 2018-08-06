package org.usfirst.frc.team283.robot;

import org.usfirst.frc.team283.robot.Scheme.Schema;

import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class DriveSubsystem 
{
	private static final double DEADZONE = 0.1;
	//The following two constants act as aliases for bool values
	private static final boolean MECHANUM = false;
	private static final boolean TANK = true;
	/** Speed multiplier for slow speed */
	private static final double SLOWSPEED = 0.5;
	
	/** Controls which drive mode we're in. True is tank, false is mechanum */
	private boolean driveMode = TANK;
	
	Talon frontLeftController;
	Talon frontRightController;
	Talon backLeftController;
	Talon backRightController;
	
	DriveSubsystem()
	{
		frontLeftController = new Talon(Constants.FRONT_LEFT_PORT);
		frontRightController = new Talon(Constants.FRONT_RIGHT_PORT);
		backLeftController = new Talon(Constants.BACK_LEFT_PORT);
		backRightController = new Talon(Constants.BACK_RIGHT_PORT);
	}
	
	/** Called once per cycle to update information */
	void periodic()
	{
		SmartDashboard.putBoolean("Mechanum Mode Enabled?", !driveMode); //Since MECHANUM = false, !driveMode is true when in mechanum
	}
	
	@Schema(value = Scheme.LEFT_Y, desc = "drive / push in for slow")
	@Schema(Scheme.LEFT_X)
	@Schema(value = Scheme.RIGHT_Y, desc = "drive / push in for slow")
	@Schema(Scheme.RIGHT_X)
	/**
	 * Controls the drive of the robot
	 * @param leftYMagnitude - Main control for left
	 * @param rightYMagnitude - Main control for right
	 * @param leftXMagnitude - Used in mechanum drive
	 * @param rightXMagnitude - Used in mechanum drive
	 * @param slow - the state of the button assigned to control slow speed
	 */
	public void drive(double leftYMagnitude, double rightYMagnitude, double leftXMagnitude, double rightXMagnitude, boolean slow)
	{
		
		double lym = Rescaler.rescale(DEADZONE, 1.0, 0.0, 1.0, leftYMagnitude);
		double rym = Rescaler.rescale(DEADZONE, 1.0, 0.0, 1.0, rightYMagnitude) * -1;
		double lxm = Rescaler.rescale(DEADZONE, 1.0, 0.0, 1.0, leftXMagnitude) * -1;
		double rxm = Rescaler.rescale(DEADZONE, 1.0, 0.0, 1.0, rightXMagnitude);
		
		if (driveMode == TANK) //X Magnitudes can be ignored
		{
			//The front and back motors of both sides are set to the same values
			frontLeftController.set(lym  * (slow ? SLOWSPEED : 1));
			frontRightController.set(rym  * (slow ? SLOWSPEED : 1));
			backLeftController.set(lym  * (slow ? SLOWSPEED : 1));
			backRightController.set(rym * (slow ? SLOWSPEED : 1));
		}
		else if (driveMode == MECHANUM) //X magnitudes are used to control robot
		{
			//Will be coded later
			/* The layout:
			 * \\  //
			 *   []
			 * //  \\
			 */
			frontLeftController.set((lym + lxm)  * (slow ? SLOWSPEED : 1));
			frontRightController.set((rym - rxm) * (slow ? SLOWSPEED : 1));
			backLeftController.set((lym - lxm)  * (slow ? SLOWSPEED : 1));
			backRightController.set((rym + rxm)  * (slow ? SLOWSPEED : 1));
		}
	}
	
	@Schema(value = Scheme.BACK, desc = "tank mode")
	@Schema(value = Scheme.START, desc = "mechanum mode")
	/**
	 * This function controls the drive mode
	 * @param tankButton - The state of button to switch to tank mode
	 * @param mechButton - The state of button to switch to mechanum mode
	 */
	public void driveMode(boolean tankButton, boolean mechButton)
	{
		//tankMode = (tankButton && !mechButton);
		if (tankButton && !mechButton) //If only the one button is pressed
		{
			driveMode = TANK; //Switch to that mode
		}
		else if (!tankButton && mechButton) //If only this one button is pressed
		{
			driveMode = MECHANUM; //Switch to this mode (mechanum)
		}
		//If both are true, the state does not change
		//If both are false, the state does not change
		System.out.println("Drive Mode: " + (driveMode ? "Tank" : "Mechanum"));
	}
}
