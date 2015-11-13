package com.pxltd.spatial;

import java.util.ArrayList;
import java.util.Iterator;

import com.microstrategy.web.app.tasks.architect.json.JSONArray;
import com.pxltd.service.EnumGeoStatusCode;


public class Points {
	private ArrayList<Point> points = new ArrayList<Point>();
	private int numValidPoints = 0;
	private int numInvalidPoints = 0;
	private double maxLatitude = 1000;
	private double minLatitude = 1000;
	private double maxLongitude = 1000;
	private double minLongitude = 1000;
	
	
	/**
	 * @return the records
	 */
	public ArrayList<Point> getPoints() {
		return points;
	}
	
	public void addPoint(int pos, Point pt){
		points.add(pos,pt);
		if(pt.getState() == EnumGeoStatusCode.OK){
			numValidPoints++;
		}else{
			numInvalidPoints++;
		}
	}
	
	public void addPoint(Point pt){
		points.add(pt);
		if(pt.getState() == EnumGeoStatusCode.OK){
			numValidPoints++;
		}
	}
	
	public boolean hasValidPoints(){
		if (numValidPoints>0){
			return true;
		}else{
			return false;
		}
	}

	public double getMaxLatitude(){
		Iterator<Point> it = points.iterator();
		
		//main point here is that we set the value if it is using default 1000
		while(it.hasNext()){
			Point pt = (Point) it.next();
			if (pt.getState() == EnumGeoStatusCode.OK){
				double tempLat = pt.getLatitude();
				if (tempLat > maxLatitude || maxLatitude == 1000){
					maxLatitude = tempLat;
				}
			}
		}
		return maxLatitude;		
	}
	
	public double getMinLatitude(){
		Iterator<Point> it = points.iterator();
		
		while(it.hasNext()){
			Point pt = (Point) it.next();
			if (pt.isGeocoded()){
				double tempLat = pt.getLatitude();
				if (tempLat < minLatitude || minLatitude == 1000){
					minLatitude = tempLat;
				}
			}
		}
		return minLatitude;	
	}
	
	public double getMaxLongitude(){
		Iterator<Point> it = points.iterator();
		
		while(it.hasNext()){
			Point pt = (Point) it.next();
			if (pt.isGeocoded()){
				double tempLng = pt.getLongitude();
				if (tempLng > maxLongitude || maxLongitude == 1000){
					maxLongitude = tempLng;
				}
			}
		}
		return maxLongitude;		
	}
	
	public double getMinLongitude(){
		Iterator<Point> it = points.iterator();

		while(it.hasNext()){
			Point pt = (Point) it.next();
			if (pt.isGeocoded()){
				double tempLat = pt.getLongitude();
				if (tempLat < minLongitude || minLongitude == 1000){
					minLongitude = tempLat;
			}
			}
		}
		return minLongitude;	
	}
	
	public JSONArray toJSON(){
		JSONArray geom = new JSONArray();
		// future enhancement
		//JSONArray bbox = new JSONArray();
	
		//set up the geo points as an array of json objects
		Iterator<Point> it = points.iterator();
		while(it.hasNext()){
			geom.put(((Point)it.next()).toJSON());
		}
		return geom;
	}
}
