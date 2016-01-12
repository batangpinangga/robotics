package com.lejos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;

import localization.MCL;
import localization.Map;

public class Mapping_PC extends JFrame{	
	private static int start;
	private static int red_x;
	private static int red_y;
	private static int green_x;
	private static int green_y;
	static int maze_length = 198;
	static int tile_length = 33;
	int maze_map;
	int tile_map;
	int zoom;
	static boolean red;
	static boolean red_done;

	static boolean green; 
	static boolean green_done;

	static int orientation;
	private static int width = maze_length*3;
	private static int height = maze_length*3;


	static InputStream inputStream;
	static DataInputStream dataInputStream;
	private static int position_x;
	private static int position_y;
	private static boolean obstacle;
	static int[][] map_to_save;

	public Mapping_PC (){
		super("Map_Miner_Robot");
		setSize(width,height);
		setVisible(true);
		start = 0;
		red_x =0;
		red_y = 0;
		green_x = 0;
		green_y = 0;
		zoom = 3;
		red = false;
		green = false;
		red_done = false;
		green_done = false;
		orientation = 0;
		maze_map = maze_length*zoom;
		tile_map = tile_length*zoom;
	}

	public static void main(String[] Args) throws UnknownHostException, IOException
	{

		Mapping_PC monitor = new Mapping_PC();

		monitor.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		String ip = "10.0.1.1";

		@SuppressWarnings("resource")
		Socket socket = new Socket(ip, 1234);
		System.out.println("Connected!");

		inputStream = socket.getInputStream();
		dataInputStream = new DataInputStream(inputStream);

		while( true ){

			try{
				orientation = dataInputStream.readInt();
				position_x = dataInputStream.readInt();
				position_y = dataInputStream.readInt();
				obstacle = dataInputStream.readBoolean();
				red = dataInputStream.readBoolean();
				green = dataInputStream.readBoolean();

				System.out.println("x: " +  position_x);
				System.out.println("y: " +  position_y);

			}
			catch(Exception e){

			}
			monitor.repaint();
		}
	}


	public void paint(Graphics g){
		setBackground(Color.white);
		displayMap(g);
	}

	public void displayMap(Graphics g){

		if(start == 0){

			g.setColor(Color.gray);
			g.fillRect(0, 0, maze_map, maze_map);
			g.setColor(Color.white);
			for (int i = 0; i<maze_map; i+=tile_map){
				g.drawLine(0, tile_map+i, maze_map, tile_map+i);
				g.drawLine(tile_map+i, 0, tile_map+i, maze_map);
			}
			start = 1;
		}

		else{

			g.setColor(Color.black);

			if (green && !green_done){
				drawColorTile(g, Color.green, position_x, position_y);
				green_y = position_y;
				green_x = position_x;
				green_done = true; //otherwise it will always draw green tiles

			}

			else if (red && !red_done){
				drawColorTile(g, Color.red, position_x, position_y);
				red_done = true;
				red_x = position_x;
				red_y = position_y;
			}
			else 
				displayRobot(g);

			if(obstacle){

				switch(orientation){
				case 0: drawObstacle(g, Color.black, position_x+tile_length, position_y);
				break;
				case 1: drawObstacle(g, Color.black, position_x+tile_length, position_y);
				break;
				case 2: drawObstacle(g, Color.black, position_x, position_y-tile_length);
				break;
				case 3: drawObstacle(g, Color.black, position_x-tile_length, position_y);
				break;
				}

			}
		}

	}

	public void displayRobot(Graphics g){
		g.setColor(Color.blue);
		if(!(position_x==red_x && position_y == red_y) && !(position_x==green_x && position_y == green_y))
			g.fillRect(position_x*zoom, position_y*zoom, tile_map, tile_map);
	}

	public void drawColorTile(Graphics g, Color color, int x, int y){
		g.setColor(color);
		g.fillRect(x*zoom, y*zoom, tile_map, tile_map); //TODO: check the positions. Where is 0? Where is 198? What is the reference point?
	}

	public void drawObstacle(Graphics g, Color color, int x, int y){
		g.setColor(color);
		g.fillRect(x*zoom, y*zoom, tile_map, tile_map); //TODO: check the positions. Where is 0? Where is 198? What is the reference point?
	}

}

