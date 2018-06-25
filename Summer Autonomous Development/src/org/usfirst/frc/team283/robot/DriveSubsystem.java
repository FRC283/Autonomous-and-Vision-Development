package org.usfirst.frc.team283.robot;


import org.usfirst.frc.team283.robot.Utilities283.Schema;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class DriveSubsystem
{
	//Constants
	private static final double DEADZONE = 0.1;
	private static final double SLOWSPEED = 0.5;
	private static final double P_CONSTANT = 1/100;
	private static final double I_CONSTANT = 0;
	
	/** If the abs value of the difference of turning values is greater than this, we switch to turning gear*/
	private static final double TURNING_THRESHOLD = 0.75;
	
	/** PI Control continues until the error is below this */
	private static final double MAX_ALLOWABLE_ERROR = 1;
	
	
	//Signage Chart
	/*
	 * 					     | True 	   | False	   |
	 * -------------------------------------------------------
	 * Gear Shift Solenoid   |  ?   	   |  ?   	   |
	 * -------------------------------------------------------
	 * 
	 * 
	 * 
	 * 					     |  +1  	   |  -1  	   |
	 * -------------------------------------------------------
	 * Left Drive Controller |   ?  	   |   ?  	   |
	 * Right Drive Controller|   ?  	   |   ?  	   |
	 * Left Drive Encoder    |   Forward   |  Backward |
	 * Right Drive Encoder   |   Backward  |  Forward  |
	 * 
	 */
	
	//Variables
	/** Previous value of the button that controllers this gearshift solenoid */
	boolean lastGearShiftState = false;
	/** Number of inches */
	double leftDriveTarget = 0;
	/** Number of inches */
	double rightDriveTarget = 0;
	/** True while PI control is active */
	boolean leftCurrentlyControlling;
	/** True while PI control is active */
	boolean rightCurrentlyControlling;
	/** accumulation of error on left side */
	double aggrLeftError = 0;
	/** accumulation of error on right side */
	double aggrRightError = 0;
	
	//Actuators\Sensors
	Spark leftController;
	Spark rightController;
	Encoder leftEnc;
	Encoder rightEnc;
	Solenoid gearShiftSol;
	
	public DriveSubsystem()
	{
		leftController = new Spark(Constants.LEFT_DRIVE_CONTROLLER_PORT);
		rightController = new Spark(Constants.RIGHT_DRIVE_CONTROLER_PORT);
		leftEnc = new Encoder(Constants.LEFT_DRIVE_ENCODER_PORT_A, Constants.LEFT_DRIVE_ENCODER_PORT_B);
		rightEnc = new Encoder(Constants.RIGHT_DRIVE_ENCODER_PORT_A, Constants.RIGHT_DRIVE_ENCODER_PORT_B);
		gearShiftSol = new Solenoid(Constants.DRIVE_GEARSHIFT_PORT);
	}
	
	/**
	 * Controls the drive speed of both sides of the robot
	 * @param leftMagnitude - Left drive magnitude
	 * @param rightMagnitude - Right drive magnitude
	 * @param slowSpeed - If true, cut speed by factor of 0.5ish (see code for value)
	 */
	@Schema(Utilities283.LOGITECH_LEFT_Y)
	@Schema(Utilities283.LOGITECH_RIGHT_Y)
	@Schema(value = Utilities283.LOGITECH_RIGHT_BUMPER, desc = SLOWSPEED + " speed")
	public void drive(double leftMagnitude, double rightMagnitude, boolean slowSpeed)
	{
		//Below: if we are turning (difference between drive side magnitudes is above a certain value) then switch to turning gear
		if (Math.abs(leftMagnitude - rightMagnitude) > TURNING_THRESHOLD)
		{
			gearShiftSol.set(false); //Gearing that allows turning
		}
		leftController.set(-1 * (Utilities283.rescale(DEADZONE, 1.0, 0.0, 1.0, leftMagnitude)) * (slowSpeed ? SLOWSPEED : 1));
		rightController.set((Utilities283.rescale(DEADZONE, 1.0, 0.0, 1.0, rightMagnitude)) * (slowSpeed ? SLOWSPEED : 1));
		//SmartDashboard.putNumber("Left Magnitude", leftMagnitude);
		//SmartDashboard.putNumber("Right Magnitude", rightMagnitude);
	}
	
	/**
	 * Shifts the drive gear
	 * @param gearShiftValue - state of the button to execute shifting gears
	 *
	 */
	@Schema(Utilities283.LOGITECH_LEFT_BUMPER)
	public void shiftGear(boolean gearShiftValue)
	{
		if (lastGearShiftState == false && gearShiftValue == true)
		{
			gearShiftSol.set(!gearShiftSol.get());
		}
		lastGearShiftState = gearShiftValue;
	}
	
	
	/**
	 * Drives the right drivetrain the specified number of inches
	 * You must periodically call 'driveDistancePeriodic()'
	 * @param inches - # of inches
	 */
	public void leftDriveDistanceInit(double inches)
	{
		leftDriveTarget = inches;
		leftCurrentlyControlling = true;
	}
	
	/**
	 * Drives the left drivetrain the specified number of inches
	 * You must periodically call 'driveDistancePeriodic()'
	 * @param inches - # of inches
	 */
	public void rightDriveDistanceInit(double inches)
	{
		rightDriveTarget = inches;
		rightCurrentlyControlling = true;
	}
	
	/**
	 * Continuously updates both of the drive motors for target distance 
	 */
	@Deprecated
	public void driveDistancePeriodic()
	{
		double leftError = leftEnc.get() - leftDriveTarget;
		double rightError = rightEnc.get() - rightDriveTarget;
		
		//--LEFT SIDE--
		if (leftCurrentlyControlling == true)
		{
			if (Math.abs(leftError) < MAX_ALLOWABLE_ERROR) //If within allowable error
			{
				leftCurrentlyControlling = false; //Stop controlling
				leftController.set(0);
			}
			else
			{
				leftController.set(-P_CONSTANT * leftError + -I_CONSTANT * aggrLeftError);
				aggrLeftError += leftError;
			}
		}
		//Nothing happens if this is false
		
		//--RIGHT SIDE--
		if (rightCurrentlyControlling == true)
		{
			if (Math.abs(rightError) < MAX_ALLOWABLE_ERROR) //If within allowable error
			{
				rightCurrentlyControlling = false; //Stop controlling
				rightController.set(0);
			}
			else
			{
				rightController.set(-P_CONSTANT * rightError + -I_CONSTANT * aggrRightError);
				aggrRightError += rightError;
			}
		}
		//Nothing happens if this is false
	}
	
	public boolean periodic()
	{
		driveDistancePeriodic();
		SmartDashboard.putNumber("Left Encoder Value", leftEnc.getDistance());
		SmartDashboard.putNumber("Right Encoder Value", rightEnc.getDistance());
		SmartDashboard.getBoolean("High Speed", gearShiftSol.get());
		if(rightCurrentlyControlling == false && leftCurrentlyControlling == false)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
}
