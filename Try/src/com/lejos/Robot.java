package com.lejos;

import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.navigation.DifferentialPilot;

public class Robot {
	DifferentialPilot pilot;

	EV3 ev3 = (EV3) BrickFinder.getDefault();
	
	EV3UltrasonicSensor ultrasonic_up;
	NXTUltrasonicSensor ultrasonic_down;
	EV3GyroSensor gyroSensor;
	EV3ColorSensor colorSensor;
	
	EV3LargeRegulatedMotor motor_left;
	EV3LargeRegulatedMotor motor_right;
	NXTRegulatedMotor motor_ultrasonic;
	EV3LargeRegulatedMotor motor_grabber;
	
	GraphicsLCD graphicsLCD;

	public Robot(){
		//instantiate sensors
		ultrasonic_up = new EV3UltrasonicSensor(SensorPort.S3);
		ultrasonic_down  = new NXTUltrasonicSensor(SensorPort.S4);
		gyroSensor  = new EV3GyroSensor(SensorPort.S1);
		colorSensor = new EV3ColorSensor(SensorPort.S2);
		//instantiate motors
		motor_left = new EV3LargeRegulatedMotor(MotorPort.A);
		motor_right = new EV3LargeRegulatedMotor(MotorPort.D);
		motor_ultrasonic = new NXTRegulatedMotor(MotorPort.C);
		motor_grabber = new EV3LargeRegulatedMotor(MotorPort.B);
		//instantiate pilot
		pilot = new DifferentialPilot(5.5, 11.73, motor_left, motor_right, false);
		//instantiate display
		graphicsLCD = ev3.getGraphicsLCD();
	}
	
	public EV3UltrasonicSensor getUltrasonic_up(){
		return ultrasonic_up;
	}
	
	public NXTUltrasonicSensor getUltrasonic_down(){
		return ultrasonic_down;
	}
	
	public EV3GyroSensor getGyroSensor(){
		return gyroSensor;
	}
	
	public EV3ColorSensor getColorSensor(){
		return colorSensor;
	}
	
	public EV3LargeRegulatedMotor getLeftMotor(){
		return motor_left;
	}
	
	public EV3LargeRegulatedMotor getRightMotor(){
		return motor_right;
	}
	
	public NXTRegulatedMotor getUltrasonicMotor(){
		return motor_ultrasonic;
	}
	
	public EV3LargeRegulatedMotor getGrabberMotor(){
		return motor_grabber;
	}

	public GraphicsLCD getLCD(){
		return graphicsLCD;
	}
	
	public DifferentialPilot getPilot(){
		return pilot;
	}
	
	public void forward(){
//		Wheel leftWheel = WheeledChassis.modelWheel(Motor.A, 42.2).offset(72).gearRatio(2);
//		Wheel rightWheel = WheeledChassis.modelWheel(Motor.B, 42.2).offset(-72).gearRatio(2);
//		Chassis myChassis = new WheeledChassis( new Wheel[]{leftWheel, rightWheel}, WheeledChassis.TYPE_DIFFERENTIAL);
	}
	
}
