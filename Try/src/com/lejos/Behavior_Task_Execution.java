package com.lejos;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Behavior;

public class Behavior_Task_Execution implements Behavior{
	private GraphicsLCD graphicsLCD;
	Robot robot;
	private boolean suppressed = false;

	private int[][]map;

	private Task_Execution task;

	public Behavior_Task_Execution(Robot robot, int[][] is){
		this.robot = robot;
		map = is;
		this.graphicsLCD = robot.getLCD();
		map = new int[3][3];
		
	}

	@Override
	public boolean takeControl() {
		if(Button.LEFT.isDown()){
			task = new Task_Execution(robot, map);
			return true;
		}
		else
			return false;
	}

	@Override
	public void action() {
		suppressed   = false;
		graphicsLCD.clear();
		if(!suppressed){
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
