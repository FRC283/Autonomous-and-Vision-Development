package org.usfirst.frc.team283.robot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Date;

import javax.imageio.ImageIO;

/**
 * The purpose of this class is to read annotations and generate a schema image based on those annotations
 * @author Benjamin G. Ranson
 * @version 1.1
 */
public class Scheme
{
	public static void main(String[] args)
	{
		Scheme s = new Scheme("SB01", "org.usfirst.frc.team283.robot.DriveSubsystem", "org.usfirst.frc.team283.robot.CannonSubsystem", "org.usfirst.frc.team283.robot.ElevatorSubsystem");
		s.generate();
	}
	
	//Logitech Ports (Default)
		//Digital
			public static final int A = 0;
			public static final int B = 1;
			public static final int X = 2;
			public static final int Y = 3;
			public static final int LEFT_BUMPER = 4;
			public static final int RIGHT_BUMPER = 5;
			public static final int BACK = 6;
			public static final int START = 7;
			public static final int LEFT_STICK_BUTTON = 8;
			public static final int RIGHT_STICK_BUTTON = 9;
		//Analog
			public static final int LEFT_X = 10;
			public static final int LEFT_Y = 11;
			public static final int LEFT_TRIGGER = 12;
			public static final int RIGHT_TRIGGER = 13;
			public static final int RIGHT_X = 14;
			public static final int RIGHT_Y = 15;
	//Xbox Ports
		//Digital
			public static final int XBOX_A = 16;
			public static final int XBOX_B = 17;
			public static final int XBOX_X = 18;
			public static final int XBOX_Y = 19;
			public static final int XBOX_LEFT_BUMPER = 20;
			public static final int XBOX_RIGHT_BUMPER = 21;
			public static final int XBOX_BACK = 22;
			public static final int XBOX_START = 23;
			public static final int XBOX_LEFT_STICK_BUTTON = 24;
			public static final int XBOX_RIGHT_STICK_BUTTON = 25;
		//Analog
			public static final int XBOX_LEFT_X = 26;
			public static final int XBOX_LEFT_Y = 27;
			public static final int XBOX_LEFT_TRIGGER = 28;
			public static final int XBOX_RIGHT_TRIGGER = 29;
			public static final int XBOX_RIGHT_X = 30;
			public static final int XBOX_RIGHT_Y = 31;	
	//Left Physical Joystick
		//Digital
		//Analog
	//Right Physical Joystick
		//Digital
		//Analog
	
	private final int LABEL_BASE_X = 835;
	private final int LABEL_BASE_Y = 139;
	private final int LABEL_INCR = 42;
	private final int TITLE_X = 36;
	private final int TITLE_Y = 78;
	
	/** What kind of controller does the driver and operator use? */
	public enum driverMode
	{
		logitech;
	}
	
	@Repeatable(Schemas.class)
	@Retention(RetentionPolicy.RUNTIME)
	/**
	 * Place this annotation in front of "action" functions like so:
	 * (at)Schema(JoystickSchema.BUTTON)
	 * and the JoystickSchema class will read this and put that function with that button on the controls image
	 * See JoystickSchema for a complete list of buttons and axes
	 */
	@interface Schema
	{
		/**
		 * Stores the button/axis that the function is assigned to 
		 * Possible Values: are above
		 */
		int value(); //Since this is named value, you can simply input it next to the annotation
		String desc() default ""; //Possibly consider including this in a later version. Purpose would be to have better naming
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	/**
	 * Container annotation for schemas. Do not use.
	 */
	@interface Schemas
	{
		Schema[] value();
	}
	
	/** Our stored references to all classes in this project. Holds a max of 20 for now */
	private Class<?>[] classes = new Class[20];
	/** Printed at the top of the Schema */
	private String robotName = "";
	
	/**
	 * Takes a list of class names and fetches classes based on that
	 * @param classNames - List of class names
	 * @param title - Name of robot
	 */
	Scheme(String title, String... classNames)
	{
		this.robotName = title;
		for (int i = 0; i < classNames.length; i++)
		{
			try 
			{
				classes[i] = Class.forName(classNames[i]);
			} 
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Takes a list of instantiated classes and stores them
	 * @deprecated
	 * @param classInstances - List of objects of desired classes
	 * @param title - printed at the top of schema
	 */
	@Deprecated
	Scheme(String title, Object... classInstances)
	{
		for (int i = 0; i < classInstances.length; i++)
		{
			classes[i] = classInstances[i].getClass();
		}
	}
	
	/**
	 * Creates an updated control schema/image based on stored class annotations 
	 */
	public void generate()
	{
		System.out.println("<=== Generating Controls Image ===>");
		BufferedImage img = null;
		try 
		{
		    img = ImageIO.read(new File("ControlsSchemaBase.png"));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		Graphics g = img.getGraphics();
	    g.setFont(new Font("Consolas", Font.BOLD, 35));
	    g.setColor(Color.BLACK);
		g.drawString(this.robotName +  " Auto-Generated Controls - " + new Date(), TITLE_X, TITLE_Y);
		g.setFont(new Font("Consolas", Font.PLAIN, 30));
		for (Class<?> c : classes)
		{
			if (c != null)
			{
				System.out.println("Detected class : \"" + c.getName() + "\"");

				Method[] methods = c.getDeclaredMethods();
				for (Method m : methods)
				{
					System.out.println("	Method Detected: " + m.getName());
					Schema singleSchema = m.getAnnotation(Schema.class); //Only returns non-null for methods with ONE @Schema marker
					Schemas allSchemas = m.getAnnotation(Schemas.class); //Only methods with multiple @Schema markers have "@SchemaS"
					if (allSchemas != null)
					{
						for (Schema s : allSchemas.value())
						{
							System.out.println("		Annotation found at function \"" + m.getName() + "\" in class " + c.getName());
							if (s.desc().equals(""))
							{
								g.drawString(m.getName(), LABEL_BASE_X, LABEL_BASE_Y + ((s.value()) * LABEL_INCR));
							}
							else
							{
								g.drawString(s.desc(), LABEL_BASE_X, LABEL_BASE_Y + ((s.value()) * LABEL_INCR));
							}
						}
					}
					else if (singleSchema != null)
					{
						System.out.println("		Annotation found at function \"" + m.getName() + "\" in class " + c.getName());
						if (singleSchema.desc().equals(""))
						{
							g.drawString(m.getName(), LABEL_BASE_X, LABEL_BASE_Y + ((singleSchema.value()) * LABEL_INCR));
						}
						else
						{
							g.drawString(singleSchema.desc(), LABEL_BASE_X, LABEL_BASE_Y + ((singleSchema.value()) * LABEL_INCR));
						}
					}
				}
			}
		}
		
		
		g.dispose();
	    try 
	    {
			ImageIO.write(img, "png", new File("ControlsImage.png"));
		} 
	    catch (IOException e) 
	    {
			e.printStackTrace();
		}
	    System.out.println("<=== End Generation ===>");
	}
}
