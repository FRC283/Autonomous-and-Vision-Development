package org.usfirst.frc.team283.robot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;

/**
 * TODO: If pass-by-reference has issues, then let's pass the joystick port and construct another joystick on that port (if WPI allows this)
 * @author 
 */
public class PhantomJoystick
{
	public static final int SAMPLING_INTERVAL = 3;
	private static final int MAX_TIME = 15000;
	
	public ArrayList<Double>[] analog = new ArrayList[10];
	public ArrayList<Boolean>[] digital = new ArrayList[5];
	
	Timer timeStamper;
	Joystick realJoystick;                 //The physical joystick being monitored
	DriveSubsystem driveTrain;
	File recordData;
	FileWriter fw;
	BufferedWriter bw;
	FileReader fr;
	BufferedReader br;
	Gson gson = new Gson();
	
	boolean isRecording = false;
	boolean startButtonBuffer = false;
	
	/**
	 * 
	 * @param js
	 * @param classPath
	 * @throws IOException
	 */
	public PhantomJoystick(Joystick js, String filePath) throws IOException
	{
		this.realJoystick = js; 				//Creating a joystick to record values off of
		recordData = new File(filePath); 		//Data being saved to the RoboRio
		driveTrain = new DriveSubsystem();
		fw = new FileWriter(recordData);		//Writer to save data
		bw = new BufferedWriter(fw);			
		fr = new FileReader(recordData);		//Reader to read data
		br = new BufferedReader(fr);
		timeStamper = new Timer();
		for (ArrayList<Double> j : analog)      //Initialize analog array
		{
			j = new ArrayList<Double>(0);
		}
		for (ArrayList<Boolean> h : digital)    //Initialize analog array
		{
			h = new ArrayList<Boolean>(0);
		}
		gson = new Gson();
	}
	
	/**
	 * Begins recording motions for the preset. Actually begins when the given value goes from true -> false
	 * @param startButtonState
	 */
	public void recordInit(boolean startButtonState)
	{
		if (startButtonState == false && this.startButtonBuffer == true)	//Test for button release
		{
			this.isRecording = true; 										//Allow recording
			timeStamper.reset();
			timeStamper.start();											//Start timer
		}
		this.startButtonBuffer = startButtonState; 							//Update the previous state
	}
	
	/**
	 * Must be repeatedly called in order to record values
	 * @throws IOException
	 */
	public boolean recordPeriodic() throws IOException
	{
		if (this.isRecording == true) 											//If button is pushed and released
		{
			int intervalNum = 0;												//Number of intervals passed
			//Below: 1000
			if((1000 / SAMPLING_INTERVAL) * intervalNum <= timeStamper.get())   //If the end of an interval
			{
				for (int i = 0; i < digital.length; i++)					    //For each possible digital input
				{
					digital[i].add(realJoystick.getRawButton(i));			    //Add values to ArrayList to execute for the current interval
				}
				for (int i = 0; i < analog.length; i++)						    //For each possible analog input
				{
					analog[i].add(realJoystick.getRawAxis(i));				    //Add values to ArrayList to execute for the current interval
				}
				intervalNum++;												    //Interval increment
				return true;                                                    //
			}
			if (timeStamper.get() >= MAX_TIME)								    //If we reach the recording period's limit
			{
				this.isRecording = false;									    //Stop recording
				bw.write(digital.toString() + "/n");						    //Save digital data first line
				bw.write(analog.toString() +"/n");							    //Save analog data second line
				return false;                                                   //                                              
			}
			return true;
		}
		return false;
	}
	
	/*
	 * NOTE: for substring(0, 1) of 'hello'  the 0 points before the 'h' and the 1 points after the h not after the 'e'
	 */
	/**
	 * 
	 * @param filePath
	 * @param fileLength
	 * @return
	 * @throws IOException
	 */
	public void readAnalogData(String filePath) throws IOException	//Read data function
	{
		//Insert example file here:
		//
		String line = br.readLine();													//Gather first line
		ArrayList<Double>[] data = new ArrayList[analog.length];							//Array to return
		int index = 0;																	//Holds current index of the array of ArrayLists that values should be stored to
		for (int i = 0; i > line.length(); i++)											//All characters in the line
		{
			String currentNum = line.substring(i, i+1);;								//Used to hold a String containing a double
			//J is the index of the end character of a number
			int j = i;																	//J is the index of the last digit in a double
			while (!currentNum.contains(",") || !currentNum.contains("]") || !currentNum.contains("}")) //Test for the end of a number(double)
			{
				currentNum = line.substring(i, j);										//Cut out a number
				j++;																	//If not the end of a number select more of the line
			}
			if (line.substring(j, j+1).equals("}"))													//End of an ArrayList so increase index
			{
				index++;																//Increase index
				j = j + 2;																//Skip parsing the comma and the ArrayList start("{");
			}
			j--;
			currentNum = line.substring(i, j);
				data[index].add(Double.parseDouble(currentNum));						//Add parsed String double to the correct index
			i = i + j;																//Start at the end of the parsed double
		}
		analog = data;																//return data; SET your internal array																//Return the ArrayList
	}
	
	public void readDigitalData(String filePath) throws IOException
	{
		String line = br.readLine();
		ArrayList<Boolean>[] data = new ArrayList[analog.length];
		int index = 0;																	//Holds current index of the array of ArrayLists that values should be stored to
		for (int i = 0; i > line.length(); i++)											//All characters in the line
		{
			String currentBol = line.substring(i, i+1);;								//Used to hold a String containing a double
			//J is the index of the end character of a number
			int j = i;
			while (!currentBol.contains(",") || !currentBol.contains("]") || !currentBol.contains("}")) //Test for the end of a number(double)
			{
				currentBol = line.substring(i, j);											//Cut out a number
				j++;																			//Length to continue after the letter to point to a whole double//If not the end of a number select more of the line
			}
			if (line.substring(j, j+1).equals("}"))										//End of an ArrayList so increase index
			{
				index++;																//Increase index																//Skip parsing the comma and the ArrayList start("{");
			}
			currentBol = line.substring(i, j);
			data[index].add(Boolean.parseBoolean(currentBol));						//Add parsed String double to the correct index
			i = i + j;																//Start at the end of the parsed double
		}
		digital = data;																//return data; SET your internal array																//Return the ArrayList
	
	}
	
	public void getVirtualDigitalInput(int interval, int buttonIndex)					//Retrieve correct Array value
	{
		digital[buttonIndex].get(interval);
	}
	public void getVirtualAnalogInput(int interval, int axisIndex)						//Retrieve correct Array value
	{
		analog[axisIndex].get(interval);
	}
	
	/* Use use Array List of time values for different subsystem magnitudes by matching controller
	 button ports values to the indexes of the digital and analog arrays
	 */
	public void playback()//could also be done in Robot Autonomous using getVirtual functions
	{
		Timer autoTime = new Timer(); 														//Start auto time
		int intervalNum = 0;																//Use intervalNum to access the values of buttons for a specific autonomous period 
		if((1000/PhantomJoystick.SAMPLING_INTERVAL)*intervalNum <= autoTime.get()) 			//Change subsystem values at each interval end
		{
			//Insert Subsystems that read the values stored in the Class's Arrays
			driveTrain.drive(analog[Constants.LEFT_Y].get(intervalNum), analog[Constants.RIGHT_X].get(intervalNum), digital[Constants.RIGHT_BUMPER].get(intervalNum));
		}	
	}
}
