package org.usfirst.frc.team283.robot;


import org.usfirst.frc.team283.robot.Scheme.Schema;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Talon;

public class ElevatorSubsystem 
{
	/* 
	 * - Guide to Polarity -
	 * Pressing Bumper -> Moving "up" -> Approaching front switch -> Positive motor direction?
	 * Pressing Trigger -> Moving "down" -> Approaching back switch -> Negative motor direction?
	 */
	
	/** Speed for transversal of worm screw, also controls polarity */
	private static final double SPEED = 1;
	
	private DigitalInput frontLimit;
	private DigitalInput backLimit;
	private Talon controller;
	
	ElevatorSubsystem()
	{
		frontLimit = new DigitalInput(Constants.FRONT_SWITCH);
		backLimit = new DigitalInput(Constants.BACK_SWITCH);
		controller = new Talon(Constants.ELEVATOR_PORT);
	}
	
	@Schema(value = Scheme.LEFT_TRIGGER, desc = "elevator down")
	@Schema(value = Scheme.LEFT_BUMPER, desc = "elevator up")
	/**
	 * Controls the up and down motion of the elevator
	 * @param up - The state of the button assigned to signal up
	 * @param down - The state of the button assigned to signal down
	 */
	public void raiseLower(boolean up, boolean down)
	{
		System.out.println("Status of Forward limit: " + frontLimit.get());
		System.out.println("Status of Backward limit: " + backLimit.get());
		
		if (up) //If attempting to go up
		{
			if (down) //But down is held
			{
				controller.set(0); //Stop motor
			}
			else //And not attempting to go down
			{
				if (frontLimit.get()) //If hitting forward limit 
				{
					controller.set(0); //stop
				}
				else //If not hitting front limit
				{
					controller.set(SPEED); //Go up
				}
			}
		}
		else //If not attempting to go up
		{
			if (down) //And down is held
			{
				if (backLimit.get()) //If hitting back limit 
				{
					controller.set(0); //stop
				}
				else //If not hitting back limit
				{
					controller.set(-1 * SPEED); //Go down
				}
			}
			else //And not attemping to go down
			{
				controller.set(0); //Stop motor (do nothing, default state)
			}
		}
	}
}
