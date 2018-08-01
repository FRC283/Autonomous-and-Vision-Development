package frc283.phantom;

import java.util.HashMap;
import java.util.Scanner;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableValue;
import edu.wpi.first.networktables.TableEntryListener;

/**
 * Allows us to control the PhantomJoystick class remotely while the robot is in motion
 * Creates a sort of pseudo-terminal which takes pseudo-java function commands
 * 
 * This class runs on two ends. The static main function runs on the laptop end,
 * and the capturing end triggers various PhantomJoystick functions
 */
public class RemoteConsole implements TableEntryListener
{
	
	/** 
	 * Function key code strings 
	 * Type the code and then space and then the argument for the function
	 * type "overview all" to print all routes 
	 */
	public static final String saveCode = "save";
	public static final String deleteCode = "delete";
	public static final String copyCode = "copy";
	public static final String setRouteCode = "setRoute";
	public static final String startRecordCode = "startRecord";
	public static final String stopRecordCode = "stopRecord";
	public static final String overviewCode = "overview";
	public static final String stopCode = "stop";
	
	/**
	 * Array of function key code values for easy iteration (Seems silly, is useful)
	 * Omits stop for propery functionality.
	 * */
	public static final String[] phantomFunctions = 
	{
		saveCode,
		deleteCode,
		copyCode,
		setRouteCode,
		startRecordCode,
		stopRecordCode,
		overviewCode
		//Do not have stopCode here
	};
	
	/** Printed to indicate that the console is ready to receive a command */
	private static final String openLineIndicator = "> ";
	
	/** Name of the networktable used to transfer data to the robot */
	public static final String tableName = "phantom_joystick";
	
	/** Key where the function being called is stored */
	public static final String functionKey = "functionCode";
	
	/** Key for the arguments of the called function */
	public static final String argsKey = "args";
	
	/** Key where the returned string and data can be found */
	public static final String returnKey = "return";
	
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
		nTable.addEntryListener(returnKey, new RemoteConsole(), kUpdate);
		
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
					nTable.getEntry(functionKey).setString(functionWord);
					nTable.getEntry(argsKey).setString(argWords);
				}
			}
		}
		while(functionWord != stopCode);
		
		//Indicate killed console
		System.out.println("Phantom Joystick RemoteConsole Closed");
	}

	/**
	 * Listens for changes to the "return" key
	 */
	@Override
	public void valueChanged(NetworkTable table, String key, NetworkTableEntry entry, NetworkTableValue value, int flags) 
	{
		//Print the changed value
		//Value only changes when it becomes something new
		System.out.println(value.getString());
	}
}