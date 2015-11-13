package com.pxltd.service;

import java.util.ArrayList;
import java.util.Iterator;

import com.microstrategy.utils.log.Level;
import com.microstrategy.web.app.tasks.architect.json.JSONException;
import com.pxltd.geodash.GeodashConfig;
import com.pxltd.geodash.GeodashException;
import com.pxltd.geodash.ServiceException;
import com.pxltd.geodash.ServiceException.GeoExceptionCodes;
import com.pxltd.spatial.Point;
import com.pxltd.spatial.Points;

public class GeocodingService {
	private GoogleGeocoder _gc;
	private int _requestInterval;
	
	final int DEFAULT_REQUEST_INTERVAL = 100;
	
	public GeocodingService() throws GeodashException, ServiceException{
		_gc = new GoogleGeocoder();		
		_requestInterval = Integer.parseInt(GeodashConfig.GEO_REQUEST_INTERVAL);
	}
	
	public Points geocode(String[] addresses) throws ServiceException, GeodashException{
		Points grs = new Points();
		
		for(int i=0;i<addresses.length;i++){
			grs.addPoint(i,geocode(addresses[i]));
		}
			
		return grs;
	}
	
	public Points geocode(ArrayList<String> addresses) throws ServiceException, GeodashException{
		Points grs = new Points();
		
		Iterator<String> addrIt = addresses.iterator();
		int counter = 0;
		while(addrIt.hasNext()){
			grs.addPoint(counter, geocode(addrIt.next()));
			counter++;
		}			
		return grs;
	}
	
	
	public Point geocode(String location) throws ServiceException, GeodashException{
		Point gr = (Point) GeocodeCache.get(location);
		if(gr != null) {
			// System.out.println("Returning cached point: " + gr);
			return gr;
		} else {
			// System.out.println("Gettig new point from Google: ");
		}
		
		try {
			gr = _gc.geocode(location);
		} catch (JSONException e) {
			throw new ServiceException("Failed to geocode:  " + location +" Possibly caused while reading google response.", 
					GeoExceptionCodes.BAD_REQUEST);
		}
		
		switch(gr.getState()){
		case OK:
		case ZERO_RESULTS:	
			break;
		case REQUEST_DENIED:
		case INVALID_REQUEST:
			Log.logger.logp(Level.SEVERE, "GeocodingService", "geocode","Request was denied possibly due to " +
					"missing components in the URL like address or client id.  " +
					"Authentication failure with the geocoding service is likely as well.");
			throw new ServiceException("Request was denied possibly due to missing components in the URL like address or client id.  " +
					"Authentication failure with the geocoding service is likely as well.", GeoExceptionCodes.BAD_REQUEST);
		case OVER_QUERY_LIMIT:
			Log.logger.logp(Level.SEVERE, "GeocodingService", "geocode","You've reached your geocoding quota limit for today.  You may want to" +
					"try adjusting your geoRequestInterval parameter.");
			throw new ServiceException("You've reached your geocoding quota limit for today.  You may want to" +
					"try adjusting your geoRequestInterval parameter.", GeoExceptionCodes.QUOTA_REACHED);
		default:
			break;
			
		}
		
		GeocodeCache.put(location, gr);
		pauseGeocoding();
		
		return gr;
	}
	
	private void pauseGeocoding(){
		try {
			Thread.sleep(_requestInterval);
		} catch (InterruptedException e) {
			return;
		}		
	}
}
