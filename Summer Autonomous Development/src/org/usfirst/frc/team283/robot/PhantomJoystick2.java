package org.usfirst.frc.team283.robot;

import edu.wpi.first.wpilibj.Joystick;

/**
 * Second version, using the PhantomRoute wrapper class, as well as gson data encoding
 *
 */
public class PhantomJoystick2 
{
	//The real joystick that this PhantomJoystick
	Joystick realJoystick;
	
	public PhantomJoystick2(Joystick realJoystick)
	{
		this.realJoystick = realJoystick;
	}
}
