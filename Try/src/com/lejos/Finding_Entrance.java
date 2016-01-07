package com.lejos;

import lejos.hardware.Button;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;

public class Finding_Entrance {
	static EV3UltrasonicSensor ultrasonic_up;
	static NXTUltrasonicSensor ultrasonic_down;
	static NXTRegulatedMotor motor_ultrasonic;
	static EV3GyroSensor gyroSensor;
	static EV3LargeRegulatedMotor motor_left;
	static EV3LargeRegulatedMotor motor_right;
	static SampleProvider sampleProviderFront;
	static SampleProvider sampleProviderSide;
	static SampleProvider sampleProvider_down;
	static SampleProvider sampleProvider_up;
	static float threshold_ev3_side = 0.14f;
	static float threshold_nxt_side = 0; 
	static float threshold_side;
	static float threshold_front;
	static int positionWall;
	static GraphicsLCD graphicsLCD;
	static DifferentialPilot pilot;
	static int configurationInitial;
	static boolean inside;
	static float distance_wall_up = 0.34f; // between 33 and 34
	static float distance_wall_down = 0.20f; // between 18 and 20

	public Finding_Entrance(EV3UltrasonicSensor ultrasonic_up,
			NXTUltrasonicSensor ultrasonic_down,
			NXTRegulatedMotor motor_ultrasonic,
			EV3LargeRegulatedMotor motor_left,
			EV3LargeRegulatedMotor motor_right,
			GraphicsLCD graphicsLCD,
			DifferentialPilot pilot, 
			EV3GyroSensor gyroSensor)
	{
		this.ultrasonic_up = ultrasonic_up;
		this.ultrasonic_down = ultrasonic_down;
		this.motor_ultrasonic = motor_ultrasonic;
		this.motor_left = motor_left;
		this.motor_right = motor_right;
		this.graphicsLCD = graphicsLCD;
		this.pilot =  pilot;
		this.gyroSensor = gyroSensor;
		threshold_side = threshold_ev3_side;
		
		configurationInitial = 1;
		positionWall = -1;
		
		sampleProvider_down = ultrasonic_down.getDistanceMode();
		sampleProvider_up = ultrasonic_up.getDistanceMode();
		sampleProviderFront = sampleProvider_down;
		sampleProviderSide = sampleProvider_up;
		inside = false;
		graphicsLCD.clear();
		graphicsLCD.drawString("Starting Finding_Entrance", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
	}

	public void locate(){
		graphicsLCD.clear();
		graphicsLCD.drawString("locate...", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
		
		// not detected: 0; wall_left: -1; wall_right: 1
		
		float sideValue = 0;
		float frontValue  = 0;
		
		float upUltraValue;
		float downUltraValue;
		//rotate sensors
		

		boolean startPosition = true;
		
		//start
		downUltraValue = getUltrasonicSensorValue(sampleProvider_down);
		upUltraValue = getUltrasonicSensorValue(sampleProvider_up);
		sideValue = getUltrasonicSensorValue(sampleProviderSide);
		
		if (sideValue-threshold_side>0.30f){
			changeConfiguration();
		}
		
		
		
		// case: sensors have to rotate
		/*if (downUltraValue > distance_wall_down && upUltraValue > distance_wall_up && startPosition){
			motor_ultrasonic.rotate(90);
			startPosition = false;
		}

		else if (downUltraValue > distance_wall_down && upUltraValue > distance_wall_up && !startPosition){
			motor_ultrasonic.rotate(-90);
			startPosition = true;
		}

		if(downUltraValue <= distance_wall_down){
			positionWall = 1;
		 */
		
		
		
			while(!inside){
				//define forward and side value
				sideValue = getUltrasonicSensorValue(sampleProviderSide);
				frontValue = getUltrasonicSensorValue(sampleProviderFront);
				graphicsLCD.clear();
			//	graphicsLCD.drawString("front = "+frontValue, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
			//	graphicsLCD.drawString("side = "+sideValue, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2+20, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
				
				move(frontValue,sideValue,positionWall);

			}
		//rotate_via_gyro(90);
		//}

		/*if(upUltraValue <= distance_wall_up){
			positionWall = -1;
			while(!inside){
				//define forward and side value
				sideValue = getUltrasonicSensorValue(sampleProviderSide);
				frontValue = getUltrasonicSensorValue(sampleProviderFront);

				move(frontValue,sideValue,positionWall);
			}
		}*/
	}

	void move(float front, float side,int positionWall){
		//motor_left.setSpeed(500);
		//motor_right.setSpeed(500);
		float distanceSide =0;
		float distanceFront = 0;
		boolean stop = false;
		int situation =0;
		while(!stop){
			float first = getGyroValue();
			pilot.travel(11,(distanceFront<0.25f && distanceFront !=0) || (distanceSide >=1.5f) );
			float second = getGyroValue();
			fix_rotation(0, second-first);
			distanceSide = getUltrasonicSensorValue(sampleProviderSide);
			distanceFront = getUltrasonicSensorValue(sampleProviderFront);
			graphicsLCD.clear();
			graphicsLCD.drawString("front = "+distanceFront, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2-40, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
			graphicsLCD.drawString("side = "+distanceSide, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2-20, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
			Thread.yield();
			if (distanceFront<0.25f){
				situation = 1;
				stop = true;
				graphicsLCD.drawString("condition = "+situation, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
			}else if (distanceSide >=1.5f){
				situation = 2;
				stop = true;
				graphicsLCD.drawString("condition = "+situation, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2+20, graphicsLCD.VCENTER|graphicsLCD.HCENTER);

			}
		}
		
		
		
		
		
		
		/*pilot.travel(11,stop);
		while(pilot.isMoving()){
			distanceSide = getUltrasonicSensorValue(sampleProviderSide);
			distanceFront = getUltrasonicSensorValue(sampleProviderFront);
			graphicsLCD.clear();
			graphicsLCD.drawString("front = "+distanceFront, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
			graphicsLCD.drawString("side = "+distanceSide, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2+20, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
			
			if(distanceFront <= 0.25f){
				stop = true;
				pilot.quickStop();
				situation = 1;
				graphicsLCD.drawString("condition = "+situation, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2+40, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
				
			}else 
				if(distanceSide >= 1.50f){
					stop = true;
					pilot.quickStop();
					situation = 2;	
					graphicsLCD.drawString("condition = "+situation, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2+60, graphicsLCD.VCENTER|graphicsLCD.HCENTER);

				}
		}*/
		if(situation == 1){
			Delay.msDelay(100);
			changeConfiguration();
			rotate_via_gyro(180);
			Delay.msDelay(100);
		}else if (situation == 2){
			pilot.travel(20);
			if (positionWall == -1){
				//pilot.rotate(90);
				rotate_via_gyro(90);
				pilot.stop();
			}
			else if (positionWall == 1){
				rotate_via_gyro(-90);
				//pilot.rotate(-90);
				pilot.stop();
			}
			pilot.travel(20);
		}else if (situation == 0){
			
		}
		/*if(front <= 0.25f){
			
			changeConfiguration();
			
			graphicsLCD.drawString("wall ", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2+40, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
			rotate_via_gyro(180);
			pilot.stop();
			//pilot.rotate(180); //motor drives towards the wall
			
		}else if(side >= 1.50f){
			pilot.stop();
			//pilot.travel(33);
			if (checkInside()){
				inside = true;
			}
			
			pilot.setRotateSpeed(100);
			if (positionWall == -1){
				//pilot.rotate(90);
				rotate_via_gyro(90);
				pilot.stop();
			}
			else if (positionWall == 1){
				rotate_via_gyro(-90);
				//pilot.rotate(-90);
				pilot.stop();
			}
			motor_left.setSpeed(500);
			motor_right.setSpeed(500);
		}*/
		//if(!inside)
			//goForward(1);
			//pilot.travel(11,true);
		Thread.yield();
	}

	//rotates turn_angle degrees and calls fix_rotation method
	public static void rotate_via_gyro(float turn_angle){
		float first = getGyroValue();
		SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
		float angle = 0;
		float angle2;
    	//while (Button.readButtons() != Button.ID_ESCAPE) {
    			
    		if(sampleProvider.sampleSize() > 0) {
				float [] sample2 = new float[sampleProvider.sampleSize()];
		    	sampleProvider.fetchSample(sample2, 0);
				angle2 = sample2[0];
    		
    		
    	  pilot.rotate(turn_angle);
    	  float second = getGyroValue();
    	  graphicsLCD.clear();
			graphicsLCD.drawString("angle: "+ (second-first), graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			fix_rotation(turn_angle, (second-first));
    		}
    		Thread.yield();
    }
	public static void changeConfiguration(){
		
		if (configurationInitial == -1){
			motor_ultrasonic.rotate(-90);
			sampleProviderFront = sampleProvider_down;
			sampleProviderSide = sampleProvider_up;
			threshold_side = threshold_nxt_side;
			configurationInitial = 1;
			positionWall = -1*configurationInitial;
			
		}else if (configurationInitial == 1){
			motor_ultrasonic.rotate(90);
			sampleProviderFront = sampleProvider_up;
			sampleProviderSide = sampleProvider_down;
			configurationInitial = -1;
			positionWall = -1*configurationInitial;
			threshold_side = threshold_ev3_side;
		}
		motor_ultrasonic.stop();
		Delay.msDelay(100);
		Thread.yield();
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
	//fix rotation error
	public static void fix_rotation(float turn_angle, float angle2){
		boolean finish = false;
		motor_left.resetTachoCount();
		motor_right.resetTachoCount();
		
		motor_left.rotateTo(0);
		motor_right.rotateTo(0);
	    motor_left.setAcceleration(800);
	    motor_right.setAcceleration(800);
	    
	    motor_left.setSpeed(10);
	    motor_right.setSpeed(10);
		SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
		float first = getGyroValue();
		float second = 0;
		
		while (!finish ) {
			second = getGyroValue();
			if(second-first > turn_angle-angle2) {
					motor_left.forward();
					motor_right.backward();
				}
				else if(second-first < turn_angle-angle2) {
					motor_left.backward();
					motor_right.forward();
				}
				else {
					motor_left.stop(true);
					motor_right.stop(true);
					finish = true;
				}
	    	
				Delay.msDelay(10);
			
			Thread.yield();
		}
		graphicsLCD.drawString("angle fix: "+ (second-first), graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2+20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);

	}
	public static float getGyroValue() {
		float angle = 0;
		SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
		if(sampleProvider.sampleSize() > 0) {
			
			float [] sample = new float[sampleProvider.sampleSize()];
	    	sampleProvider.fetchSample(sample, 0);
			 angle = sample[0];
		}
		return angle;
	}
	public static boolean checkInside() {
		//SampleProvider sampleProvider = ultrasonic_down.getDistanceMode();
		boolean result = false;
		float dist = 0;
		goForward(1);
		while(pilot.isMoving()){
			if(sampleProviderSide.sampleSize() > 0) {
				float [] samples = new float[sampleProviderSide.sampleSize()];
				sampleProviderSide.fetchSample(samples, 0);
				dist = samples[0];
			}
			if (dist <= 0.30f-threshold_side){
				result = true;
			}
		}
		goForward(-1);
		return result;
		
				
	}
	public static void goForward(int direction){
		pilot.setTravelSpeed(100);
		pilot.setAcceleration(100);
		pilot.travel(33*direction);
		SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
		while(pilot.isMoving()){
			float [] sample = new float[sampleProvider.sampleSize()];
	    	sampleProvider.fetchSample(sample, 0);
			float angle = sample[0];
			if(angle>=1){
				pilot.stop();
				fix_rotation(0, angle);
				pilot.travel(33);
			}
			Thread.yield();	
		}
	}
}


