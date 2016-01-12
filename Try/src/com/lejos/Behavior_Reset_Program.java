package com.lejos;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Behavior;

public class Behavior_Reset_Program implements Behavior{
	private GraphicsLCD graphicsLCD;
	private DifferentialPilot pilot;
	private boolean suppressed = false;

	public Behavior_Reset_Program(Robot robot){
		this.graphicsLCD = robot.getLCD();
	}

	@Override
	public boolean takeControl() {
		if(Button.ESCAPE.isDown())
			return true;
		else
			return false;
	}

	@Override
	public void action() {
		graphicsLCD.clear();
//		suppressed  = false;
//		graphicsLCD.clear();
//		
//		while(!suppressed){
//			graphicsLCD.drawString("ENTRANCE", graphicsLCD.getWidth()/2, 60, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
		//		graphicsLCD.drawString("Interrupted", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 40 , GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
	}

	@Override
	public void suppress() {
		Sound.beepSequenceUp();
//		graphicsLCD.clear();
//
	}

}
