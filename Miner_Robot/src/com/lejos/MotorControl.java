package com.lejos;
import java.util.Random;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class MotorControl {
	static EV3UltrasonicSensor ultrasonicSensorUp = new EV3UltrasonicSensor(SensorPort.S3);
	static NXTUltrasonicSensor ultrasonicSensorDown = new NXTUltrasonicSensor(SensorPort.S4);
	public static void main(String[] args) {
		Motor.B.resetTachoCount();
		//Motor.C.resetTachoCount();
		Motor.C.resetTachoCount();
		
		Motor.B.rotateTo(0);
	    //Motor.C.rotateTo(0);
	    Motor.B.setSpeed(100);
	    Motor.C.setSpeed(100);
	    //Motor.C.setSpeed(100);
	    
	    

		EV3 ev3 = (EV3) BrickFinder.getDefault();
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Motor Control", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		
		Button.waitForAnyPress();
		Motor.B.forward();
		Delay.msDelay(1000);
    	Motor.B.backward();
		Delay.msDelay(1000);
		Motor.C.rotate(-90);
		//Motor.C.rotate(90);
    	Thread.yield();
    	SampleProvider sampleProviderUp = ultrasonicSensorUp.getDistanceMode();
    	SampleProvider sampleProviderDown = ultrasonicSensorDown.getDistanceMode();
    	float ultrasonicSensorDifference = 3.2f;
    	while (Button.readButtons() != Button.ID_ESCAPE) {
    		float distanceUp = 0;
    		float distanceDown = 0;
    	
    		if(sampleProviderUp.sampleSize() > 0) {
		    	float [] sample = new float[sampleProviderUp.sampleSize()];
		    	sampleProviderUp.fetchSample(sample, 0);
		    	
		    	 distanceUp = sample[0];
    		}
		    	if(sampleProviderDown.sampleSize() > 0) {
			    	float [] sampleDown = new float[sampleProviderDown.sampleSize()];
			    	sampleProviderDown.fetchSample(sampleDown, 0);
			    	
			    	 distanceDown = sampleDown[0];
		    	}
    		
    		graphicsLCD.clear();
    		graphicsLCD.drawString("Up  : "+distanceUp+ultrasonicSensorDifference, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
    		graphicsLCD.drawString("Down: "+distanceDown, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2+20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);

    		
    		Delay.msDelay(500);
        	Thread.yield();
    	}
    	
		/*int currentState = 0;
		while (Button.readButtons() != Button.ID_ESCAPE) {
			
			Random random = new Random();
			//(random.nextInt() % 10 + 10) % 10;
			
			graphicsLCD.clear();
			graphicsLCD.drawString("Motor Control", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.drawString(""+currentState, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			
			if(currentState == 0) {				
				Motor.B.forward();
				Motor.C.forward();
				if(currentState==0){
					currentState++;
				}
			}
			else if(currentState == 1) {
				Motor.B.stop();
				Motor.C.stop();
			}
			/*else if(currentState == 2) {
				Motor.B.forward();
				Motor.C.backward();
			}
			else if(currentState == 3) {
				Motor.B.backward();
				Motor.C.forward();
			}
			else if(currentState == 4) {
				Motor.B.rotate(90,true);
				Motor.C.stop();
			}
			else if(currentState == 5) {
				Motor.B.stop();
				Motor.C.rotate(90,true);
			}
			else if(currentState == 6) {
				Motor.B.rotate(90,true);
				Motor.C.stop();
			}
			else if(currentState == 7) {
				Motor.B.stop();
				Motor.C.rotate(90,true);
			}
			else if(currentState == 8) {
				Motor.B.stop();
			}
			else if(currentState == 9) {
				Motor.B.stop();
			}*/
			
			//Delay.msDelay(5000);
	    	
	    	//Thread.yield();
		//}
	}

}
