package com.lejos;

import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

public class Subsumptio {
	// constants
	static int maze_length = 198;
	static int tile_length = 33;
	static float distance_to_wall = 20.0f;

	public static void main(String[] args) {
	
		Robot robot = new Robot();
		Behavior entrance = new Behavior_Task_Entrance(robot);
		Behavior mapping = new Behavior_Task_Mapping(robot);
		Behavior task = new Behavior_Task_Execution(robot);
		Behavior reset = new Behavior_Reset_Program(robot); 
		Behavior standard = new Behavior_Standard(robot);
		
		Behavior[] behaviors = {standard,entrance, mapping, task, reset};
		Arbitrator arbitrator = new Arbitrator(behaviors,false);
		arbitrator.start();
		
		Behavior_Task_Mapping.configuration = ((Behavior_Task_Entrance) entrance).getConfiguration();
		Task_Execution.map = ((Behavior_Task_Mapping) mapping).getMap();

	}

}
