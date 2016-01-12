package com.lejos;

import java.io.IOException;

import lejos.hardware.Button;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.robotics.subsumption.Behavior;

public class Behavior_Task_Mapping implements Behavior{
	GraphicsLCD graphicsLCD;
	private Mapping_Robot mapping;
	private boolean suppressed = false;
	public static int configuration;

	public Behavior_Task_Mapping(Robot robot, int i) {
		configuration = i;
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
		
		if(!suppressed){
			graphicsLCD.drawString("MAPPING", graphicsLCD.getWidth()/2, 60, graphicsLCD.VCENTER|graphicsLCD.HCENTER);

			mapping.connect(suppressed);
			
			Thread.yield();
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				mapping.locate(suppressed);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Thread.yield();
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				mapping.move(suppressed);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Thread.yield();
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mapping.getMap();
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
