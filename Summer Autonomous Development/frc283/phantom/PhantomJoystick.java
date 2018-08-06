package frc283.phantom;

import java.io.File;
import java.util.HashMap;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableValue;
import edu.wpi.first.networktables.TableEntryListener;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;

/**
 * Second version, using the PhantomRoute wrapper class, as well as gson data encoding
 * 
 * This class is a jukebox. It contains all the discs, and can play the discs, as well as record new discs and record over old discs
 * When you create a PhantomJoystick, it will find all .route files stored on the system, not just the designated save folder
 * 
 * Note: nothing is static on this class because we trawl for Routes when constructing a joystick
 *
 * Terms:
 *     Active Route: The PhantomRoute currently being played and/or recorded over. "Route" (singular) in function names usually refers to this
 *     Stored Route: All PhantomRoutes that were found on the file system or just created this session. Includes the active route. "Routes" (plural) usually refers to these
 *     Playback: The process of playing back all the joystick data
 *     Recording: The process of actually recording the joystick data
 */
public class PhantomJoystick implements TableEntryListener
{
	public static void main (String... args)
	{
		Joystick j = new Joystick(0);
		PhantomJoystick pj = new PhantomJoystick(j);
	}
	
	//The folder where all NEW routes are saved. It's possible that some old routes fell outside this folder. Should not end with a slash
	public final static String routeFolder = "C:\\Users\\Benjamin\\Desktop\\routes";
	
	//The folder that is searched for all .route files. Should be as high up in the file system as possible
	//TODO: Find the best value for this
	public final static String rootSearchFolder = routeFolder;
	
	//True when playing back the data
	public boolean playback = false;
	
	//True when recording the data
	public boolean recording = false;
	
	//Used to mete out recording and playback
	private Timer timer;
	
	//The string name of the route currently being written/read to/from
	//Why is this a string and not a PhantomRoute? Because java passes objects strangely, so we dont want to make a bunch of dupes
	private String activeRoute;
	
	//Joystick where values are watched during recording
	private Joystick recordingJoystick;
	
	//Contains all PhantomRoutes found all the system
	private HashMap<String, PhantomRoute> storedRoutes;
	
	/** Used to receive commands from the laptop-side */
	private NetworkTable nTable;
	
	public PhantomJoystick(Joystick recordingJoystick)
	{
		storedRoutes = new HashMap<String, PhantomRoute>();
		
		timer = new Timer();
		
		this.recordingJoystick = recordingJoystick;
		
		NetworkTableInstance nTableInst = NetworkTableInstance.getDefault();
		nTable = nTableInst.getTable(RemoteConsole.tableName);
		nTable.getEntry(RemoteConsole.functionKey);
		
		//Register self as event listener for function calls over ther remote console
		nTable.addEntryListener(RemoteConsole.functionKey, this, EntryListenerFlags.kUpdate);
		
		//Create a directory representation, and start iterating through it for .route files
		createPhantomRoutes(new File(PhantomJoystick.rootSearchFolder).listFiles());
	}
	
	/**
	 * Searches a directory for .route files then makes PhantomRoute wrappers for them
	 * @param files
	 */
	private void createPhantomRoutes(File[] files)
	{
		for (File singleFile : files)
		{
			if (singleFile.isDirectory())
			{
				//If it's a directory, then make another call to this function to also iterate through THAT directory
				createPhantomRoutes(singleFile.listFiles());
			}
			else
			{
				//Position of the "." in the file name
				int dotIndex = singleFile.getName().lastIndexOf(".") + 1; 
				
				//Grab the "route" part of "file.route" (or any other other file extension, like "txt")
				String extension = singleFile.getName().substring(dotIndex, singleFile.getName().length());
				if (extension.equalsIgnoreCase("route"))
				{
					//Create a PhantomRoute for this .route file
					PhantomRoute newPhantomRoute = new PhantomRoute(singleFile.getAbsolutePath());
					
					//Push that route onto the storedRoutes
					storedRoutes.put(newPhantomRoute.getName(), newPhantomRoute);
				}
			}
		}
	}
	
	/**
	 * Event Listener for incoming commands on the table
	 */
	@Override
	public void valueChanged(NetworkTable table, String key, NetworkTableEntry entry, NetworkTableValue value, int flags) 
	{	
		//Entry for the function arguments
		NetworkTableEntry argsEntry = table.getEntry(RemoteConsole.argsKey);
		
		//Get the function code value;
		String functionStr = value.getString();
		
		//Get the args value, default is blank string if not found
		String argsStr = argsEntry.getString("");
		
		//String that will be returned to the RemoteConsole and printed there
		String returnString = "Specified function had no return value.";
		
		//Execute the proper function depending on the function code
		switch (functionStr)
		{
			case RemoteConsole.saveCode:
				saveRoutes();
				returnString = "Routes saved to drive.";
			break;
			case RemoteConsole.copyCode:
				copyRoute(argsStr);
				returnString = "Route " + argsStr + " copied successfully.";
			break;
			case RemoteConsole.setRouteCode:
				setActiveRoute(argsStr);
				returnString = "Active route is now " + argsStr + ".";
			break;
			case RemoteConsole.getRouteCode:
				returnString = "Active route is " + getActiveRouteName() + ".";
			break;
			case RemoteConsole.deleteCode:
				deleteRoute(argsStr);
				returnString = "Route " + argsStr + " has been deleted.";
			break;
			case RemoteConsole.startRecordCode:
				recordInit();
				returnString = "Now recording data for route " + activeRoute + ".";
			break;
			case RemoteConsole.stopRecordCode:
				recordTerminate();
				returnString = "Recording stopped.";
			break;
			case RemoteConsole.overviewCode:
				if (argsStr == "all")
				{
					returnString = getAllOverviews();
				}
				else
				{
					returnString = getRouteOverview(argsStr);
				}
			break;
		}
		//Return the returnString
		//Setting the entry to blank first ensures that the script fires off the response into the console
		nTable.getEntry(RemoteConsole.returnKey).setString("");
		nTable.getEntry(RemoteConsole.returnKey).setString(returnString);
	}
	
	/**
	 * Whenever the timer is rolling for playback or recording, this function helps translate that
	 * time value into a useful integer for accessing the arrays that describe the routes
	 * 
	 * First, converts the timer value into milliseconds
	 * Then divides that 1000 milliseconds into a number of steps based on the time spacing
	 * E.g. if the time spacing is 100, then 1 second will be divided into 10 saved values
	 * The typecast to int acts as truncation e.g. 127 milliseconds would become 1.17 which is cast to 1.00
	 *		
	 * @param timeValue - the number of seconds on the timer.
	 * @return - the time index for the current timer value
	 */
	private int getTimeIndex(double timeValue)
	{
		return (int)(timeValue * 1000 / storedRoutes.get(activeRoute).getTimeSpacing());
	}
	
	/**
	 * Save each PhantomRoute
	 */
	private void saveRoutes()
	{
		//Iterates through each PhantomRoute and saves it
		for (PhantomRoute pr : storedRoutes.values())
		{
			pr.save();
		}
	}
	
	/**
	 * Cannot be used during playback or recording
	 * @param routeName - name of the route to set to being active
	 */
	public void setActiveRoute(String routeName)
	{
		activeRoute = routeName;
	}
	
	/**
	 * @return - the name of the current active string
	 */
	public String getActiveRouteName()
	{
		return storedRoutes.get(activeRoute).getName();
	}
	
	/**
	 * @param number - the axis number to get the value for
	 * @return - the most appropriate value for the current time since playback started
	 */
	public double getRawAxis(int number)
	{
		if (playback == true)
		{
			return storedRoutes.get(activeRoute).getAnalog(number).get(getTimeIndex(timer.get()));
		}
		else
		{
			System.err.print("PhantomJoystick.getRawAxis: You must call playbackInit to initiate playback");
			return 0;
		}
	}
	
	/**
	 * @param number - the button number to get the value for
	 * @return - the most appropriate value for the current time since playback started
	 */
	public boolean getRawButton(int number)
	{
		if (playback == true)
		{
			return storedRoutes.get(activeRoute).getDigital(number).get(getTimeIndex(timer.get()));
		}
		else
		{
			System.err.print("PhantomJoystick.getRawAxis: You must call playbackInit to initiate playback");
			return false;
		}
	}
	
	/**
	 * Initiates recording. Values from the passed joystick will be watched
	 */
	public void recordInit()
	{
		if (playback == false)
		{
			timer.reset();
			timer.start();
			recording = true;
		}
	}
	
	/**
	 * Records joystick values at proper times. Must be called rapidly and periodically to function
	 */
	public void recordPeriodic()
	{
		if (recording == true)
		{
			//First, converts the timer value into milliseconds
			//Then divides that 1000 milliseconds into a number of steps based on the time spacing
			//E.g. if the time spacing is 100, then 1 second will be divided into 10 saved values
			//The typecast to int acts as truncation e.g. 127 milliseconds would become 1.17 which is cast to 1.00
			int timeIndex = (int)(timer.get() * 1000 / storedRoutes.get(activeRoute).getTimeSpacing());
			
			//For each possible analog input
			for (int a = 0; a < 10; a++)
			{
				//Get analog input #a, set its value at the timeIndex to be the current joystick axis value for axis #a
				storedRoutes.get(activeRoute).getAnalog(a).set(timeIndex, recordingJoystick.getRawAxis(a));
			}
			
			//For each possible digital input
			for (int d = 0; d < 5; d++)
			{
				//Get digital input #d, set its value at the timeIndex to be the current joystick button value for digital #d
				storedRoutes.get(activeRoute).getDigital(d).set(timeIndex, recordingJoystick.getRawButton(d));
			}
		}
	}
	
	/**
	 * Stops recording
	 * Saves all PhantomRoutes
	 */
	public void recordTerminate()
	{
		if (recording == true)
		{
			recording = false;
			timer.stop();
			timer.reset();
			saveRoutes();
		}
	}
	
	/**
	 * Initiate playback, to allow using getAxis and getButton
	 */
	public void playbackInit()
	{
		if (recording == false)
		{
			timer.reset();
			timer.start();
			playback = true;
		}
	}
	
	/**
	 * Stop playback
	 */
	public void playbackTerminate()
	{
		if (playback == true)
		{
			playback = false;
			timer.stop();
			timer.reset();
		}
	}
	
	/**
	 * Creates a copy of the route in the system. Will have name changed to _v2, _v3, etc
	 * This is the only way to modify the version number
	 * @param routeName - name of route to be copied
	 */
	public void copyRoute(String routeName)
	{
		PhantomRoute copy = new PhantomRoute(storedRoutes.get(routeName));
		storedRoutes.put(copy.getName(), copy);
	}
	
	/**
	 * Deletes the route from the system
	 * @param routeName - name of route to be deleted
	 */
	public void deleteRoute(String routeName)
	{
		storedRoutes.get(routeName).delete();
		storedRoutes.remove(routeName);
	}
	
	/**
	 * @param routeName - name of route who's data will be printed
	 * @return - nicely formatted table string
	 */
	public String getRouteOverview(String routeName)
	{
		return storedRoutes.get(routeName).getOverview();
	}
	
	/**
	 * @param routeName - name of route to print data of
	 */
	public void printRouteOverview(String routeName)
	{
		System.out.println(getRouteOverview(routeName));
	}
	
	/**
	 * @return - A multiline string, formatted as a table, that describes all routes stored on the system
	 */
	public String getAllOverviews()
	{
		String tableStr = "";
		tableStr += "+------------------------------------------------------------------------+" + "\n";
		tableStr += "|                           # " + "Phantom Routes" + " #                           |" + "\n";
		tableStr += "+------------------------------------------------------------------------+" + "\n";
		
		//Go through each stored PhantomRoute
		for (PhantomRoute pr : storedRoutes.values())
		{
			tableStr += pr.getOverview() + "\n";
			tableStr += "+------------------------------------------------------------------------+" + "\n";
		}
		return tableStr;
	}
	
	/**
	 * Prints getRouteTable()
	 */
	public void printAllOverviews()
	{
		System.out.println(getAllOverviews());
	}
}
