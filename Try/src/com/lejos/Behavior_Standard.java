package com.lejos;

import lejos.hardware.lcd.GraphicsLCD;
import lejos.robotics.subsumption.Behavior;

public class Behavior_Standard implements Behavior{
	private GraphicsLCD graphicsLCD;

	public Behavior_Standard(Robot robot){
		this.graphicsLCD = robot.getLCD();
	}

	@Override
	public boolean takeControl() {
		return true;
	}

	@Override
	public void action() {
		graphicsLCD.drawString("Miner Robot", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2-40, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);

		graphicsLCD.drawString("UP: Entrance", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 , GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.drawString("DOWN: Mapping", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 20 , GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.drawString("LEFT: Task", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 40 , GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
	}

	@Override
	public void suppress() {
		graphicsLCD.clear();
	}

}
