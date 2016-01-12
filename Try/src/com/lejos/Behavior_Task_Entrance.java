package com.lejos;

import lejos.hardware.Button;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.robotics.subsumption.Behavior;

public class Behavior_Task_Entrance implements Behavior{
	GraphicsLCD graphicsLCD;
	private static Finding_Entrance entrance;
	private boolean suppressed;

	public Behavior_Task_Entrance(Robot robot) {
		this.graphicsLCD = robot.getLCD();
		suppressed = false;
		entrance = new Finding_Entrance(robot);
	}

	@Override
	public boolean takeControl() {
		if(Button.UP.isDown()){
			return true;
		}
		else
			return false;
	}

	@Override
	public void action() {

		suppressed = false;
		graphicsLCD.clear();
		
		while(!suppressed){
			graphicsLCD.drawString("ENTRANCE", graphicsLCD.getWidth()/2, 60, graphicsLCD.VCENTER|graphicsLCD.HCENTER);

			entrance.locate(suppressed);
			Thread.yield();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}

	@Override
	public void suppress() {

		suppressed = true;
		entrance.stop(suppressed);
		graphicsLCD.clear();

	}
	
	public int getConfiguration(){
		return entrance.getConfiguration();
	}

}
