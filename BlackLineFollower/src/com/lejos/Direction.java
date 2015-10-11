package com.lejos;

public enum Direction {
	RIGHT("right"), 
	LEFT("left"), 
	FORWARD("forward"), 
	NONE("none");
	private String dir;
	Direction(String dir){
		this.dir = dir;
	}
	public String getValue(){
		return dir;
	}
}
