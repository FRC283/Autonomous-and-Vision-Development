package org.usfirst.frc.team283.robot;

import org.usfirst.frc.team283.robot.Scheme.Schema;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class CannonSubsystem
{
	//The following two constants act as aliases for bool values
	private static final boolean OPEN = true;
	private static final boolean CLOSED = false;
	/** The maximum duration (in seconds) to fill the tank before auto shut-off */
	private static final double FILL_TIMEOUT = 10;
	/** The duration (in seconds) to allow the tank to vent */
	private static final double FIRE_TIMEOUT = 0.5;
	
	/** Used the track the state of the split function */
	private boolean fillBool = false;
	/** Used the track the state of the split function */
	private boolean fireBool = false;
	
	DigitalInput pressureSwitch;
	Solenoid fireSolenoid;
	Solenoid fillSolenoid;
	Timer fillTimer; //Used to stop filling for safety
	Timer fireTimer; //Used to allow air time to vent
	
	CannonSubsystem()
	{
		pressureSwitch = new DigitalInput(Constants.PRESSURE_SWITCH);
		fireSolenoid = new Solenoid(Constants.FIRE_PORT);
		fillSolenoid = new Solenoid(Constants.FILL_PORT);
		fillTimer = new Timer();
		fireTimer = new Timer();
	}
	
	/** Called once per cycle to update information */
	void periodic()
	{
		this.fillPeriodic();
		this.firePeriodic();
		SmartDashboard.putBoolean("At Pressure", pressureSwitch.get());
	}
	
	@Schema(Scheme.RIGHT_BUMPER)
	/**
	 * Begins filling if possible
	 */
	public void fillInit()
	{
		if (!fillBool)
		{
			fillBool = true;
			fillTimer.start();
			fillSolenoid.set(OPEN); //Begin filling
		}
		else
		{
			//Do nothing, we are still filling
		}
	}
	
	/**
	 * Periodically checked to see if filling is done
	 */
	public void fillPeriodic()
	{
		if (pressureSwitch.get() || fillTimer.get() > FILL_TIMEOUT) //If we have reached pressure or reached timeout
		{
			fillSolenoid.set(CLOSED); //Stop filling air
			fillTimer.stop(); //Restart timer
			fillTimer.reset();
			fillBool = false; //End checking for fill
			
			if (fillTimer.get() > FILL_TIMEOUT) //If we timed out, send out an alert about that
				System.out.println("FILLING TIMEOUT. CUTTING AIR. READY TO FIRE.");
		}
		else
		{
			//Do nothing, continue filling
		}
		System.out.println("Pressure Switch : " + pressureSwitch.get());
	}
	
	@Schema(Scheme.RIGHT_TRIGGER)
	@Schema(value = Scheme.X, desc = "override fire (ignore tank pressure)")
	/**
	 * Fires the cannon
	 * @param override - The state of the button used to override the pressure check
	 */
	public void fireInit(boolean override)
	{
		if (!fireBool)
		{
			if (pressureSwitch.get() || override) //If we're ready to fire, or we override
			{
				fireSolenoid.set(OPEN); //Fire
				System.out.println("YAR HAR HAR!");
				fireTimer.start(); //Start counting
				fireBool = true; //Cut out ability to fire
			}
		}
		else
		{
			//Do nothing if we're currently venting air
		}
	}
	
	/** Closes the canister after a set amount of time */
	public void firePeriodic()
	{
		if (fireBool)
		{
			if (fireTimer.get() > FIRE_TIMEOUT)
			{
				fireSolenoid.set(CLOSED); //Stop venting air
				fireTimer.stop(); //Reset the timer
				fireTimer.reset();
				fireBool = false; //Stop checking the timer
			}
		}
	}
}
