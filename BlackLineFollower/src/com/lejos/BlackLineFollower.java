package com.lejos;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.LightDetectorAdaptor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.hardware.motor.Motor;

public class BlackLineFollower {

	static EV3 ev3 = (EV3) BrickFinder.getDefault();

	static EV3ColorSensor lightSensor = new EV3ColorSensor(SensorPort.S3);
	static LightDetectorAdaptor lightDetectorAdaptor = new LightDetectorAdaptor((SampleProvider)lightSensor);
	public static void main(String[] args) {
		// Initialization
		Direction direction = Direction.LEFT;
		Motor.B.resetTachoCount();
		Motor.C.resetTachoCount();
		
		Motor.B.rotateTo(0);
	    Motor.C.rotateTo(0);
	    Motor.B.setSpeed(400);
	    Motor.C.setSpeed(400);
	    Motor.B.setAcceleration(800);
	    Motor.C.setAcceleration(800);
	    
	    Motor.B.setSpeed(100);
		Motor.C.setSpeed(100);
		
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		
		graphicsLCD.clear();
		
		graphicsLCD.drawString("Black Follower", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		
		Button.waitForAnyPress();
		
		//DirectionChangePermission => controls robot change the direction one time 
		//when the robot is out of black line
		boolean DirectionChangePermission = false;
		
		while (Button.readButtons() != Button.ID_ESCAPE) {
			float value = lightDetectorAdaptor.getLightValue();
			//Shows information on LCD screen
			graphicsLCD.clear();
			graphicsLCD.drawString(""+value, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.drawString("Direction : " + direction.getValue(), 10, 60 , GraphicsLCD.VCENTER|GraphicsLCD.LEFT);
			boolean isBlackLine = value < 0.05;
			graphicsLCD.drawString("Black Line: " + isBlackLine, 10, 100 , GraphicsLCD.VCENTER|GraphicsLCD.LEFT);	
			//In Black Line
			if( isBlackLine){
				DirectionChangePermission = true;
				if(direction.getValue().equals("left")){
					turnLeft();
				}else if (direction.getValue().equals("right")){
					turnRight();
				}
			
			//Out of Black Line
			}else{
				
				//Changes one time when robot is out of Black Line
				if(DirectionChangePermission){
					DirectionChangePermission = false;
					if(direction.getValue().equals("left")){
						direction = Direction.RIGHT;
					}else if (direction.getValue().equals("right")){
						direction = Direction.LEFT;
					}
				}
				
				if(direction.getValue().equals("left")){
					turnLeft();
				}else if (direction.getValue().equals("right")){
					turnRight();
				}
				
			}
			
			
			Delay.msDelay(100);
			
			Thread.yield();
		}	
			
	}
	
	//Turn Left sharper
	public static void turnLeft(){
		Motor.B.setSpeed(50);
		Motor.C.setSpeed(100);
		Motor.B.backward();
		Motor.C.forward();
		
	}
	
	//Turn Right sharper
	public static void turnRight(){
		Motor.B.setSpeed(100);
		Motor.C.setSpeed(50);
		Motor.C.backward();
		Motor.B.forward();
		
	}
	
}
