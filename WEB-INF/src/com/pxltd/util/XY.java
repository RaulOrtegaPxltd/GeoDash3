package com.pxltd.util;

public class XY {
	public double x;
	public double y;
	
	public XY(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public double mercNormX() {
		return this.x + 0.5;
	}
	
	public double mercNormY() {
		return Math.abs(this.y - 0.5);
	}
}
