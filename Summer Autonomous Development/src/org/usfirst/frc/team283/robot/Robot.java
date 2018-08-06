package org.usfirst.frc.team283.robot;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import frc283.phantom.PhantomJoystick;
import frc283.phantom.PhantomRoute;

public class Robot extends IterativeRobot 
{
	private static final double TRIGGER_THRESHOLD = 0.5;
	
	DriveSubsystem driveSubsystem;
	ElevatorSubsystem elevatorSubsystem;
	CannonSubsystem cannonSubsystem;
	NetworkTable table; //Carries the controls image to the smartdashboard
	
	PhantomJoystick phantomLogitech; 
	
	Joystick logitech;
	Joystick xbox;
	
	public void robotInit() 
	{
		System.out.println("robotInit called");
		driveSubsystem = new DriveSubsystem();
		elevatorSubsystem = new ElevatorSubsystem();
		cannonSubsystem = new CannonSubsystem();
		
		logitech = new Joystick(Constants.LOGITECH_PORT);
		xbox = new Joystick(Constants.XBOX_PORT);
		
		phantomLogitech = new PhantomJoystick(logitech);
		phantomLogitech.setActiveRoute("sb01_test_route_operator");
		phantomLogitech.printAllOverviews();
	}
	
	public void autonomousInit()
	{
		PhantomRoute pRoute = new PhantomRoute("test route", "sb01", "Ben's test route. It doesn't do anything in particular.", "Operator", 100, PhantomJoystick.routeFolder);
		//pRoute.save();
	}
	
	@Override
	public void teleopPeriodic()
	{
		driveSubsystem.drive(logitech.getRawAxis(Constants.LEFT_Y), logitech.getRawAxis(Constants.RIGHT_Y), logitech.getRawAxis(Constants.LEFT_X), logitech.getRawAxis(Constants.RIGHT_X), (logitech.getRawButton(Constants.LEFT_STICK_BUTTON) || logitech.getRawButton(Constants.RIGHT_STICK_BUTTON)));
		driveSubsystem.driveMode(logitech.getRawButton(Constants.BACK), logitech.getRawButton(Constants.START));
		elevatorSubsystem.raiseLower(logitech.getRawButton(Constants.LEFT_BUMPER), (logitech.getRawAxis(Constants.LEFT_TRIGGER) >= TRIGGER_THRESHOLD));
		if (logitech.getRawButton(Constants.RIGHT_BUMPER)) 
			cannonSubsystem.fillInit();
		if (logitech.getRawAxis(Constants.RIGHT_TRIGGER) >= TRIGGER_THRESHOLD)
			cannonSubsystem.fireInit(logitech.getRawButton(Constants.X));
		if (xbox.getRawButton(Constants.A))
			phantomLogitech.recordInit();
		if (xbox.getRawButton(Constants.B))
			phantomLogitech.recordStop();
		if (xbox.getRawButton(Constants.Y))
			phantomLogitech.printAllOverviews();
	
		cannonSubsystem.periodic();
		driveSubsystem.periodic();
	}
}

