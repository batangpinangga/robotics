package com.lejos;

import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;

public class Finding_Entrance {
	static EV3UltrasonicSensor ultrasonic_up;
	static NXTUltrasonicSensor ultrasonic_down;
	static NXTRegulatedMotor motor_ultrasonic;
	EV3LargeRegulatedMotor motor_left;
	EV3LargeRegulatedMotor motor_right;
	GraphicsLCD graphicsLCD;
	DifferentialPilot pilot;

	static float distance_wall_up = 0.34f; // between 33 and 34
	static float distance_wall_down = 0.20f; // between 18 and 20

	public Finding_Entrance(EV3UltrasonicSensor ultrasonic_up,
			NXTUltrasonicSensor ultrasonic_down,
			NXTRegulatedMotor motor_ultrasonic,
			EV3LargeRegulatedMotor motor_left,
			EV3LargeRegulatedMotor motor_right,
			GraphicsLCD graphicsLCD,
			DifferentialPilot pilot)
	{
		this.ultrasonic_up = ultrasonic_up;
		this.ultrasonic_down = ultrasonic_down;
		this.motor_ultrasonic = motor_ultrasonic;
		this.motor_left = motor_left;
		this.motor_right = motor_right;
		this.graphicsLCD = graphicsLCD;
		this.pilot =  pilot;
		graphicsLCD.clear();
		graphicsLCD.drawString("Starting Finding_Entrance", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
	}

	public void locate(){
		graphicsLCD.clear();
		graphicsLCD.drawString("locate...", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
		boolean inside = false;
		// not detected: 0; wall_left: -1; wall_right: 1
		int positionWall = 0;
		float sideValue = 0;
		float frontValue  = 0;
		float upUltraValue;
		float downUltraValue;
		//rotate sensors
		SampleProvider sampleProvider_down = ultrasonic_down.getDistanceMode();
		SampleProvider sampleProvider_up = ultrasonic_up.getDistanceMode();

		boolean startPosition = true;

		//start
		downUltraValue = getUltrasonicSensorValue(sampleProvider_down);
		upUltraValue = getUltrasonicSensorValue(sampleProvider_up);

		// case: sensors have to rotate
		if (downUltraValue > distance_wall_down && upUltraValue > distance_wall_up && startPosition){
			motor_ultrasonic.rotate(90);
			startPosition = false;
		}

		else if (downUltraValue > distance_wall_down && upUltraValue > distance_wall_up && !startPosition){
			motor_ultrasonic.rotate(-90);
			startPosition = true;
		}

		if(downUltraValue <= distance_wall_down){
			positionWall = 1;

			while(!inside){
				//define forward and side value
				sideValue = getUltrasonicSensorValue(sampleProvider_down);
				frontValue = getUltrasonicSensorValue(sampleProvider_up);

				move(frontValue,sideValue,positionWall);

			}
		}

		if(upUltraValue <= distance_wall_up){
			positionWall = -1;
			while(!inside){
				//define forward and side value
				sideValue = getUltrasonicSensorValue(sampleProvider_up);
				frontValue = getUltrasonicSensorValue(sampleProvider_down);

				move(frontValue,sideValue,positionWall);
			}
		}
	}

	void move(float front, float side,int positionWall){
		motor_left.setSpeed(1000);
		motor_right.setSpeed(1000);
		if(front <= 0.55){
			pilot.rotate(180); //motor drives towards the wall
		}
		motor_left.forward();
		motor_right.forward();

		if(side >= 2.00){
			pilot.stop();
			pilot.setRotateSpeed(100);
			if (positionWall == -1){
				pilot.rotate(90);
				pilot.forward();
			}
			else if (positionWall == 1){
				pilot.rotate(-90);
				pilot.forward();
			}
			motor_left.setSpeed(1000);
			motor_right.setSpeed(1000);
			motor_left.forward();
			motor_right.forward();
			Delay.msDelay(20);
		}
	}




	static float getUltrasonicSensorValue(SampleProvider sampleProvider) {
		//SampleProvider sampleProvider = ultrasonic_down.getDistanceMode();
		if(sampleProvider.sampleSize() > 0) {
			float [] samples = new float[sampleProvider.sampleSize()];
			sampleProvider.fetchSample(samples, 0);
			return samples[0];
		}
		return -1;		
	}
}
