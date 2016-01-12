package com.lejos;

import java.io.IOException;

import lejos.hardware.Button;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.robotics.subsumption.Behavior;

public class Behavior_Task_Mapping implements Behavior{
	GraphicsLCD graphicsLCD;
	private Mapping_Robot mapping;
	private Mapping_PC map_pc;
	
	private boolean suppressed = false;
	public static int configuration;

	public Behavior_Task_Mapping(Robot robot) {
		this.graphicsLCD = robot.getLCD();
		mapping = new Mapping_Robot(robot, configuration);
	}

	@Override
	public boolean takeControl() {
		if (Button.DOWN.isDown()){
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
			graphicsLCD.drawString("MAPPING", graphicsLCD.getWidth()/2, 60, graphicsLCD.VCENTER|graphicsLCD.HCENTER);

			try {
				mapping.connect(suppressed);
				mapping.locate();
				mapping.move();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			mapping.getMap();
			
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
		try {
			mapping.stop(suppressed);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		graphicsLCD.clear();
	}

	public int[][] getMap() {
		return mapping.getMap();
	}

}
