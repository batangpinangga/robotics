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

	static DataOutputStream dataOutputStream;

	private boolean mapping_done;

	private boolean start;

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

		if(configurationInitial == -1){
			changeConfiguration();
		}

		side = getUltrasonicSensorValue(sampleProviderSide);
		front = getUltrasonicSensorValue(sampleProviderFront);

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

	public void locate(){
		float side_left = side;
		orientation = 0;
		colorUpdate();
		reverse = false; //if the maze is reversed
		
		if (side_left < 0.90){
			reverse = true;
			position_x = 66;
			position_y = 4*33;
		}
		
		else{
			position_x = 99;
			position_y = 4*33;
		}
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
		else
			return true;
	}

	//TODO
	private void motionUpdate() throws IOException{
		int n_turns = 4;
		int o = orientation;
		
		if (orientation==0) { //starting position
			goForward(tile_length, 1);
			rotate_via_gyro(90);	

			if (reverse){
				goForward(tile_length, 1);
			}
			else{
				goForward(tile_length*2, 1);
			}
			longside = true;
			start = false;
		}
		
		else if (orientation == 4){
			rotate_via_gyro(-90);
			goForward(tile_length, 1);
			orientation = 1;
			if (green==false | red==false){
				goExtraRound(longside, n_turns);
			}
		}
		
		else{
			moveAlongWall(n_turns);
		}


	}


	private void moveAlongWall(int n_turns) throws IOException {
		rotate_via_gyro(-90);			
		longside = !longside;
		int tiles = 3;
		float distance = tiles*tile_length;
		goForward(distance, 3);
	}

	private void goExtraRound(boolean longside, int n_turns) {
		longside = false;
		for(int i = 0; i<n_turns; i++){
			int tiles = 0;
			if (longside){
				tiles = 2;
			}
			else
				tiles = 1;

			float distance = tiles*tile_length;
			longside = !longside;

			try{
				goForward(distance, tiles);
			}
			catch(IOException e){
				e.printStackTrace();
			}
			rotate_via_gyro(-90);			
		}
		mapping_done = true;

	}

	private void send_variables_to_PC(int distance){

	}


	public static void goForward(float distance, int n) throws IOException{
		positionUpdate(0);
		float substep = distance/n;
		for (int i=0; i<n; i++){
			float first = getGyroValue();
			pilot.travel(substep);
			positionUpdate(substep);
			colorUpdate();
			float second = getGyroValue();
			fix_rotation(0, second-first);

			while(pilot.isMoving()){
				graphicsLCD.clear();
				graphicsLCD.drawString("x: " + position_x + " ,y: " + position_y, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2+20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
				dataOutputStream.writeInt(orientation);
				dataOutputStream.writeInt(position_x);
				dataOutputStream.writeInt(position_y);
				dataOutputStream.writeFloat(getUltrasonicSensorValue(sampleProviderSide));
				dataOutputStream.writeFloat(getUltrasonicSensorValue(sampleProviderFront));
				dataOutputStream.writeBoolean(red); //Red
				dataOutputStream.writeBoolean(green); //Green
				dataOutputStream.flush();

			}
		}
	}

	private static void colorUpdate() {
		if (getColor(1) > 40){
			Sound.beepSequence();
			green = true;
		}
		
		if (getColor(0) > 50){
			Sound.beepSequence();
			red = true;
		}
	}

	private static void positionUpdate(float substep) {
		switch(orientation){
		case 1: position_x -= substep;
		break;
		case 2: position_y -= substep;
		break;
		case 3: position_x += substep;
		break;
		case 4: position_y += substep;
		break;
		}
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
