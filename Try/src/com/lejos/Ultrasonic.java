package com.lejos;

import lejos.hardware.Button;
import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;

public class Ultrasonic {
	static EV3 ev3 = (EV3) BrickFinder.getDefault();
	static EV3UltrasonicSensor ultrasonic_up = new EV3UltrasonicSensor(SensorPort.S3);
	static NXTUltrasonicSensor ultrasonic_down = new NXTUltrasonicSensor(SensorPort.S4);

	public static void main(String[] args) {
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		graphicsLCD.drawString("Ultrasonic", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2-40, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
		Button.waitForAnyPress();
		SampleProvider sampleProvider_down = ultrasonic_down.getDistanceMode();
		SampleProvider sampleProvider_up = ultrasonic_up.getDistanceMode();
		while (Button.readButtons() != Button.ID_ESCAPE) {
			graphicsLCD.clear();
			graphicsLCD.drawString("Up " + getUltrasonicSensorValue(sampleProvider_up), graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2-20, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
			graphicsLCD.drawString("Down " + getUltrasonicSensorValue(sampleProvider_down), graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
			Delay.msDelay(10);
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
