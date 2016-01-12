package com.lejos;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.Color;
import lejos.robotics.ColorAdapter;
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
	static ColorAdapter colorAdapter;


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
		this.colorAdapter = new ColorAdapter(colorSensor);

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
		int turn_degree = 0;
		lastorientation = map[2][2];
		if(lastorientation ==1 | lastorientation ==5){
			turn_degree = -90;
		}else if(lastorientation ==2 | lastorientation ==6){
			turn_degree = 180;
		}else if(lastorientation ==3 | lastorientation ==7){
			turn_degree = 90;
		}
		if(turn_degree!=0)
			rotate_via_gyro(turn_degree);
		gooo(turn_degree,0, 1);
		gooo(turn_degree,0, 2);
		//gooo(turn_degree, 2);
		//goToGreen(goToRed());
		//closeGrabber();
	}

	private void gooo(int turn_degree,int extra, int step) {
		// TODO Auto-generated method stub
		if(step ==1){
			int degreee = 0;
			int dist_x = map[1][0]-map[2][0];
			goForward(dist_x, Math.abs(dist_x/tile_length));
			rotate_via_gyro(90);
			int dist_y = map[2][1]-map[1][1];
			goForward(dist_y, Math.abs(dist_y/tile_length));
			//if(!(getColor(0)> 50)){
			//	degreee =colorDetection(1);
			//}
			//colorDetection();
			Sound.beepSequenceUp();
			Button.waitForAnyEvent();
			openGrabber();
			
			

		}else if(step == 2){
			if(extra!=0)
				rotate_via_gyro(-1*extra);
			Delay.msDelay(100);
			Button.waitForAnyEvent();
			rotate_via_gyro(-90);
			Delay.msDelay(100);
			int dist_x = map[0][0]-map[1][0];
			
			goForward(dist_x, Math.abs(dist_x/tile_length));
			rotate_via_gyro(90);
			Delay.msDelay(100);
			int dist_y = map[1][1]-map[0][1];
			
			goForward(dist_y, Math.abs(dist_y/tile_length));
			//if(!(getColor(1)> 40)){
				//int degreee =colorDetection(2);
			//}
			closeGrabber();
			Sound.beepSequenceUp();
		}
		//goForward(, n);
	}

	private int colorDetection(int step) {
		
		// TODO Auto-generated method stub
		boolean finished = false;
		int result = 0;
		while((step == 1 && getColor(0)>50) | (step==2 && getColor(1)>40)){
			if(result <360){
				result +=90;
				rotate_via_gyro(90);
			}
		}
		return result;
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
		goGreen(1,0,tiles);
		//goToTile(1,0, tiles);
	}


	private void goGreen(int current, int goal, int tiles) {
		int diff = getDifference(current, goal)+1; //robot-red, red-green
		goForward(tiles, tile_length);
		positionUpdate(-1*tiles);
		rotate_via_gyro(90);
		lastorientation++;
		if(lastorientation-goal==0){
			if(position_y-map[goal][1]==0){
				diff = position_x-map[goal][0];
				goForward(Math.abs(diff), diff/tile_length);
				openGrabber();
				Sound.beepSequence();
			}

			else if (position_x-map[goal][0]==0){
				diff = position_y-map[goal][1];
				goForward(Math.abs(diff), diff/tile_length);
				openGrabber();
				Sound.beepSequence();
			}
		}else if(diff<0){
			for(int i = 0; i < Math.abs(diff)-1;i++){
				int dist =  getDistance(lastorientation);
				goForward(dist, dist/tile_length);
				positionUpdate(-1*dist);
				rotate_via_gyro(90);
				lastorientation++;
			}
			
			if(position_y-map[goal][1]==0){
				diff = position_x-map[goal][0];
				goForward(Math.abs(diff), diff/tile_length);
				openGrabber();
				Sound.beepSequence();
			}

			else if (position_x-map[goal][0]==0){
				diff = position_y-map[goal][1];
				goForward(Math.abs(diff), diff/tile_length);
				openGrabber();
				Sound.beepSequence();
			}
		}else{
			rotate_via_gyro(90);
			lastorientation--;
			for(int i=0; i<diff-1; i++){
				int distance = getDistance(lastorientation);
				goForward(distance, distance/tile_length);
				rotate_via_gyro(-90);
				positionUpdate(distance);
				lastorientation--;
			}

			if(position_y-map[goal][1]==0){
				diff = position_x-map[goal][0];
				goForward(Math.abs(diff), diff/tile_length);
			}

			else if (position_x-map[goal][0]==0){
				diff = position_y-map[goal][1];
				goForward(Math.abs(diff), diff/tile_length);
			}
			
		}
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
			lastorientation--;
		}

		for(int i=0; i<diff-1; i++){
			int distance = getDistance(lastorientation);
			goForward(distance, distance/tile_length);
			rotate_via_gyro(-90*reverse);
			positionUpdate(distance);
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
		
		rotate_via_gyro(-180);
		Sound.beepSequenceUp();
		Button.waitForAnyPress();
		closeGrabber();
		return Math.abs(diff);
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
		switch(lastorientation){
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
			graphicsLCD.drawString("angle: "+ (second-first), graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			fix_rotation(turn_angle, (second-first));
    		}
    		//Thread.yield();
    }

	public static void fix_rotation(float turn_angle, float angle2){
		boolean finish = false;
		motor_left.resetTachoCount();
		motor_right.resetTachoCount();
		
		motor_left.rotateTo(0);
		motor_right.rotateTo(0);
	    motor_left.setAcceleration(800);
	    motor_right.setAcceleration(800);
	    
	    motor_left.setSpeed(20);
	    motor_right.setSpeed(20);
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
		return angle;
	}
}
