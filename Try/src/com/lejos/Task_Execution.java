package com.lejos;

import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;

import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;

public class Task_Execution {
	static int tile_length = 33;

	static EV3GyroSensor gyroSensor;
	static EV3ColorSensor colorSensor;
	static EV3LargeRegulatedMotor motor_left;
	static EV3LargeRegulatedMotor motor_right;
	static GraphicsLCD graphicsLCD;
	static DifferentialPilot pilot;
	static EV3LargeRegulatedMotor motor_grabber;


	static float side;
	static float front;
	static boolean reverse;
	static boolean green = false;
	static boolean red = false;

	static float threshold_ev3_side = 0.14f;
	static int positionWall;
	static int configurationInitial;
	static int[][] map = new int[3][3]; //map[2][2] of the robot: 0:i, 1: <-, 2: ->, 3: !
	static boolean longside;
	static int lastorientation;

	private static int position_y;

	private static int position_x;

	private static int orientation;

	public Task_Execution(int [][]map,
			EV3ColorSensor colorSensor,
			EV3LargeRegulatedMotor motor_left,
			EV3LargeRegulatedMotor motor_right,
			GraphicsLCD graphicsLCD,
			DifferentialPilot pilot, 
			EV3GyroSensor gyroSensor,
			EV3LargeRegulatedMotor motor_grabber){
		this.colorSensor = colorSensor;
		this.motor_left = motor_left;
		this.motor_right = motor_right;
		this.graphicsLCD = graphicsLCD;
		this.pilot = pilot;
		this.gyroSensor = gyroSensor;
		this.map = map;
		this.motor_grabber = motor_grabber;

		motor_grabber.setSpeed(200);
		lastorientation = map[2][2];
		position_x = map[2][0];
		position_y = map[2][1];
		orientation = map[2][2];
	}

	public int getDirection(){
		int direction = 0;
		if(map[2][2]==2 | map[2][2]==6){
			direction = 1;
		}
		else if(map[2][2]==3 | map[2][2]==7){
			direction = 2;
		}
		else if (map[2][2]==4 ){
			direction = 3;
		}
		else if (map[2][2] == 5){
			direction = 4;
		}
		return direction;
	}

	private int getOrientation(int i){
		return map[i][2];
	}

	public void go(){
		goToGreen(goToRed());
		closeGrabber();
	}

	private void openGrabber() {
		motor_grabber.rotate(-90);
	}

	private void closeGrabber() {
		motor_grabber.rotate(90);
	}

	private int goToRed() {
		int lasttiles=  goToTile(2,1,-1);
		openGrabber();
		return lasttiles;
	}

	private void goToGreen(int tiles) {
		goToTile(1,0, tiles);
	}


	private int goToTile(int current, int goal, int tiles){
		int diff = getDifference(current, goal); //robot-red, red-green
		int reverse = 1;

		if(tiles != -1){
			if(diff < 0){
				rotate_via_gyro(180);
			}	
			reverse = -1;
		}
		else{
			rotate_via_gyro(90);
		}

		for(int i=0; i<diff-1; i++){
			int distance = getDistance(lastorientation-1);
			goForward(distance, distance/tile_length);
			rotate_via_gyro(-90*reverse);
			lastorientation-=reverse;
		}

		if(position_y-map[goal][1]==0){
			diff = position_x-map[goal][0];
			goForward(Math.abs(diff), diff/tile_length);
		}

		else if (position_x-map[goal][0]==0){
			diff = position_y-map[goal][1];
			goForward(Math.abs(diff), diff/tile_length);
		}
		else
			diff = 0;
		return diff;
	}

	public int getDifference(int i, int j){
		return map[i][2]-map[j][2];
	}

	private int getDistance(int orientation) { //i is lastOrientation
		int distance = 0;
		switch(orientation){
		case 1: distance = 3;
		break;
		case 2: distance = 3;
		break;
		case 3: distance = 3;
		break;
		case 4: distance = 2;
		break;
		case 5: distance = 2;
		break;
		case 6: distance = 1; 
		break;
		case 7: distance = 1;
		}
		return distance*tile_length;
	}


	private static void positionUpdate(float substep) {
		switch(orientation){
		case 1: position_y += substep;
		break;
		case 2: position_x += substep;
		break;
		case 3: position_y -= substep;
		break;
		case 4: position_x -= substep;
		break;
		case 5: position_y += substep;
		break;
		case 6: position_x += substep;
		break;
		case 7: position_y -= substep;
		}
	}

	public static void goForward(float distance, int n){

		float substep = distance/n;
		for (int i=0; i<n; i++){
			float first = getGyroValue();
			pilot.travel(substep);
			float second = getGyroValue();
			fix_rotation(0, second-first);
		}

		positionUpdate(distance);
	}

	public static void rotate_via_gyro(float turn_angle){

		SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
		float angle;
		//while (Button.readButtons() != Button.ID_ESCAPE) {
		if(sampleProvider.sampleSize() > 0) {
			float [] sample = new float[sampleProvider.sampleSize()];
			sampleProvider.fetchSample(sample, 0);
			angle = sample[0];


			pilot.rotate(turn_angle);
			graphicsLCD.clear();
			graphicsLCD.drawString("angle: "+ angle, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			fix_rotation(turn_angle, angle);
		}
		Thread.yield();
	}

	public static void fix_rotation(float turn_angle, float angle2){
		boolean finish = false;
		motor_left.resetTachoCount();
		motor_right.resetTachoCount();

		motor_left.rotateTo(0);
		motor_right.rotateTo(0);
		motor_left.setAcceleration(800);
		motor_right.setAcceleration(800);

		motor_left.setSpeed(100);
		motor_right.setSpeed(100);
		SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
		while (!finish ) {

			if(sampleProvider.sampleSize() > 0) {
				float [] sample = new float[sampleProvider.sampleSize()];
				sampleProvider.fetchSample(sample, 0);
				float angle = sample[0];

				graphicsLCD.clear();
				graphicsLCD.drawString("angle: "+ angle, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);

				if(angle > turn_angle-angle2) {
					motor_left.forward();
					motor_right.backward();
				}
				else if(angle < turn_angle-angle2) {
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
			Thread.yield();
		}

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
}
