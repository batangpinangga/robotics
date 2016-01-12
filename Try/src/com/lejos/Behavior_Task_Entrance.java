package com.lejos;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Behavior;

public class Behavior_Task_Entrance implements Behavior{
	static EV3 ev3 = (EV3) BrickFinder.getDefault();
	GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
	private boolean done;
	private DifferentialPilot pilot;
	private EV3UltrasonicSensor ultrasonic_up;
	private NXTUltrasonicSensor ultrasonic_down;
	private NXTRegulatedMotor motor_ultrasonic;
	private EV3LargeRegulatedMotor motor_left;
	private EV3LargeRegulatedMotor motor_right;
	private EV3GyroSensor gyroSensor;
	private static Finding_Entrance entrance;
	private boolean suppressed;

//	public Task_Entrance(EV3UltrasonicSensor ultrasonic_up, NXTUltrasonicSensor ultrasonic_down, NXTRegulatedMotor motor_ultrasonic, EV3LargeRegulatedMotor motor_left, EV3LargeRegulatedMotor motor_right, GraphicsLCD graphicsLCD, DifferentialPilot pilot, EV3GyroSensor gyroSensor) {
	public Behavior_Task_Entrance(Robot robot) {
		this.graphicsLCD = robot.getLCD();
		done = false;
		suppressed = false;
		entrance = new Finding_Entrance(robot);
	}

	@Override
	public boolean takeControl() {
		if(Button.UP.isDown()){
			return true;
		}
		else
			return false;
	}

	@Override
	public void action() {

		suppressed = false;
		graphicsLCD.clear();
		
		while(!suppressed){
			graphicsLCD.drawString("ENTRANCE", graphicsLCD.getWidth()/2, 60, graphicsLCD.VCENTER|graphicsLCD.HCENTER);

			entrance.locate(suppressed);
			Thread.yield();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}

	@Override
	public void suppress() {

		suppressed = true;
		entrance.stop(suppressed);
		graphicsLCD.clear();

	}
	
	public static int getConfiguration(){
		return entrance.getConfiguration();
	}

}
