package com.pxltd.util;

import java.awt.Color;

public class GeoUtil {
	public static int mapSize(int zoom) {
		return 256 * zoom;
	}
	
	public static Bbox bboxForTile(int x, int y, int z) {
		double tilesAtZoom = Math.pow(2, z);
		double mercHeight = 1.0 / tilesAtZoom;
		double topLatMerc = y * mercHeight;
		double bottomLatMerc = topLatMerc + mercHeight;
		
		double topLat =    (180 / Math.PI) * ((2*Math.atan(Math.exp(Math.PI * (1 - (2 * topLatMerc)))))-(Math.PI/2));
		double bottomLat = (180 / Math.PI) * ((2*Math.atan(Math.exp(Math.PI * (1 - (2 * bottomLatMerc)))))-(Math.PI/2));

		double tlLng =  (360 / (Math.pow(2, z) / x)  - 180);
		return new Bbox(topLat, tlLng, bottomLat, tlLng + lngsInTile(z));
	}

	public static Bbox bboxForTileWithNeighbours(int x, int y, int z) {
		Bbox bbox = bboxForTile(x, y, z);
		int tilesPerRow = (int) Math.pow(2, z) / 2;
		
		// extend row above
		if(y > 0) {
			for(int i=-1; i<2; i++) {
				bbox.extend(bboxForTile(x + i, y - 1, z));
			}
		}
		// extend left and right
		bbox.extend(bboxForTile(x - 1, y, z));
		bbox.extend(bboxForTile(x + 1, y, z));
		
		// extend row below
		if(y < tilesPerRow) {
			for(int i=-1; i<2; i++) {
				bbox.extend(bboxForTile(x + i, y + 1, z));
			}
		}
		return bbox;
	}
	
	public static double lngsInTile(int z) {
		return 360.0 / Math.pow(2, z);
	}
	
	public static double degreesToRadians(int deg) {
		return Math.toRadians(deg);
	}
	
	public static XY toMercatorCoords(double lat, double lng) {
		if(lng > 180) {
			lng = lng - 360;
		}
		
		lng = lng / 360.0;
		lat = asinh(Math.tan((lat/180.0) * Math.PI)) / Math.PI / 2.0;
		
		return new XY(lng, lat	);
	}
	
	private static double asinh(double x) { 
		return Math.log(x + Math.sqrt(x*x + 1.0)); 
	}
	
	public static Color toAlpha(Color c, float alpha) {
		float[] components = c.getRGBComponents(null);
		return new Color(components[0], components[1], components[2], alpha);
	}
}
