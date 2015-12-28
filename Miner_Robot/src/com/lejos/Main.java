package com.lejos;

import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.navigation.DifferentialPilot;

public class Main {
	static int maze_length = 198;
	EV3UltrasonicSensor ultrasonic_up;
	EV3UltrasonicSensor ultrasonic_down;
	EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
	EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);
	DifferentialPilot pilot = new DifferentialPilot(5.5, 11.73, leftMotor, rightMotor, false);

	EV3GyroSensor gyroSensor;
	
	public static void main(String[] args) {
		buttonInterface();
	}
	
	public static void buttonInterface(){
		while (Button.readButtons() != Button.ID_ESCAPE) {
		}
		while (Button.readButtons() != Button.ID_UP){
			
		}
		while (Button.readButtons() != Button.ID_RIGHT){
			
		}
		while (Button.readButtons() != Button.ID_DOWN){
			
		}
		while (Button.readButtons() != Button.ID_UP){
			
		}
	}
}
