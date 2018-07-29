package org.usfirst.frc.team283.robot;

import java.io.File;
import java.util.HashMap;

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
 *
 * Usage:
 * Initialization:
 *     PhantomJoystick pj = new PhantomJoystick();
 * 	   pj.printStoredRoutes(); //See a stored routes
 *     pj.setRoute("napalm_upper_left_shot");
 *     
 * Periodic:
 *     pj.startRoute()   
 * 	   controlDrive(pj.getRawAxis(Constants.LeftX))
 * 
 */
public class PhantomJoystick2 
{
	//The folder where all NEW routes are saved. It's possible that some old routes fell outside this folder. Should not end with a slash
	public final static String routeFolder = "C:\\Users\\Benjamin\\Desktop\\routes";
	
	//The folder that is searched for all .route files. Should be as high up in the file system as possible
	//TODO: Find the best value for this
	public final static String rootSearchFolder = routeFolder;
	
	//True when playing back the data
	public boolean playback = false;
	
	//True when recording the data
	public boolean recording = false;
	
	//The string name of the route currently being written/read to/from
	//Why is this a string and not a PhantomRoute? Because java passes objects strangely, so we dont want to make a bunch of dupes
	private String activeRoute;
	
	//Contains all PhantomRoutes found all the system
	private HashMap<String, PhantomRoute> storedRoutes;
	
	public PhantomJoystick2()
	{
		storedRoutes = new HashMap<String, PhantomRoute>();
		
		//Create a directory representation, and start iterating through it for .route files
		createPhantomRoutes(new File(PhantomJoystick2.rootSearchFolder).listFiles());
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
	 * @return
	 */
	public double getRawAxis()
	{
		return 0;
	}
	
	public void recordInit()
	{
		
	}
	
	public void recordPeriodic()
	{
		
	}
	
	public void playbackInit()
	{
		
	}
	
	public void playbackPeriodic()
	{
		
	}
	
	/**
	 * @return
	 */
	public boolean getRawButton()
	{
		return false;
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
