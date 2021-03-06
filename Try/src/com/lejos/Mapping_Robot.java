package com.lejos;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.Color;
import lejos.robotics.ColorAdapter;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;

public class Mapping_Robot {
	static int tile_length = 33;

	static EV3UltrasonicSensor ultrasonic_up;
	static NXTUltrasonicSensor ultrasonic_down;
	static EV3GyroSensor gyroSensor;
	static EV3ColorSensor colorSensor;

	static NXTRegulatedMotor motor_ultrasonic;
	static EV3LargeRegulatedMotor motor_left;
	static EV3LargeRegulatedMotor motor_right;

	static GraphicsLCD graphicsLCD;
	static DifferentialPilot pilot;

	static ColorAdapter colorAdapter;

	static float threshold_nxt_side = 0; 
	static float threshold_side;
	static float threshold_front;

	static SampleProvider sampleProviderFront;
	static SampleProvider sampleProviderSide;
	static SampleProvider sampleProvider_down;
	static SampleProvider sampleProvider_up;

	static float side;
	static float front;
	static boolean reverse;
	static boolean green = false;
	static boolean red = false;

	static float threshold_ev3_side = 0.14f;
	static int positionWall;
	static int configurationInitial;
	static int orientation; //orientation of the robot: 0:i, 1: <-, 2: ->, 3: !
	static boolean longside;

	static int position_x;
	static int position_y;

	static int n_obstacles; //expected 6

	static DataOutputStream dataOutputStream;

	static Mapping_PC map;

	private static boolean mapping_done = false;

	private static float left_distance;

	private static float right_distance;

	private static boolean first_time;

	private static boolean obstacle;

	private boolean start;

	private static boolean stop = false;

	private static int[][] map_to_save;

	private Socket client;
	private ServerSocket serverSocket;
	private OutputStream outputStream;

	public Mapping_Robot(Robot robot, int configuration)
	{
		Mapping_Robot.pilot = robot.getPilot();
		Mapping_Robot.graphicsLCD = robot.getLCD();

		Mapping_Robot.ultrasonic_up = robot.getUltrasonic_up();
		Mapping_Robot.ultrasonic_down = robot.getUltrasonic_down();
		Mapping_Robot.gyroSensor = robot.getGyroSensor();
		Mapping_Robot.colorSensor = robot.getColorSensor();

		Mapping_Robot.motor_ultrasonic = robot.getUltrasonicMotor();
		Mapping_Robot.motor_left = robot.getLeftMotor();
		Mapping_Robot.motor_right = robot.getRightMotor();

		configurationInitial = configuration;
		reverse = false;
		first_time=true;

		Mapping_Robot.colorAdapter = new ColorAdapter(colorSensor);
		pilot.setRotateSpeed(200);
		pilot.setTravelSpeed(100);
		pilot.setAcceleration(50);

		sampleProvider_down = ultrasonic_down.getDistanceMode();
		sampleProvider_up = ultrasonic_up.getDistanceMode();
		sampleProviderFront = sampleProvider_down;
		sampleProviderSide = sampleProvider_up;
		green = false;
		red = false;
		orientation = 0;
		longside = false;
		start = true;
		n_obstacles = 0;
		obstacle = false;
		map_to_save = new int[3][3];

		for (int i=0; i<map_to_save.length; i++){
			for (int j=0; j<map_to_save[0].length; j++)
				map_to_save[i][j] = 0;
		}

		if(configurationInitial == -1){
			changeConfiguration();
		}
	}

	/*TODO: maybe different mapping 
	 * go to shorter side
	 * map both obstacles at same time
	 * go further if red and green have not been found
	 */
	public void connect(boolean stop){
		try {
			Mapping_Robot.stop = stop;
			serverSocket = new ServerSocket(1234);
			graphicsLCD.clear();
			graphicsLCD.drawString("Open the map.", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.drawString("Then press enter.", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);

			Button.waitForAnyPress(0);
			//			if(!stop && Button.DOWN.isDown())
			client = serverSocket.accept();
			outputStream = client.getOutputStream();
			dataOutputStream = new DataOutputStream(outputStream);
			serverSocket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void locate(boolean stop) throws IOException{
		Mapping_Robot.stop = stop;
		if(!Mapping_Robot.stop){
			left_distance = getUltrasonicSensorValue(sampleProviderSide); //worst case: 0.90, 0.90, 0.91, 0.65
			changeConfiguration();
			right_distance = getUltrasonicSensorValue(sampleProviderSide); // worst case: 0.44, 0.8, 1.06, 1.07
			changeConfiguration();
			orientation = 0;
			colorUpdate();
			reverse = false; //if the maze is reversed

			if (right_distance >= 0.90){
				reverse = true;
				position_x = 2*tile_length;
				position_y = 5*tile_length;
			}

			else{
				position_x = 3*tile_length;
				position_y = 5*tile_length;
			}
			send_variables_to_PC();
		}
	}

	public void move(boolean stop) throws IOException{
		Mapping_Robot.stop = stop;
		pilot.setAcceleration(50);
		pilot.setTravelSpeed(100);

		boolean finished = false;

		while (!finished && !stop){
			motionUpdate();
			finished = checkfinish();
		}

	}

	private boolean checkfinish() throws IOException {
		if (green==false | red==false | mapping_done==false)
			return false;
		else{
			map_to_save[2][0] = position_x;
			map_to_save[2][1] = position_y;
			map_to_save[2][2] = orientation;
			String filename = "Map.txt";
			File file = new File(filename);
			try(BufferedWriter writer = new BufferedWriter(new FileWriter(filename))){
				if(!file.exists()){
					file.createNewFile();
				}
				do{
					String str  = "";
					for(int i = 0; i < map_to_save.length; i++){
						for(int j = 0; j < map_to_save.length; j++){
						}
						writer.write(str);
					}
				}
				return true;
			}
		}

		//TODO
		private void motionUpdate() throws IOException{
			int n_turns = 4;
			int tiles = 0;
			boolean updateSensors = true;
			if(!stop){
				if (orientation==0) { //starting position
					if (reverse){
						tiles = 2;
					}
					else{
						tiles = 1;
					}
					rotate_via_gyro(-90);	
					goForward(tile_length*tiles, 1, false);

					rotate_via_gyro(90);
					changeConfiguration();
					goForward(tile_length, 1, updateSensors);

					//locate
					right_distance = getUltrasonicSensorValue(sampleProviderSide);
					changeConfiguration();
					left_distance = getUltrasonicSensorValue(sampleProviderSide);
					changeConfiguration();
					motor_ultrasonic.rotate(90);
					float back_distance = getUltrasonicSensorValue(sampleProviderSide);
					motor_ultrasonic.rotate(-90);



					orientation = 1;
					return;
				}

				else if (orientation == 1){
					tiles = 3;
				}
				else if (orientation == 2 | orientation == 3){
					tiles = 3;
				}
				else if (orientation == 4 | orientation == 5 ){
					tiles = 2;
				}
				else if (orientation == 6 | orientation == 7  ){
					tiles = 1;
				}
				moveAlongWall(tiles, updateSensors);
			}


		}

		private void moveAlongWall(int tiles, boolean updateSensors) throws IOException {

			float distance = tiles*tile_length;
			if(updateSensors)
				if(!stop)
					goForward(distance, tiles, true);
				else
					if(!stop)
						goForward(distance, tiles, false);
			if(!stop){
				rotate_via_gyro(90);
				colorUpdate();
				if(updateSensors)
					if(sensorUpdate()){
						obstacle = true;
						n_obstacles++;
						Sound.beep();
					}
				send_variables_to_PC();
				obstacle = false;
			}

		}

		private static void send_variables_to_PC() throws IOException{
			dataOutputStream.writeInt(orientation);
			dataOutputStream.writeInt(position_x);
			dataOutputStream.writeInt(position_y);
			dataOutputStream.writeBoolean(obstacle);
			dataOutputStream.writeBoolean(red); //Red
			dataOutputStream.writeBoolean(green); //Green
			dataOutputStream.flush();

		}

		public static void goForward(float distance, int n, boolean updateSensors) throws IOException{

			float substep = distance/n;
			for (int i=0; i<n && !stop; i++){
				float first = getGyroValue();
				if(!stop)
					pilot.travel(substep);
				pilot.stop();
				float second = getGyroValue();
				if(!stop)
					fix_rotation(0, second-first);

				positionUpdate(substep);
				if(updateSensors){
					if(sensorUpdate()){
						obstacle = true;
						n_obstacles++;
						Sound.beep();
					}
				}
				colorUpdate();
				enoughObstaclesUpdate();
				send_variables_to_PC();
				obstacle = false;


			}
		}

		private static boolean sensorUpdate() {
			boolean obstacle_detected = false;
			right_distance = getUltrasonicSensorValue(sampleProviderSide);
			if(right_distance < 0.3){
				obstacle_detected = true;
			}

			return obstacle_detected;
		}

		private static void enoughObstaclesUpdate() {
			if(n_obstacles == 6)
				mapping_done = true;
		}

		private static void colorUpdate() {
			if (getColor(1) > 40){
				Sound.beepSequence();
				map_to_save[0][0] = position_x;
				map_to_save[0][1] = position_y;
				map_to_save[0][2] = orientation;
				green = true;
			}

			if (getColor(0) > 50){
				Sound.beepSequence();
				map_to_save[1][0] = position_x;
				map_to_save[1][1] = position_y;
				map_to_save[1][2] = orientation;
				red = true;
			}
		}

		private static void positionUpdate(float substep) {
			int o = orientation;
			switch(orientation){
			case 0: {
				if(first_time){
					position_x+=substep;
					first_time=false;
				}else{
					position_y-=substep;
				}
			}
			break;
			case 1: position_y -= substep;
			break;
			case 2: position_x -= substep;
			break;
			case 3: position_y += substep;
			break;
			case 4: position_x += substep;
			break;
			case 5: position_y -= substep;
			break;
			case 6: position_x -= substep;
			break;
			case 7: position_y += substep;
			}
		}

		public static int[][] getMap(){
			int[][] i = {{1,1,1},{2,2,2},{3,3,3}};
			return i;
			//return map_to_save;
		}


		public static int getColor(int i){
			Color color = colorAdapter.getColor();
			int[] colors = new int[2];
			graphicsLCD.clear();
			graphicsLCD.drawString("R : " + color.getRed(), 10, 20 , GraphicsLCD.VCENTER|GraphicsLCD.LEFT); // red tile: R~60
			graphicsLCD.drawString("G : " + color.getGreen(), 10, 40 , GraphicsLCD.VCENTER|GraphicsLCD.LEFT); //green tile: G~45
			graphicsLCD.drawString("B : " + color.getBlue(), 10, 60 , GraphicsLCD.VCENTER|GraphicsLCD.LEFT);

			colors[0] = color.getRed();
			colors[1] = color.getGreen();
			return colors[i];
		}

		//rotates turn_angle degrees and calls fix_rotation method
		public static void rotate_via_gyro(float turn_angle){
			if(!stop){
				if(orientation!=0)
					orientation++;
				float first = getGyroValue();
				//SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
				float angle = 0;
				float angle2;
				//while (Button.readButtons() != Button.ID_ESCAPE) {

				/*if(sampleProvider.sampleSize() > 0) {
				float [] sample2 = new float[sampleProvider.sampleSize()];
		    	sampleProvider.fetchSample(sample2, 0);
				angle2 = sample2[0];*/


				pilot.rotate(turn_angle);
				float second = getGyroValue();
				graphicsLCD.clear();
				graphicsLCD.drawString("angle: "+ turn_angle, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);

				graphicsLCD.drawString("angle to fix: "+ (second-first), graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
				fix_rotation(turn_angle, (second-first));
				//}
				//Thread.yield();
			}
		}

		static float getUltrasonicSensorValue(SampleProvider sampleProvider) {
			//SampleProvider sampleProvider = ultrasonic_down.getDistanceMode();
			if(sampleProvider.sampleSize() > 0) {
				float [] samples = new float[sampleProvider.sampleSize()];
				sampleProvider.fetchSample(samples, 0);
				return samples[0];
			}
			return -1;		
		}
		//fix rotation error
		public static void fix_rotation(float turn_angle, float angle2){
			boolean finish = false;
			motor_left.resetTachoCount();
			motor_right.resetTachoCount();
			if(!stop){
				motor_left.rotateTo(0);
				motor_right.rotateTo(0);
			}
			motor_left.setAcceleration(800);
			motor_right.setAcceleration(800);

			motor_left.setSpeed(20);
			motor_right.setSpeed(20);
			//SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
			float first = getGyroValue();
			float second = 0;

			if(!stop){
				while (!finish ) {
					second = getGyroValue();
					if(second-first > turn_angle-angle2 && !stop) {
						motor_left.forward();
						motor_right.backward();
					}
					else if(second-first < turn_angle-angle2 && !stop) {
						motor_left.backward();
						motor_right.forward();
					}
					else {
						motor_left.stop(true);
						motor_right.stop(true);
						finish = true;
					}

					Delay.msDelay(10);
				}
				//Thread.yield();
			}
			graphicsLCD.drawString("angle fix: "+ (second-first), graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2+20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);

		}
		public static float getGyroValue() {
			float angle = 0;
			SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
			if(sampleProvider.sampleSize() > 0) {

				float [] sample = new float[sampleProvider.sampleSize()];
				sampleProvider.fetchSample(sample, 0);
				angle = sample[0];
			}
			Thread.yield();
			return angle;
		}

		/**
		 * configuration of ultrasonic-sensors. Initial: 1 (means wall is left of the robot)
		 */
		public static void changeConfiguration(){

			if (configurationInitial == -1 && !stop){
				motor_ultrasonic.rotate(-90);
				sampleProviderFront = sampleProvider_down;
				sampleProviderSide = sampleProvider_up;
				threshold_side = threshold_nxt_side;
				configurationInitial = 1;
				positionWall = -1*configurationInitial;

			}else if (configurationInitial == 1 && !stop){
				motor_ultrasonic.rotate(90);
				sampleProviderFront = sampleProvider_up;
				sampleProviderSide = sampleProvider_down;
				configurationInitial = -1;
				positionWall = -1*configurationInitial;
				threshold_side = threshold_ev3_side;
			}
		}

		public void stop(boolean suppressed) throws IOException {
			Mapping_Robot.stop = suppressed;
			client.close();
			serverSocket.close();
			outputStream.close();
			dataOutputStream.close();


		}

	}