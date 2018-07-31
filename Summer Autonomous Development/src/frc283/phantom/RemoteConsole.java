package frc283.phantom;

import java.util.HashMap;
import java.util.Scanner;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * Allows us to control the PhantomJoystick class remotely while the robot is in motion
 * Creates a sort of pseudo-terminal which takes pseudo-java function commands
 * 
 * This class runs on two ends. The static main function runs on the laptop end,
 * and the capturing end triggers various PhantomJoystick functions
 */
public abstract class RemoteConsole 
{
	/** Function key strings 
	 * There is one command not here. "stop" is embedded into the interpretation code
	 * */
	public static final String[] phantomFunctions = 
	{
		"save",
		"delete",
		"copy",
		"setRoute",
		"startRecord",
		"stopRecord",
		"overview"
	};
	
	/** Printed to indicate that the console is ready to receive a command */
	private static final String openLineIndicator = "> ";
	
	/** Name of the networktable used to transfer data to the robot */
	private static final String tableName = "phantom_joystick";
	
	/** Used to transfer all data over to the robot */
	private static NetworkTable nTable;
	
	/** Keyboard input */
	private static Scanner keyboard;
	
	public static void main(String... args)
	{
		RemoteConsole.open();
	}
	
	public static void open()
	{
		//Opening message to prompt input
		System.out.println("Phantom Joystick RemoteConsole Open");
		
		//Initialize keyboard
		keyboard = new Scanner(System.in);
		
		NetworkTableInstance nTableInst = NetworkTableInstance.getDefault();
		nTable = nTableInst.getTable(tableName);
		nTable.getEntry("function_word").setDefaultString("");
		nTable.getEntry("arg_word").setDefaultString("");
		
		String trimmedInput; //Input without start and end space
		String functionWord; //Word that indicates desired command
		String argWords;     //All args that follow are args
		do
		{
			//Prints a prompt for the next command
			System.out.print(openLineIndicator);
			
			//Remove leading and trailing spacing
			trimmedInput = keyboard.nextLine().trim();
			
			//Find the first space after a function keyword.
			int spaceIndex = trimmedInput.indexOf(" ");
			
			//IndexOf returns -1 if not found. Means it's a 1-word function like "stop"
			if (spaceIndex != -1)
			{
				//Grab everything from the start to the first space if it exists
				functionWord = trimmedInput.substring(0, spaceIndex);	
				
				//argWords are all the words that come after the main functionWord
				argWords = trimmedInput.substring(spaceIndex, trimmedInput.length());
			}
			else
			{
				//One one functions will just be the trimmed input
				functionWord = trimmedInput;
				
				//If it's a one-word function, no args
				argWords = "";
			}			
			
			//Trim out any excess middle space
			argWords = argWords.trim();
			
			//Go through each possible value of the enum
			for (String s : phantomFunctions)
			{
				//If the functionWord matches one the commands
				if (functionWord == s)
				{
					System.out.println("Detected " + s);
					nTable.getEntry("function_word").setString(functionWord);
					nTable.getEntry("arg_word").setString(argWords);
				}
			}
		}
		while(functionWord != "stop");
		
		//Indicate killed console
		System.out.println("Phantom Joystick RemoteConsole Closed");
	}
}