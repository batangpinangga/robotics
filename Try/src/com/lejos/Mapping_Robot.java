package com.lejos;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

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

	static float threshold_ev3_side = 0.14f;
	static int positionWall;
	static int configurationInitial;
	
	static DataOutputStream dataOutputStream;

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

		this.colorAdapter = new ColorAdapter(colorSensor);
		pilot.setRotateSpeed(200);
		pilot.setTravelSpeed(35);
		pilot.setAcceleration(15);

		sampleProvider_down = ultrasonic_down.getDistanceMode();
		sampleProvider_up = ultrasonic_up.getDistanceMode();
		sampleProviderFront = sampleProvider_down;
		sampleProviderSide = sampleProvider_up;
		
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

	public void move() throws IOException{
		//motor_left.synchronizeWith(new RegulatedMotor[]{motor_right});
		//bring robot in starting position 
		//rotate_via_gyro(90); 
		//pilot.travel(2*tile_length);
		int n_turns = 4;
		boolean longside = false;

		for(int i = 0; i<n_turns; i++){
			rotate_via_gyro(-90);			
			longside = !longside;
			int tiles = 0;
			if (longside){
				tiles = 4;
			}
			else
				tiles = 3;
			
			int distance = tiles*tile_length;
			pilot.travel(distance);
			
			while(pilot.isMoving()){
				dataOutputStream.writeInt(distance);
				dataOutputStream.flush();

				dataOutputStream.writeFloat(getUltrasonicSensorValue(sampleProviderSide));
				dataOutputStream.flush();

				dataOutputStream.writeFloat(getUltrasonicSensorValue(sampleProviderFront));
				dataOutputStream.flush();
			}
		}
	}

	private void send_variables_to_PC(){

	}

	public static void goForward(int direction, float distance){
		pilot.forward();
		//pilot.travel(distance*direction);
		SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
		while(pilot.isMoving()){
			float [] sample = new float[sampleProvider.sampleSize()];
			sampleProvider.fetchSample(sample, 0);
			float angle = sample[0];
			if(angle>=1){
				pilot.stop();
				fix_rotation(0, angle);
				pilot.travel(33);
			}
			Thread.yield();	
		}
	}

	private static float getSideValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void getColor(){
		Color color = colorAdapter.getColor();

		graphicsLCD.clear();
		graphicsLCD.drawString("R : " + color.getRed(), 10, 20 , GraphicsLCD.VCENTER|GraphicsLCD.LEFT); // red tile: R~60
		graphicsLCD.drawString("G : " + color.getGreen(), 10, 40 , GraphicsLCD.VCENTER|GraphicsLCD.LEFT); //green tile: G~45
		graphicsLCD.drawString("B : " + color.getBlue(), 10, 60 , GraphicsLCD.VCENTER|GraphicsLCD.LEFT);
	}

	public static void rotate_via_gyro(float turn_angle){
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
