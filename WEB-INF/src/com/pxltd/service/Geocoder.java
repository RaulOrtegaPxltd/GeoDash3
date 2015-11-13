package com.pxltd.service;

import com.pxltd.spatial.Point;

public interface Geocoder {
	public Point geocode(String location);
}
