package com.lejos;

import java.io.IOException;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
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

public class Miner_Robot {
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
	static DifferentialPilot pilot = new DifferentialPilot(5.5, 11.73, motor_left, motor_right, false);
	static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S1);
	static EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S2);

	public static void main(String[] args) {
		minerRobot();
	}

	private static void minerRobot() {
		pilot.setTravelSpeed(100);
		pilot.setRotateSpeed(100);
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		graphicsLCD.drawString("Miner Robot", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2-40, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
		int configuration = 1;
		int[][] map = new int [3][2];
		
//		Button.ESCAPE.addKeyListener(new KeyListener() {
//			@Override
//			public void keyPressed(Key k) {
//				entrance.stop();
//			}
//
//			@Override
//			public void keyReleased(Key k) {
//				// TODO Auto-generated method stub
//				minerRobot();
//				
//			}
//			});

		while(true)
		{	
			graphicsLCD.drawString("UP for Entrance", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 , graphicsLCD.VCENTER|graphicsLCD.HCENTER);
			graphicsLCD.drawString("DOWN for Mapping", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 20 , graphicsLCD.VCENTER|graphicsLCD.HCENTER);
			graphicsLCD.drawString("LEFT for Task Execution", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 40 , graphicsLCD.VCENTER|graphicsLCD.HCENTER);
			//ENTRANCE TASK
			if (Button.UP.isDown()){
				graphicsLCD.clear();
				while (Button.readButtons() != Button.ID_ESCAPE){
					graphicsLCD.drawString("ENTRANCE", graphicsLCD.getWidth()/2, 60, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
					Finding_Entrance entrance = new Finding_Entrance(ultrasonic_up, ultrasonic_down, motor_ultrasonic, motor_left, motor_right, graphicsLCD, pilot,gyroSensor);
					entrance.run();
					configuration = entrance.getConfiguration();
				}
				graphicsLCD.clear();
			}
			//MAPPING TASK
			if (Button.DOWN.isDown()){
				graphicsLCD.clear();
				//while (Button.readButtons() != Button.ID_ESCAPE){
				//graphicsLCD.drawString("MAPPING", graphicsLCD.getWidth()/2, 60, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
				//graphicsLCD.clear();
				Mapping_Robot mapping = new Mapping_Robot(ultrasonic_up, ultrasonic_down, colorSensor, motor_ultrasonic, motor_left, motor_right, graphicsLCD, pilot, gyroSensor, configuration);

				try {
					mapping.locate();
					mapping.move();
					map = mapping.getMap();//saves the map
					for(int i = 0; i<map.length; i++){
						for(int j = 0; j<map.length; j++){
							System.out.print(map[i][j] + " ");
						}
						System.out.println();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				//}
				graphicsLCD.clear();
			}

			//TASK EXECUTION
			if (Button.LEFT.isDown()){
				graphicsLCD.clear();
				graphicsLCD.drawString("TASK EXECUTION", graphicsLCD.getWidth()/2, 60, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
				int [][] map_try = {{33,33,2},{132,33,1},{33,132,4}};
				Task_Execution task = new Task_Execution(map_try, colorSensor, motor_left, motor_right, graphicsLCD, pilot, gyroSensor, motor_grabber);
				task.go();
				graphicsLCD.clear();
			}
		}
	}

}
