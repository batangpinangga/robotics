package com.lejos;

import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;

public class Main {
	static int maze_length = 198;
	EV3UltrasonicSensor ultrasonic_up;
	EV3UltrasonicSensor ultrasonic_down;
	DifferentialPilot pilot;
	RegulatedMotor motor_ultrasonic;
	EV3GyroSensor gyroSensor;
	
	public static void main(String[] args) {
		
	}
	
	public void ButtonInterface(){
		
	}
}
