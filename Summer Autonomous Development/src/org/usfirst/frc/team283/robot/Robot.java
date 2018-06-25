/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team283.robot;

import java.io.IOException;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class Robot extends IterativeRobot {
	private static final int DRIVER_CONTROLLER_PORT = 0; //move to constants page
	PhantomJoystick virtualJs;
	Joystick js;

	
	@Override
	public void robotInit() 
	{
		try {
			virtualJs = new PhantomJoystick(new Joystick(DRIVER_CONTROLLER_PORT), "/home/");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		js = new Joystick(DRIVER_CONTROLLER_PORT);
	}
	@Override
	public void autonomousInit() 
	{
	
	}

	
	@Override
	public void autonomousPeriodic()
	{
		virtualJs.playback();
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic()
	{
		virtualJs.recordInit(js.getRawButton(Constants.START));
		try {
			virtualJs.recordPeriodic();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic()
	{
	
	}
}
