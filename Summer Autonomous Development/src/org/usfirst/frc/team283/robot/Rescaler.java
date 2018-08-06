package org.usfirst.frc.team283.robot;

/*
 * This class was created to shorten some math in our programs to create smoother deadzones everywhere
 * For 90% of cases, we'll just do rescale([deadzone e.g. (0.1)], 1, 0, 1, speed)
 * Where speed is, for example, the magnitude of your joystick and deadzone is your deadzone(e.g. 0.1)
 */
public class Rescaler
{
	
	public static double rescale(double lowero, double uppero, double lowern, double uppern, double value)
	{
	
		boolean neg = false;
		double rescaledValue = 0;	//Rescaled Value = number to be returned
		if (value < 0)
		{
				neg = true;
				value *= -1;
		}
		double oldscale = uppero - lowero;
		double newscale = uppern - lowern;

		rescaledValue = value - lowero;
		rescaledValue /= oldscale;
		rescaledValue *= newscale;
		rescaledValue += lowern;

		if (rescaledValue < 0)
		{
			rescaledValue = 0;
		}
		if (neg == true)
		{
			rescaledValue *= -1;
		}
		return rescaledValue;
	}
}