package com.lejos;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.navigation.DifferentialPilot;

public class Main {
	// constants
	static int maze_length = 198;
	static int tile_length = 33;
	static float distance_to_wall = 20.0f;
	
	//initialization
	static EV3 ev3 = (EV3) BrickFinder.getDefault();
	
	static EV3UltrasonicSensor ultrasonic_up = new EV3UltrasonicSensor(SensorPort.S3);
	static NXTUltrasonicSensor ultrasonic_down = new NXTUltrasonicSensor(SensorPort.S4);
	static EV3LargeRegulatedMotor motor_left = new EV3LargeRegulatedMotor(MotorPort.A);
	static EV3LargeRegulatedMotor motor_right = new EV3LargeRegulatedMotor(MotorPort.D);
	static NXTRegulatedMotor motor_ultrasonic = new NXTRegulatedMotor(MotorPort.C);
	static EV3LargeRegulatedMotor motor_grabber = new EV3LargeRegulatedMotor(MotorPort.B);
	//static DifferentialPilot pilot = new DifferentialPilot(5.5, 11.73, motor_left, motor_right, false);
	static DifferentialPilot pilot = new DifferentialPilot(5.5, 5.55, 11.73, motor_left, motor_right, false);

	static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S1);

	public static void main(String[] args) {
		pilot.setTravelSpeed(100);
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		graphicsLCD.drawString("Miner Robot", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2-40, graphicsLCD.VCENTER|graphicsLCD.HCENTER);

		while(true)
		{	
			graphicsLCD.drawString("UP for Entrance", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 , graphicsLCD.VCENTER|graphicsLCD.HCENTER);
			graphicsLCD.drawString("DOWN for Mapping", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 20 , graphicsLCD.VCENTER|graphicsLCD.HCENTER);
			graphicsLCD.drawString("LEFT for Task Execution", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 40 , graphicsLCD.VCENTER|graphicsLCD.HCENTER);
			//ENTRANCE TASK
			if (Button.UP.isDown()){
				graphicsLCD.clear();
				//while (Button.readButtons() != Button.ID_ESCAPE){
					graphicsLCD.drawString("ENTRANCE", graphicsLCD.getWidth()/2, 60, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
					Finding_Entrance entrance = new Finding_Entrance(ultrasonic_up, ultrasonic_down, motor_ultrasonic, motor_left, motor_right, graphicsLCD, pilot,gyroSensor);
					entrance.locate();
				//}
				graphicsLCD.clear();
			}
			//MAPPING TASK
			if (Button.DOWN.isDown()){
				graphicsLCD.clear();
				while (Button.readButtons() != Button.ID_ESCAPE){
					graphicsLCD.drawString("MAPPING", graphicsLCD.getWidth()/2, 60, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
				}
				graphicsLCD.clear();
			}
			
			//TASK EXECUTION
			if (Button.LEFT.isDown()){
				graphicsLCD.clear();
				while (Button.readButtons() != Button.ID_ESCAPE){
					graphicsLCD.drawString("TASK EXECUTION", graphicsLCD.getWidth()/2, 60, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
				}
				graphicsLCD.clear();
			}
		}
	}

}
