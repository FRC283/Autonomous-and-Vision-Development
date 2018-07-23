package org.usfirst.frc.team283.robot;

import java.util.ArrayList;
import java.util.Date;

public class PhantomRoute 
{
	//The time, in milliseconds, between ArrayList values
	//Not functional. Here to help give context to the data
	int timeSpacing;
	
	//A number that can be used to see when this route was created
	long creationTime;
	
	//An array containing an ArrayList of doubles
	protected ArrayList<Double>[] analog;   
	
	//An array containing an ArrayList of booleans
	protected ArrayList<Boolean>[] digital;  
	
	public PhantomRoute(int timeSpacing)
	{
		//Match the timespacing
		this.timeSpacing = timeSpacing;
		
		this.creationTime = new Date().getTime();
		
		//There are 10 analog inputs on the robot
		this.analog = new ArrayList[10];
		
		//Initialize analog array
		for (ArrayList<Double> j : this.analog)     
		{
			j = new ArrayList<Double>(0);
		}
		
		//There are 10 analog inputs on the robot
		digital = new ArrayList[5];
		
		//Initialize analog array
		for (ArrayList<Boolean> h : digital)    
		{
			//Each array value is a boolean ArrayList
			h = new ArrayList<Boolean>(0);
		}
	}
	
	/**
	 * 
	 * @param index - The index of analog data timeline to be retrieved
	 * @return - An ArrayList of the history of all analog values on the requested port
	 */
	public ArrayList<Double> getAnalog(int index)
	{
		return analog[index];
	}
	
	/**
	 * 
	 * @param index - The index of digital data timeline to be retrieved
	 * @return - An ArrayList of the history of all digital values on the requested port
	 */
	public ArrayList<Boolean> getDigital(int index)
	{
		return digital[index];
	}
	
	/**
	 * 
	 * @param inputIndex - Index of the digital input to set (e.g. the joystick left bumper button might be 3, or something)
	 * @param recordingIndex - On that input's timeline, this is the index of the value you intend to change (e.g. the fourth recorded value is index=3)
	 * @param value - True or false, the value to record
	 */
	public void setDigital(int inputIndex, int recordingIndex, Boolean value)
	{
		digital[inputIndex].set(recordingIndex, value);
	}
	
	/**
	 * 
	 * @param inputIndex - Index of the analog input to set (e.g. the joystick left y axis button might be 3, or something)
	 * @param recordingIndex - On that input's timeline, this is the index of the value you intend to change (e.g. the fourth recorded value is index=3)
	 * @param value - A double, the value to record
	 */
	public void setAnalog(int inputIndex, int recordingIndex, Double value)
	{
		analog[inputIndex].set(recordingIndex, value);
	}
	
	public String getCreationDate()
	{
		Date d = new Date(this.creationTime);
		return (d.getMonth() + "-" + d.getDate() + "-" + d.getYear());
	}
}
