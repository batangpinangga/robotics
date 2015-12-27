package com.lejos;

import java.util.Random;

public class GrabTheBall {
	public static void main(String[] args) {
		Motor.A.resetTachoCount();
		Motor.D.resetTachoCount();
		
		Motor.A.rotateTo(0);
	    Motor.D.rotateTo(0);
	    Motor.A.setSpeed(400);
	    Motor.D.setSpeed(400);
	    Motor.A.setAcceleration(800);
	    Motor.D.setAcceleration(800);
	    
	    Motor.A.setSpeed(5000);
		Motor.D.setSpeed(5000);

		EV3 ev3 = (EV3) BrickFinder.getDefault();
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Motor Control", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		
		Button.waitForAnyPress();
		
		while (Button.readButtons() != Button.ID_ESCAPE) {
			
			Random random = new Random();
			int currentState = (random.nextInt() % 10 + 10) % 10;
			currentState = 0;
			
			graphicsLCD.clear();
			graphicsLCD.drawString("Motor Control", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.drawString("Speed:"+Motor.A.getSpeed(), graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			
			if(currentState == 0) {	
				Motor.A.forward();
				Motor.D.forward();
			}
			else if(currentState == 1) {
				Motor.A.backward();
				Motor.D.backward();
			}
			else if(currentState == 2) {
				Motor.A.forward();
				Motor.D.backward();
			}
			else if(currentState == 3) {
				Motor.A.backward();
				Motor.D.forward();
			}
			else if(currentState == 4) {
				Motor.A.rotate(90,true);
				Motor.D.stop();
			}
			else if(currentState == 5) {
				Motor.A.stop();
				Motor.D.rotate(90,true);
			}
			else if(currentState == 6) {
				Motor.A.rotate(90,true);
				Motor.D.stop();
			}
			else if(currentState == 7) {
				Motor.A.stop();
				Motor.D.rotate(90,true);
			}
			else if(currentState == 8) {
				Motor.A.stop();
			}
			else if(currentState == 9) {
				Motor.D.stop();
			}
			Delay.msDelay(5000);
	    	Motor.A.stop(true);
	    	Motor.B.stop(true);
	    	Thread.yield();
	    	break;
}
