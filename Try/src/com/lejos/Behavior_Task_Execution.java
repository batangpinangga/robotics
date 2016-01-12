package com.lejos;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Behavior;

public class Behavior_Task_Execution implements Behavior{
	private GraphicsLCD graphicsLCD;

	private boolean suppressed = false;

	private int[][]map;

	private Task_Execution task;

	public Behavior_Task_Execution(Robot robot){
		this.graphicsLCD = robot.getLCD();
		map = new int[3][3];
		task = new Task_Execution(robot, map);
	}

	@Override
	public boolean takeControl() {
		if(Button.LEFT.isDown())
			return true;
		else
			return false;
	}

	@Override
	public void action() {
		suppressed   = false;
		graphicsLCD.clear();
		while(!suppressed){
			graphicsLCD.drawString("EXECUTION", graphicsLCD.getWidth()/2, 60, graphicsLCD.VCENTER|graphicsLCD.HCENTER);
			task.go(suppressed);
			Thread.yield();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void suppress() {
		suppressed = true;
		task.stop(suppressed);
		graphicsLCD.clear();
	}

}
