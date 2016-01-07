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

public class Mapping_PC extends JFrame{
	private static int distance;
	private static int turn;
	private static int travel;
	private static int start;
	private static int pred_travel;
	private static int x_offset = 200;
	private static int y_offset = 200;
	static int maze_length = 198;
	static int tile_length = 33;
	int maze_map;
	int tile_map;
	int zoom;
	private static int width = maze_length*3;
	private static int height = maze_length*3;
	private static int stepwidth = 5;
	private int expected_distance_obstacle;


	static InputStream inputStream;
	static DataInputStream dataInputStream;

	public Mapping_PC (){
		super("Map_Miner_Robot");
		setSize(width,height);
		setVisible(true);
		distance = 0;
		turn = 0;
		travel = 0;
		start = 0;
		pred_travel = 0;
		zoom = 3;
		maze_map = maze_length*zoom;
		tile_map = tile_length*zoom;
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
		//Test
		drawColorTile(g, Color.green, 15, 17); 
		drawColorTile(g, Color.red, 50, 160);
		drawObstacle(g, Color.black, 33, 66);
		g.setColor(Color.black);
		
		if(distance <= expected_distance_obstacle){ //TODO:Define expected_distance_obstacle
			switch(turn){
			case 1: g.fillRect(-50+distance*stepwidth, 550-travel*stepwidth , 5, 5); //TODO: put right equations for Miner_Robot
			break;
			case 2: g.fillRect(0+travel*stepwidth, -50+distance*stepwidth , 5, 5);
			break;
			case 3: g.fillRect(650-distance*stepwidth, 50+travel*stepwidth, 5, 5);
			break;
			case 4: g.fillRect(550-travel*stepwidth, 650-distance*stepwidth, 5, 5);
			}
		}
		
	}

	public void displayRobot(Graphics g){
		Graphics2D g2 = ( Graphics2D ) g;
		g2.setPaint( Color.blue );
		g2.setStroke( new BasicStroke( 5.0f ));
		switch(turn){
		case 1: g2.draw(new Line2D.Double((int) x_offset, (int) y_offset+travel*stepwidth, (int) 5, (int) 5));
		break;
		case 2: g2.draw(new Line2D.Double((int) x_offset + travel*stepwidth, (int) height-y_offset, (int) 5, (int) 5));
		break;
		case 3: g2.draw(new Line2D.Double((int) width-x_offset, (int) (height-y_offset-travel)*stepwidth, (int) 5, (int) 5)); 
		break;
		case 4: g2.draw(new Line2D.Double((int) (width-x_offset-travel)*stepwidth, (int)y_offset, (int)5, (int)5));
		break;
		}	
	}
	
	public void drawColorTile(Graphics g, Color color, int x, int y){
		g.setColor(color);
		g.fillRect(x*zoom, y*zoom, tile_map, tile_map); //TODO: check the positions. Where is 0? Where is 198? What is the reference point?
	}
	
	public void drawObstacle(Graphics g, Color color, int x, int y){
		g.setColor(color);
		g.fillRect(x*zoom, y*zoom, tile_map, tile_map); //TODO: check the positions. Where is 0? Where is 198? What is the reference point?
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
				pred_travel = travel;
				travel = dataInputStream.readInt();
				distance = (int) dataInputStream.readFloat()*100;
				turn = dataInputStream.readInt();
				System.out.println(travel + " " + distance);
			}
			catch(Exception e){

			}
			monitor.repaint();
		}
	}
}

