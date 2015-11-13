package com.pxltd.util;

public class Bbox {
	private double tlLat;
	private double tlLng;
	private double brLat;
	private double brLng;
	
	public Bbox(double tlLat, double tlLng, double brLat, double brLng) {
		this.tlLat = tlLat;
		this.tlLng = tlLng;
		this.brLat = brLat;
		this.brLng = brLng;
	}
	
	public void extend(Bbox bbox){
		this.tlLat = Math.max(this.tlLat, bbox.tlLat);
		this.tlLng = Math.min(this.tlLng, bbox.tlLng);
		this.brLat = Math.min(this.brLat, bbox.brLat);
		this.brLng = Math.max(this.brLng, bbox.brLng);
	}

	public double getTlLat() {
		return tlLat;
	}
	
	public void setTlLat(double tlLat) {
		this.tlLat = tlLat;
	}
	
	public double getTlLng() {
		return tlLng;
	}
	
	public void setTlLng(double tlLng) {
		this.tlLng = tlLng;
	}
	
	public double getBrLat() {
		return brLat;
	}
	
	public void setBrLat(double brLat) {
		this.brLat = brLat;
	}
	
	public double getBrLng() {
		return brLng;
	}
	
	public void setBrLng(double brLng) {
		this.brLng = brLng;
	}
}
