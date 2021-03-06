package frc283.phantom;

import java.util.ArrayList;

/**
 * Just the raw data for the timeline
 */
public class RouteData 
{
	//Identifies the robot intended to be used with this route. E.g. "Guillotine"
	//Appears at start of name
	public String robot;
	
	//Gives a broad name to the route idea. E.g. "left_side_high_goal"
	//Appears at middle of name
	public String title;
	
	//Describes the intended path taken. E.g. "Starting from the baseline, travel about 12ft forwards, then turn and shoot at the goal"
	public String description;
	
	//A number that determines which iteration this is of the original.
	//Appears on end of name if greater than 1
	public int version;
	
	//A small description of the intended role e.g. "operator" or "driver"
	public String role;
	
	//The time, in milliseconds, between ArrayList values
	//Different versions of this class may use different spacing
	public int timeSpacing;
	
	//A number that can be used to see when this route's timeline data was last modified
	public long lastModified;
	
	//An array containing ArrayLists of doubles
	//Length 6
	//These are the analog "timelines"
	protected ArrayList<Double>[] analog;   
	
	//An array containing ArrayLists of booleans
	//Length 10
	//These are the digital "timelines"
	protected ArrayList<Boolean>[] digital;  
}
