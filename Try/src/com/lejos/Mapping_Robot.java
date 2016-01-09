package com.lejos;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.Color;
import lejos.robotics.ColorAdapter;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;

public class Mapping_Robot {
	static int tile_length = 33;

	static EV3UltrasonicSensor ultrasonic_up;
	static NXTUltrasonicSensor ultrasonic_down;
	static NXTRegulatedMotor motor_ultrasonic;
	static EV3GyroSensor gyroSensor;
	static EV3ColorSensor colorSensor;
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

	private static boolean mapping_done = false;

	private static float left_distance;

	private static float right_distance;

	private static boolean first_time;

	private static boolean obstacle;

	private boolean start;

	private static int[][] map_to_save;

	public Mapping_Robot(EV3UltrasonicSensor ultrasonic_up,
			NXTUltrasonicSensor ultrasonic_down,
			EV3ColorSensor colorSensor,
			NXTRegulatedMotor motor_ultrasonic,
			EV3LargeRegulatedMotor motor_left,
			EV3LargeRegulatedMotor motor_right,
			GraphicsLCD graphicsLCD,
			DifferentialPilot pilot, 
			EV3GyroSensor gyroSensor,
			int configuration)
	{
		this.ultrasonic_up = ultrasonic_up;
		this.ultrasonic_down = ultrasonic_down;
		this.motor_ultrasonic = motor_ultrasonic;
		this.gyroSensor = gyroSensor;
		this.colorSensor = colorSensor;
		this.motor_left = motor_left;
		this.motor_right = motor_right;
		this.graphicsLCD = graphicsLCD;
		this.pilot =  pilot;
		configurationInitial = configuration;
		reverse = false;
		first_time=true;

		this.colorAdapter = new ColorAdapter(colorSensor);
		pilot.setRotateSpeed(200);
		pilot.setTravelSpeed(35);
		pilot.setAcceleration(15);

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
		map_to_save = new int[2][3];
		
		for (int i=0; i<map_to_save.length; i++){
			for (int j=0; j<map_to_save[0].length; j++)
				map_to_save[i][j] = 0;
		}

		if(configurationInitial == -1){
			changeConfiguration();
		}

		try {
			ServerSocket serverSocket = new ServerSocket(1234);
			Socket client = serverSocket.accept();
			OutputStream outputStream = client.getOutputStream();
			dataOutputStream = new DataOutputStream(outputStream);
			serverSocket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*TODO: maybe different mapping 
	 * go to shorter side
	 * map both obstacles at same time
	 * go further if red and green have not been found
	 */
	public void locate() throws IOException{
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

	public void move() throws IOException{
		pilot.setAcceleration(5);
		pilot.setTravelSpeed(10);

		boolean finished = false;

		while (!finished){
			motionUpdate();
			finished = checkfinish();
		}

	}

	private boolean checkfinish() {
		if (green==false | red==false | mapping_done==false)
			return false;
		else{
			map_to_save[2][0] = position_x;
			map_to_save[2][1] = position_y;
			map_to_save[2][2] = orientation;
			return true;
		}
	}

	//TODO
	private void motionUpdate() throws IOException{
		int n_turns = 4;
		int tiles = 0;
		boolean updateSensors = true;

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

	private void moveAlongWall(int tiles, boolean updateSensors) throws IOException {
		float distance = tiles*tile_length;
		if(updateSensors)
			goForward(distance, tiles, true);
		else
			goForward(distance, tiles, false);

		rotate_via_gyro(90);
		if(updateSensors)
			if(sensorUpdate()){
				n_obstacles++;
				Sound.beep();
			}
		send_variables_to_PC();
	}

	private static void send_variables_to_PC() throws IOException{
		dataOutputStream.writeInt(orientation);
		dataOutputStream.writeInt(position_x);
		dataOutputStream.writeInt(position_y);
		dataOutputStream.writeBoolean(sensorUpdate());
		//dataOutputStream.writeFloat(getUltrasonicSensorValue(sampleProviderSide));
		//dataOutputStream.writeFloat(getUltrasonicSensorValue(sampleProviderFront));
		dataOutputStream.writeBoolean(red); //Red
		dataOutputStream.writeBoolean(green); //Green
		dataOutputStream.flush();

	}

	public static void goForward(float distance, int n, boolean updateSensors) throws IOException{

		float substep = distance/n;
		for (int i=0; i<n; i++){
			float first = getGyroValue();
			pilot.travel(substep);
			float second = getGyroValue();
			fix_rotation(0, second-first);

			positionUpdate(substep);
			colorUpdate();
			if(updateSensors){
				if(sensorUpdate()){
					n_obstacles++;
					Sound.beep();
				}
			}
			enoughObstaclesUpdate();
			send_variables_to_PC();


		}
	}

	private static boolean sensorUpdate() {
		boolean obstacle_detected = false;
		right_distance = getUltrasonicSensorValue(sampleProviderSide);
		if(right_distance < 0.3){
			map_to_save[position_x/tile_length][position_y/tile_length] = 3;
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
	
	public int[][] getMap(){
		return map_to_save;
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

	public static void rotate_via_gyro(float turn_angle){
		if(orientation!=0)
			orientation++;
		float first = getGyroValue();
		SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
		float angle = 0;
		float angle2;
		//while (Button.readButtons() != Button.ID_ESCAPE) {

		if(sampleProvider.sampleSize() > 0) {
			float [] sample2 = new float[sampleProvider.sampleSize()];
			sampleProvider.fetchSample(sample2, 0);
			angle2 = sample2[0];


			pilot.rotate(turn_angle);
			float second = getGyroValue();
			graphicsLCD.clear();
			fix_rotation(turn_angle, (second-first));
		}
		Thread.yield();
	}

	//fix rotation error
	public static void fix_rotation(float turn_angle, float angle2){
		boolean finish = false;
		motor_left.resetTachoCount();
		motor_right.resetTachoCount();

		motor_left.rotateTo(0);
		motor_right.rotateTo(0);
		motor_left.setAcceleration(800);
		motor_right.setAcceleration(800);

		motor_left.setSpeed(10);
		motor_right.setSpeed(10);
		SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
		float first = getGyroValue();
		float second = 0;

		while (!finish ) {
			second = getGyroValue();
			if(second-first > turn_angle-angle2) {
				motor_left.forward();
				motor_right.backward();
			}
			else if(second-first < turn_angle-angle2) {
				motor_left.backward();
				motor_right.forward();
			}
			else {
				motor_left.stop(true);
				motor_right.stop(true);
				finish = true;
			}

			Delay.msDelay(10);

			Thread.yield();
		}
		graphicsLCD.drawString("angle fix: "+ (second-first), graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2+20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		Delay.msDelay(100);
	}
	public static float getGyroValue() {
		float angle = 0;
		SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
		if(sampleProvider.sampleSize() > 0) {

			float [] sample = new float[sampleProvider.sampleSize()];
			sampleProvider.fetchSample(sample, 0);
			angle = sample[0];
		}
		return angle;
	}

	/**
	 * configuration of ultrasonic-sensors. Initial: 1 (means wall is left of the robot)
	 */
	public static void changeConfiguration(){

		if (configurationInitial == -1){
			motor_ultrasonic.rotate(-90);
			sampleProviderFront = sampleProvider_down;
			sampleProviderSide = sampleProvider_up;
			threshold_side = threshold_nxt_side;
			configurationInitial = 1;
			positionWall = -1*configurationInitial;

		}else if (configurationInitial == 1){
			motor_ultrasonic.rotate(90);
			sampleProviderFront = sampleProvider_up;
			sampleProviderSide = sampleProvider_down;
			configurationInitial = -1;
			positionWall = -1*configurationInitial;
			threshold_side = threshold_ev3_side;
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
}
